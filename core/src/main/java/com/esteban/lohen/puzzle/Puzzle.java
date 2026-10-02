package com.esteban.lohen.puzzle;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.JsonValue;

import com.esteban.lohen.Assets;
import com.esteban.lohen.AudioManager;
import com.esteban.lohen.ui.Fx;
import com.esteban.lohen.ui.UiKit;

/**
 * Base class of every puzzle. A puzzle owns its own touch handling and
 * rendering inside the scene; it never requires a joystick, only taps, long
 * presses and drags. Content (prompts, solutions, hints) comes from JSON.
 */
public abstract class Puzzle {
    protected JsonValue cfg;
    protected UiKit ui;
    protected Fx fx;
    protected AudioManager audio;
    protected Assets assets;
    protected boolean solved;
    protected float time;
    protected float feedback;          // >0 red shake, <0 green pulse
    public int attempts;

    public void init(JsonValue cfg, UiKit ui, Fx fx, AudioManager audio, Assets assets) {
        this.cfg = cfg;
        this.ui = ui;
        this.fx = fx;
        this.audio = audio;
        this.assets = assets;
        setup();
    }

    protected abstract void setup();

    public void update(float dt) {
        time += dt;
        if (feedback > 0) feedback = Math.max(0, feedback - dt * 1.6f);
        if (feedback < 0) feedback = Math.min(0, feedback + dt * 1.6f);
    }

    public abstract void draw(SpriteBatch b);

    public void touchDown(float x, float y) { }

    public void touchDragged(float x, float y) { }

    public void touchUp(float x, float y) { }

    public boolean isSolved() { return solved; }

    /** How much the scene behind should be dimmed (0 = fully visible). */
    public float dim() { return 0.72f; }

    public String prompt() { return cfg.getString("prompt", ""); }

    public String riddle() { return cfg.getString("riddle", null); }

    public String hint() { return cfg.getString("hint", ""); }

    protected void succeed(float x, float y) {
        if (solved) return;
        com.badlogic.gdx.Gdx.app.log("Milestone", "puzzle-solved " + getClass().getSimpleName());
        solved = true;
        feedback = -1f;
        audio.unlock();
        fx.burst(Fx.Kind.SPARK, x, y, 42);
    }

    protected void fail() {
        attempts++;
        feedback = 1f;
        audio.wrong();
    }

    public static Puzzle create(String type) {
        if ("order".equals(type)) return new OrderPuzzle();
        if ("lanterns".equals(type)) return new LanternPuzzle();
        if ("riddle".equals(type)) return new RiddlePuzzle();
        if ("constellation".equals(type)) return new ConstellationPuzzle();
        if ("melody".equals(type)) return new MelodyPuzzle();
        if ("code".equals(type)) return new CodePuzzle();
        throw new IllegalArgumentException("unknown puzzle type: " + type);
    }
}
