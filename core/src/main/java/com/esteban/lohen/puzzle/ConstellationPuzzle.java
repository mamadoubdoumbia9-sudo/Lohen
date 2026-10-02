package com.esteban.lohen.puzzle;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.JsonValue;

import com.esteban.lohen.ui.Fx;
import com.esteban.lohen.ui.UiKit;

/**
 * Chapter 4 — draw a constellation by dragging a finger from star to star.
 * A real drag gesture (touch down, move, lift) with forgiving 120px targets.
 * Lifting the finger early simply clears the trace, nothing is lost.
 */
public class ConstellationPuzzle extends Puzzle {
    private Vector2[] stars;
    private int[] path;
    private int progress;
    private boolean dragging;
    private final Vector2 finger = new Vector2();
    private float[] twinkle;

    @Override
    protected void setup() {
        JsonValue arr = cfg.get("stars");
        stars = new Vector2[arr.size];
        twinkle = new float[arr.size];
        int i = 0;
        for (JsonValue v = arr.child; v != null; v = v.next) {
            stars[i] = new Vector2(v.getFloat("x") * UiKit.W, (1f - v.getFloat("y")) * UiKit.H);
            twinkle[i] = MathUtils.random(MathUtils.PI2);
            i++;
        }
        JsonValue p = cfg.get("path");
        path = new int[p.size];
        i = 0;
        for (JsonValue v = p.child; v != null; v = v.next) path[i++] = v.asInt();
    }

    @Override public float dim() { return 0.25f; }

    /**
     * Nearest star within reach, not the first one found: two stars of the
     * heart are only 135 px apart, so a first-match test could return the
     * wrong one for a finger sitting between them.
     */
    private int starAt(float x, float y) {
        int best = -1;
        float bestD2 = 130f * 130f;
        for (int i = 0; i < stars.length; i++) {
            float d2 = stars[i].dst2(x, y);
            if (d2 < bestD2) { bestD2 = d2; best = i; }
        }
        return best;
    }

    @Override
    public void touchDown(float x, float y) {
        if (solved) return;
        finger.set(x, y);
        int s = starAt(x, y);
        if (s == path[0]) {
            dragging = true;
            progress = 1;
            audio.note(0);
            fx.burst(Fx.Kind.SPARK, stars[s].x, stars[s].y, 10);
        } else {
            dragging = false;
            progress = 0;
        }
    }

    @Override
    public void touchDragged(float x, float y) {
        if (solved || !dragging) return;
        finger.set(x, y);
        int s = starAt(x, y);
        if (s < 0) return;
        int next = path[progress];
        if (s == next) {
            progress++;
            audio.note(progress);
            fx.burst(Fx.Kind.SPARK, stars[s].x, stars[s].y, 10);
            if (progress >= path.length) {
                dragging = false;
                succeed(UiKit.W / 2f, UiKit.H * 0.55f);
            }
        } else if (progress >= 2 && s != path[progress - 1] && s != path[progress - 2]) {
            // wrong star: soft reset
            dragging = false;
            progress = 0;
            fail();
        }
    }

    @Override
    public void touchUp(float x, float y) {
        if (solved) return;
        if (progress < path.length) progress = 0;
        dragging = false;
    }

    @Override
    public void update(float dt) {
        super.update(dt);
        if (solved && MathUtils.random() < dt * 8f) {
            int i = MathUtils.random(stars.length - 1);
            fx.spawn(Fx.Kind.STAR, stars[i].x + MathUtils.random(-30, 30), stars[i].y + MathUtils.random(-30, 30));
        }
    }

    @Override
    public void draw(SpriteBatch b) {
        Texture glow = assets.tex("img/glow.png");
        Texture spark = assets.tex("img/spark.png");
        int srcF = b.getBlendSrcFunc(), dstF = b.getBlendDstFunc();
        b.setBlendFunction(com.badlogic.gdx.graphics.GL20.GL_SRC_ALPHA, com.badlogic.gdx.graphics.GL20.GL_ONE);

        // drawn segments
        for (int i = 0; i + 1 < progress; i++) {
            line(b, spark, stars[path[i]], stars[path[i + 1]], 1f);
        }
        if (dragging && progress > 0 && progress < path.length) {
            line(b, spark, stars[path[progress - 1]], finger, 0.45f);
        }

        for (int i = 0; i < stars.length; i++) {
            boolean done = false;
            for (int k = 0; k < progress; k++) if (path[k] == i) done = true;
            float tw = 0.6f + 0.4f * MathUtils.sin(time * 2f + twinkle[i]);
            float s = done ? 150f : 110f;
            b.setColor(done ? 1f : 0.78f, done ? 0.92f : 0.85f, done ? 0.75f : 1f, (done ? 0.95f : 0.5f) * tw);
            b.draw(glow, stars[i].x - s / 2, stars[i].y - s / 2, s, s);
            b.setColor(1f, 1f, 1f, done ? 1f : 0.75f * tw);
            b.draw(spark, stars[i].x - 26, stars[i].y - 26, 52, 52);
        }
        b.setColor(Color.WHITE);
        b.setBlendFunction(srcF, dstF);

        if (!solved) {
            ui.small.setColor(UiKit.MOON);
            ui.centered(b, ui.small, progress == 0
                            ? "Pose ton doigt sur l'étoile la plus haute et glisse, sans lever le doigt."
                            : "Continue… " + progress + " / " + path.length,
                    UiKit.W / 2f, 120f);
            ui.small.setColor(Color.WHITE);
        }
    }

    private void line(SpriteBatch b, Texture spark, Vector2 a, Vector2 c, float alpha) {
        float dist = a.dst(c);
        int steps = Math.max(2, (int) (dist / 14f));
        for (int i = 0; i <= steps; i++) {
            float t = i / (float) steps;
            float x = a.x + (c.x - a.x) * t;
            float y = a.y + (c.y - a.y) * t;
            float s = 22f + 6f * MathUtils.sin(time * 6f + i);
            b.setColor(1f, 0.88f, 0.72f, 0.5f * alpha);
            b.draw(spark, x - s / 2, y - s / 2, s, s);
        }
    }
}
