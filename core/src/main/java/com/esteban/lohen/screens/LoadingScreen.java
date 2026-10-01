package com.esteban.lohen.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;

import com.esteban.lohen.LohenGame;
import com.esteban.lohen.ui.UiKit;

/** First screen: streams the assets in while showing a breathing heart. */
public class LoadingScreen extends BaseScreen {
    private float t;
    private boolean ready;
    private float readyTimer;
    private BitmapFont bootFont;
    private Texture heart;
    private float shown;

    public LoadingScreen(LohenGame game) {
        super(game);
        game.assets.queueAll();
        try {
            FreeTypeFontGenerator g = new FreeTypeFontGenerator(Gdx.files.internal("font/serif.ttf"));
            FreeTypeFontGenerator.FreeTypeFontParameter p = new FreeTypeFontGenerator.FreeTypeFontParameter();
            p.size = Math.max(18, Gdx.graphics.getHeight() / 26);
            p.color = new Color(0.976f, 0.949f, 0.906f, 1f);
            p.characters = FreeTypeFontGenerator.DEFAULT_CHARS + "\u00e9\u00e8\u00ea\u00e0\u00e7\u00f4\u2019\u2026";
            bootFont = g.generateFont(p);
            bootFont.getData().setScale(UiKit.H / Math.max(1, Gdx.graphics.getHeight()));
            g.dispose();
        } catch (Throwable e) {
            Gdx.app.error("Loading", "boot font failed", e);
        }
        try {
            heart = new Texture(Gdx.files.internal("img/heart.png"), true);
            heart.setFilter(Texture.TextureFilter.MipMapLinearLinear, Texture.TextureFilter.Linear);
        } catch (Throwable e) {
            Gdx.app.error("Loading", "heart missing", e);
        }
    }

    @Override
    public void render(float delta) {
        float dt = Math.min(delta, 1 / 20f);
        t += dt;
        clear();

        boolean done = game.assets.update();
        shown = MathUtils.lerp(shown, game.assets.progress(), Math.min(1f, dt * 5f));

        if (done && !ready) {
            ready = true;
            game.onAssetsReady();
        }
        if (ready) {
            readyTimer += dt;
            if (readyTimer > 0.6f) {
                game.audio.playMusic("audio/music_title.ogg");
                game.switchTo(new TitleScreen(game));
                return;
            }
        }

        game.viewport.apply();
        game.batch.setProjectionMatrix(game.viewport.getCamera().combined);
        game.batch.begin();

        if (heart != null) {
            float beat = 1f + 0.08f * Interpolation.sineOut.apply(Math.abs(MathUtils.sin(t * 2.1f)));
            float s = 180f * beat;
            game.batch.setColor(1f, 1f, 1f, 0.9f);
            game.batch.draw(heart, UiKit.W / 2f - s / 2, UiKit.H / 2f - s / 2 + 60f, s, s);
            game.batch.setColor(Color.WHITE);
        }

        // progress bar drawn with the heart's own texture tint
        float bw = 520f, bh = 6f;
        float bx = UiKit.W / 2f - bw / 2f, by = UiKit.H / 2f - 130f;
        if (heart != null) {
            game.batch.setColor(1f, 1f, 1f, 0.18f);
            game.batch.draw(heart, bx, by, bw, bh);
            game.batch.setColor(UiKit.GOLD);
            game.batch.draw(heart, bx, by, bw * shown, bh);
            game.batch.setColor(Color.WHITE);
        }

        if (bootFont != null) {
            bootFont.setColor(UiKit.CREAM.r, UiKit.CREAM.g, UiKit.CREAM.b, 0.85f);
            String msg = "un cadeau se prépare\u2026";
            com.badlogic.gdx.graphics.g2d.GlyphLayout gl = new com.badlogic.gdx.graphics.g2d.GlyphLayout(bootFont, msg);
            bootFont.draw(game.batch, gl, UiKit.W / 2f - gl.width / 2f, by - 60f);
        }

        game.batch.end();
    }

    @Override
    public void dispose() {
        if (bootFont != null) bootFont.dispose();
        if (heart != null) heart.dispose();
    }
}
