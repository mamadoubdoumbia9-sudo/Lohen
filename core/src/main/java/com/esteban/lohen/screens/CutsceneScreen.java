package com.esteban.lohen.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import com.esteban.lohen.LohenGame;
import com.esteban.lohen.ui.TouchButton;
import com.esteban.lohen.ui.UiKit;

/**
 * Plays a pre-rendered cutscene: a sequence of 1600x900 JPEG frames streamed
 * from the APK assets at a fixed frame rate, with French captions on top.
 *
 * Android/libGDX has no video decoder we can rely on, so the cutscenes are
 * shipped as frame sequences (see tools/gen_cutscenes.py). Frames are decoded
 * on a worker thread into Pixmaps and uploaded to a GL texture on the render
 * thread, which keeps playback smooth and memory flat (a handful of frames in
 * flight, never the whole scene).
 *
 * If the cutscene assets are absent (lightweight build), the screen is a no-op
 * and hands over to the next screen immediately.
 */
public class CutsceneScreen extends BaseScreen {
    private static final int QUEUE = 4;

    private final String id;
    private final Runnable onDone;
    private final TouchButton skipBtn;

    private int frames;
    private float fps = 18f;
    private final Array<float[]> capTimes = new Array<float[]>();
    private final Array<String> capTexts = new Array<String>();

    private ArrayBlockingQueue<Pixmap> queue;
    private Thread loader;
    private volatile boolean stop;

    private Texture current;
    private float clock;
    private int shown = -1;
    private boolean handedOver;
    private float holdLast;

    public CutsceneScreen(LohenGame game, String id, Runnable onDone) {
        super(game);
        this.id = id;
        this.onDone = onDone;
        skipBtn = new TouchButton("Passer", UiKit.W - 260f, 60f, 200f, 78f);
        skipBtn.visible = false;
        load();
    }

    /** True when the cutscene assets for this id are present in the build. */
    public static boolean available(String id) {
        FileHandle m = Gdx.files.internal("cutscene/manifest.json");
        if (!m.exists()) return false;
        try {
            JsonValue scenes = new JsonReader().parse(m).get("scenes");
            JsonValue s = scenes == null ? null : scenes.get(id);
            return s != null && s.getInt("frames", 0) > 1
                    && Gdx.files.internal("cutscene/" + id + "/f0000.jpg").exists();
        } catch (Exception e) {
            return false;
        }
    }

    private void load() {
        if (!available(id)) { frames = 0; return; }
        JsonValue s = new JsonReader().parse(Gdx.files.internal("cutscene/manifest.json"));
        fps = s.getFloat("fps", 18f);
        JsonValue scene = s.get("scenes").get(id);
        frames = scene.getInt("frames", 0);
        JsonValue caps = scene.get("captions");
        if (caps != null) {
            for (JsonValue c = caps.child; c != null; c = c.next) {
                capTimes.add(new float[]{c.getFloat(0)});
                capTexts.add(c.getString(1));
            }
        }
        queue = new ArrayBlockingQueue<Pixmap>(QUEUE);
        loader = new Thread(new Runnable() {
            @Override public void run() {
                for (int i = 0; i < frames && !stop; i++) {
                    FileHandle fh = Gdx.files.internal(
                            "cutscene/" + id + "/" + String.format("f%04d.jpg", i));
                    if (!fh.exists()) break;
                    try {
                        byte[] bytes = fh.readBytes();
                        Pixmap p = new Pixmap(bytes, 0, bytes.length);
                        while (!stop && !queue.offer(p, 200, TimeUnit.MILLISECONDS)) { /* wait */ }
                        if (stop) p.dispose();
                    } catch (Exception e) {
                        Gdx.app.error("Cutscene", "frame " + i + " of " + id + ": " + e);
                        break;
                    }
                }
            }
        }, "cutscene-" + id);
        loader.setDaemon(true);
        loader.start();
        if ("letter".equals(id)) {
            game.audio.playCue("hd_letter", "audio/music_letter.ogg");
        } else if ("ch6".equals(id)) {
            game.audio.playCue("hd_cutscene_dawn", "audio/music_memory.ogg");
        } else {
            game.audio.playCue("hd_cutscene_night", "audio/music_night.ogg");
        }
    }

    @Override
    public void show() {
        super.show();
        if (frames == 0) finish();
    }

    private void finish() {
        if (handedOver) return;
        handedOver = true;
        stop = true;
        fadeTo(new Runnable() { public void run() { onDone.run(); } });
    }

    @Override
    protected void onTouchDown(float x, float y) {
        if (frames == 0) return;
        if (skipBtn.visible && skipBtn.hit(x, y)) finish();
    }

    @Override
    protected void onBack() { finish(); }

    @Override
    public void render(float dt) {
        updateFades(dt);
        clear();

        if (frames > 0 && !handedOver) {
            clock += dt;
            skipBtn.visible = clock > 2f;
            int want = (int) (clock * fps);
            // pull the frames we are late for; never block the render thread
            while (shown < want && shown < frames - 1) {
                Pixmap p = queue.poll();
                if (p == null) break;
                if (current != null) current.dispose();
                current = new Texture(p);
                current.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
                p.dispose();
                shown++;
            }
            if (shown >= frames - 1) {
                holdLast += dt;
                if (holdLast > 0.6f) finish();
            }
        }

        begin();
        if (current != null) {
            game.batch.setColor(Color.WHITE);
            game.batch.draw(current, 0, 0, UiKit.W, UiKit.H);
        }
        game.ui.drawVignette(game.batch, 0.45f);
        drawCaption();
        if (skipBtn.visible) { skipBtn.update(Gdx.graphics.getDeltaTime()); skipBtn.draw(game.batch, game.ui); }
        drawFades();
        end();
    }

    private void drawCaption() {
        String text = null;
        float age = 0f;
        for (int i = 0; i < capTimes.size; i++) {
            float start = capTimes.get(i)[0];
            float end = (i + 1 < capTimes.size) ? capTimes.get(i + 1)[0] - 0.25f : start + 4.5f;
            if (clock >= start && clock <= end) { text = capTexts.get(i); age = clock - start; }
        }
        if (text == null) return;
        float alpha = Math.min(1f, age / 0.6f);
        float w = UiKit.W - 440f;
        float h = game.ui.measureWrapped(game.ui.body, text, w);
        game.ui.rect(game.batch, 0, 40f, UiKit.W, h + 110f, UiKit.DEEP, 0.55f * alpha);
        game.ui.body.setColor(UiKit.CREAM.r, UiKit.CREAM.g, UiKit.CREAM.b, alpha);
        game.ui.wrappedCentered(game.batch, game.ui.body, text, UiKit.W / 2f, 90f + h, w);
        game.ui.body.setColor(Color.WHITE);
    }

    @Override
    public void dispose() {
        stop = true;
        if (loader != null) loader.interrupt();
        if (queue != null) {
            Pixmap p;
            while ((p = queue.poll()) != null) p.dispose();
        }
        if (current != null) { current.dispose(); current = null; }
    }
}
