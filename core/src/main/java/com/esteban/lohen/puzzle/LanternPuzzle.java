package com.esteban.lohen.puzzle;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.JsonValue;

import com.esteban.lohen.ui.Fx;
import com.esteban.lohen.ui.UiKit;

/**
 * Chapter 2 — light the five lanterns in the order given by a spoken riddle.
 * The scene stays visible (dim 0.25): the puzzle happens inside the painting.
 * A wrong lantern gently resets the chain instead of punishing the player.
 */
public class LanternPuzzle extends Puzzle {
    private float[] lx, ly;
    private int[] solution;
    private int progress;
    private float[] lit;      // 0..1 per lantern
    private float resetTimer;

    @Override
    protected void setup() {
        JsonValue arr = cfg.get("lanterns");
        lx = new float[arr.size];
        ly = new float[arr.size];
        lit = new float[arr.size];
        int i = 0;
        for (JsonValue v = arr.child; v != null; v = v.next) {
            lx[i] = v.getFloat("x") * UiKit.W;
            ly[i] = (1f - v.getFloat("y")) * UiKit.H;
            i++;
        }
        JsonValue sol = cfg.get("solution");
        solution = new int[sol.size];
        i = 0;
        for (JsonValue v = sol.child; v != null; v = v.next) solution[i++] = v.asInt();
    }

    @Override public float dim() { return 0.22f; }

    /** Nearest lantern within reach: neighbours in the painting can overlap. */
    private int nearest(float x, float y) {
        int best = -1;
        float bestD = Float.MAX_VALUE;
        for (int i = 0; i < lx.length; i++) {
            float dx = x - lx[i], dy = y - ly[i];
            float d = dx * dx + dy * dy;
            if (d < bestD) { bestD = d; best = i; }
        }
        return (best >= 0 && bestD < 150f * 150f) ? best : -1;
    }

    @Override
    public void touchDown(float x, float y) {
        if (solved || resetTimer > 0) return;
        int hit = nearest(x, y);
        for (int i = 0; i < lx.length; i++) {
            if (i == hit) {
                if (lit[i] > 0.1f) { audio.tap(); return; }
                if (solution[progress] == i) {
                    lit[i] = 0.001f;
                    audio.note(progress);
                    fx.burst(Fx.Kind.SPARK, lx[i], ly[i], 14);
                    progress++;
                    if (progress >= solution.length) succeed(lx[i], ly[i]);
                } else {
                    fail();
                    resetTimer = 0.9f;
                }
                return;
            }
        }
    }

    @Override
    public void update(float dt) {
        super.update(dt);
        for (int i = 0; i < lit.length; i++) {
            if (lit[i] > 0f && lit[i] < 1f) lit[i] = Math.min(1f, lit[i] + dt * 2.2f);
            if (lit[i] > 0.4f && MathUtils.random() < dt * 1.6f) {
                fx.spawn(Fx.Kind.FIREFLY, lx[i] + MathUtils.random(-40, 40), ly[i] + MathUtils.random(-20, 40));
            }
        }
        if (resetTimer > 0) {
            resetTimer -= dt;
            if (resetTimer <= 0) {
                for (int i = 0; i < lit.length; i++) lit[i] = 0f;
                progress = 0;
            }
        }
    }

    @Override
    public void draw(SpriteBatch b) {
        Texture glow = assets.tex("img/glow.png");
        int srcF = b.getBlendSrcFunc(), dstF = b.getBlendDstFunc();
        b.setBlendFunction(com.badlogic.gdx.graphics.GL20.GL_SRC_ALPHA, com.badlogic.gdx.graphics.GL20.GL_ONE);
        for (int i = 0; i < lx.length; i++) {
            float pulse = 0.55f + 0.45f * MathUtils.sin(time * 2.2f + i);
            if (lit[i] > 0f) {
                float s = 330f * lit[i];
                b.setColor(1f, 0.82f, 0.48f, 0.85f * lit[i]);
                b.draw(glow, lx[i] - s / 2, ly[i] - s / 2, s, s);
                float s2 = 130f * lit[i] * (0.9f + 0.1f * pulse);
                b.setColor(1f, 0.95f, 0.8f, lit[i]);
                b.draw(glow, lx[i] - s2 / 2, ly[i] - s2 / 2, s2, s2);
            } else if (!solved) {
                // unlit lanterns breathe faintly so they are discoverable
                float s = 150f;
                b.setColor(0.65f, 0.75f, 1f, 0.16f + 0.14f * pulse);
                b.draw(glow, lx[i] - s / 2, ly[i] - s / 2, s, s);
            }
        }
        b.setColor(Color.WHITE);
        b.setBlendFunction(srcF, dstF);

        if (!solved) {
            for (int i = 0; i < lx.length; i++) {
                if (lit[i] > 0.05f) continue;
                ui.small.setColor(UiKit.MOON.r, UiKit.MOON.g, UiKit.MOON.b, 0.75f);
                ui.centered(b, ui.small, String.valueOf(i + 1), lx[i], ly[i] - 70f);
                ui.small.setColor(Color.WHITE);
            }
            ui.small.setColor(UiKit.ROSE);
            ui.centered(b, ui.small, progress + " / " + solution.length + " lanternes allumées",
                    UiKit.W / 2f, 140f);
            ui.small.setColor(Color.WHITE);
        }
    }
}
