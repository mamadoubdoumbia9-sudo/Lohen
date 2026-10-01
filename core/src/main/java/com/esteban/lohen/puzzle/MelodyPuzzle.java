package com.esteban.lohen.puzzle;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.JsonValue;

import com.esteban.lohen.ui.Fx;
import com.esteban.lohen.ui.UiKit;

/**
 * Chapter 5 — memory puzzle on the music box. The melody can be replayed as
 * many times as the player wants; every note has a distinct colour and a
 * distinct height so the puzzle is solvable without sound (accessibility).
 */
public class MelodyPuzzle extends Puzzle {
    private int[] sequence;
    private int input;
    private float[] pulse;
    private boolean playing;
    private int playIndex;
    private float playTimer;
    private boolean hasListened;

    private static final Color[] NOTE_COLORS = {
            new Color(0.95f, 0.55f, 0.60f, 1f),
            new Color(0.98f, 0.78f, 0.45f, 1f),
            new Color(0.70f, 0.88f, 0.70f, 1f),
            new Color(0.60f, 0.78f, 0.96f, 1f),
            new Color(0.82f, 0.66f, 0.95f, 1f)
    };

    private final float orbR = 92f;
    private float orbY = 400f;
    private float listenX, listenY = 170f, listenW = 420f, listenH = 96f;

    @Override
    protected void setup() {
        JsonValue s = cfg.get("sequence");
        sequence = new int[s.size];
        int i = 0;
        for (JsonValue v = s.child; v != null; v = v.next) sequence[i++] = v.asInt();
        pulse = new float[5];
        listenX = UiKit.W / 2f - listenW / 2f;
    }

    private float orbX(int i) { return UiKit.W / 2f - 2 * 260f + i * 260f; }

    private float orbHeight(int i) { return orbY + i * 26f; }

    @Override
    public void touchDown(float x, float y) {
        if (solved || playing) return;
        if (x >= listenX && x <= listenX + listenW && y >= listenY && y <= listenY + listenH) {
            playing = true;
            playIndex = 0;
            playTimer = 0f;
            input = 0;
            hasListened = true;
            return;
        }
        if (!hasListened) return;
        for (int i = 0; i < 5; i++) {
            float dx = x - orbX(i), dy = y - orbHeight(i);
            if (dx * dx + dy * dy < (orbR + 24f) * (orbR + 24f)) {
                pulse[i] = 1f;
                audio.note(i);
                fx.burst(Fx.Kind.SPARK, orbX(i), orbHeight(i), 8);
                if (sequence[input] == i) {
                    input++;
                    if (input >= sequence.length) succeed(UiKit.W / 2f, orbY + 80f);
                } else {
                    input = 0;
                    fail();
                }
                return;
            }
        }
    }

    @Override
    public void update(float dt) {
        super.update(dt);
        for (int i = 0; i < pulse.length; i++) pulse[i] = Math.max(0, pulse[i] - dt * 1.8f);
        if (playing) {
            playTimer -= dt;
            if (playTimer <= 0f) {
                if (playIndex < sequence.length) {
                    int n = sequence[playIndex];
                    pulse[n] = 1f;
                    audio.note(n);
                    fx.burst(Fx.Kind.SPARK, orbX(n), orbHeight(n), 6);
                    playIndex++;
                    playTimer = 0.62f;
                } else {
                    playing = false;
                }
            }
        }
    }

    @Override
    public void draw(SpriteBatch b) {
        Texture glow = assets.tex("img/glow.png");
        int srcF = b.getBlendSrcFunc(), dstF = b.getBlendDstFunc();
        b.setBlendFunction(com.badlogic.gdx.graphics.GL20.GL_SRC_ALPHA, com.badlogic.gdx.graphics.GL20.GL_ONE);
        for (int i = 0; i < 5; i++) {
            Color c = NOTE_COLORS[i];
            float p = pulse[i];
            float idle = 0.35f + 0.1f * MathUtils.sin(time * 1.6f + i);
            float s = orbR * 2 * (1f + 0.22f * p);
            b.setColor(c.r, c.g, c.b, (idle + 0.65f * p));
            b.draw(glow, orbX(i) - s / 2, orbHeight(i) - s / 2, s, s);
            float s2 = orbR * (0.9f + 0.3f * p);
            b.setColor(1f, 1f, 1f, 0.25f + 0.7f * p);
            b.draw(glow, orbX(i) - s2 / 2, orbHeight(i) - s2 / 2, s2, s2);
        }
        b.setColor(Color.WHITE);
        b.setBlendFunction(srcF, dstF);

        if (solved) return;

        ui.drawPanelLight(b, listenX, listenY, listenW, listenH, playing ? 0.6f : 1f);
        ui.body.setColor(UiKit.GOLD);
        ui.centered(b, ui.body, playing ? "écoute…" : (hasListened ? "Réécouter la mélodie" : "Écouter la mélodie"),
                UiKit.W / 2f, listenY + listenH / 2f + 16f);
        ui.body.setColor(Color.WHITE);

        // progress pearls
        float pw = sequence.length * 40f;
        for (int i = 0; i < sequence.length; i++) {
            float x = UiKit.W / 2f - pw / 2f + i * 40f;
            ui.rect(b, x, 300f, 26f, 26f, i < input ? UiKit.GOLD : UiKit.PLUM, i < input ? 1f : 0.6f);
        }

        ui.small.setColor(UiKit.MOON);
        ui.centered(b, ui.small, hasListened
                        ? "Rejoue les notes dans le même ordre."
                        : "Commence par écouter la mélodie d'Esteban.",
                UiKit.W / 2f, 268f);
        ui.small.setColor(Color.WHITE);
    }
}
