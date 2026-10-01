package com.esteban.lohen;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.FitViewport;

import com.esteban.lohen.game.Content;
import com.esteban.lohen.screens.LoadingScreen;
import com.esteban.lohen.ui.Fx;
import com.esteban.lohen.ui.UiKit;

/**
 * Root application class. Owns the global services (assets, audio, save state,
 * design system, particles, narrative content) and the screen stack.
 */
public class LohenGame extends Game {
    public SpriteBatch batch;
    public ShapeRenderer shapes;
    public FitViewport viewport;
    public Assets assets;
    public GameState state;
    public AudioManager audio;
    public UiKit ui;
    public Fx fx;
    public Content content;

    @Override
    public void create() {
        Gdx.app.setLogLevel(com.badlogic.gdx.Application.LOG_INFO);
        batch = new SpriteBatch();
        shapes = new ShapeRenderer();
        viewport = new FitViewport(UiKit.W, UiKit.H);
        state = new GameState();
        state.load();
        assets = new Assets();
        audio = new AudioManager(assets, state);
        Gdx.app.log("Lohen", "boot: save v" + GameState.SAVE_VERSION + " chapter=" + state.chapter);
        setScreen(new LoadingScreen(this));
    }

    /** Called by the loading screen once textures/fonts/audio are available. */
    public void onAssetsReady() {
        ui = new UiKit();
        fx = new Fx(assets.tex("img/glow.png"), assets.tex("img/spark.png"), assets.tex("img/petal.png"));
        fx.reducedMotion = state.reducedMotion;
        content = Content.load(Gdx.files.internal("data/chapters.json"), Gdx.files.internal("data/letter.json"));
        String problems = content.validate();
        if (problems.length() > 0) Gdx.app.error("Content", "validation problems:\n" + problems);
        else Gdx.app.log("Content", "validated: " + content.chapters.size + " chapters");
    }

    @Override
    public void render() {
        float dt = Math.min(Gdx.graphics.getDeltaTime(), 1 / 20f);
        if (audio != null) audio.update(dt);
        super.render();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        super.resize(width, height);
    }

    @Override
    public void pause() {
        if (state != null) state.save();
        super.pause();
    }

    public void switchTo(Screen next) {
        Screen old = getScreen();
        setScreen(next);
        if (old != null) old.dispose();
    }

    @Override
    public void dispose() {
        if (getScreen() != null) getScreen().dispose();
        if (state != null) state.save();
        if (batch != null) batch.dispose();
        if (shapes != null) shapes.dispose();
        if (ui != null) ui.dispose();
        if (audio != null) audio.dispose();
        if (assets != null) assets.dispose();
    }
}
