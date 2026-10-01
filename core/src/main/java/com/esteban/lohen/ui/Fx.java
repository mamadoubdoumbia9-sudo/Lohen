package com.esteban.lohen.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

/**
 * Lightweight particle layer: fireflies, drifting petals, dust and sparkle
 * bursts. Pooled, allocation free in steady state, and fully disabled when the
 * player turns on "mouvement réduit" in the accessibility settings.
 */
public class Fx {
    public enum Kind { FIREFLY, PETAL, DUST, SPARK, STAR }

    private static class P {
        float x, y, vx, vy, life, maxLife, size, rot, rotV, phase;
        Kind kind;
        final Color color = new Color();
        boolean alive;
    }

    private final Array<P> pool = new Array<P>();
    private final Texture glow, spark, petal;
    public boolean reducedMotion = false;

    public Fx(Texture glow, Texture spark, Texture petal) {
        this.glow = glow;
        this.spark = spark;
        this.petal = petal;
        for (int i = 0; i < 260; i++) pool.add(new P());
    }

    private P obtain() {
        for (int i = 0; i < pool.size; i++) {
            P p = pool.get(i);
            if (!p.alive) return p;
        }
        return null;
    }

    public void ambient(Kind kind, float dt, float rate, float w, float h) {
        if (reducedMotion) return;
        if (MathUtils.random() < rate * dt) spawn(kind, MathUtils.random(w), MathUtils.random(h));
    }

    public void burst(Kind kind, float x, float y, int count) {
        for (int i = 0; i < count; i++) spawn(kind, x, y);
    }

    public void spawn(Kind kind, float x, float y) {
        P p = obtain();
        if (p == null) return;
        p.alive = true;
        p.kind = kind;
        p.x = x;
        p.y = y;
        p.phase = MathUtils.random(MathUtils.PI2);
        p.rot = MathUtils.random(360f);
        p.rotV = MathUtils.random(-50f, 50f);
        switch (kind) {
            case FIREFLY:
                p.vx = MathUtils.random(-18f, 18f);
                p.vy = MathUtils.random(-8f, 14f);
                p.maxLife = MathUtils.random(5f, 11f);
                p.size = MathUtils.random(26f, 54f);
                p.color.set(1f, 0.86f, 0.52f, 1f);
                break;
            case PETAL:
                p.vx = MathUtils.random(-55f, -15f);
                p.vy = MathUtils.random(-70f, -35f);
                p.maxLife = MathUtils.random(6f, 10f);
                p.size = MathUtils.random(22f, 40f);
                p.color.set(1f, 0.82f, 0.86f, 1f);
                break;
            case DUST:
                p.vx = MathUtils.random(-10f, 10f);
                p.vy = MathUtils.random(4f, 20f);
                p.maxLife = MathUtils.random(6f, 12f);
                p.size = MathUtils.random(8f, 18f);
                p.color.set(1f, 0.95f, 0.85f, 1f);
                break;
            case SPARK:
                float a = MathUtils.random(MathUtils.PI2);
                float s = MathUtils.random(90f, 320f);
                p.vx = MathUtils.cos(a) * s;
                p.vy = MathUtils.sin(a) * s;
                p.maxLife = MathUtils.random(0.6f, 1.4f);
                p.size = MathUtils.random(14f, 34f);
                p.color.set(1f, 0.9f, 0.68f, 1f);
                break;
            default: // STAR twinkle
                p.vx = 0;
                p.vy = 0;
                p.maxLife = MathUtils.random(2f, 5f);
                p.size = MathUtils.random(10f, 26f);
                p.color.set(0.92f, 0.95f, 1f, 1f);
                break;
        }
        p.life = p.maxLife;
    }

    public void update(float dt, float w, float h) {
        for (int i = 0; i < pool.size; i++) {
            P p = pool.get(i);
            if (!p.alive) continue;
            p.life -= dt;
            if (p.life <= 0) { p.alive = false; continue; }
            p.phase += dt * 2.2f;
            switch (p.kind) {
                case FIREFLY:
                    p.x += (p.vx + MathUtils.sin(p.phase) * 24f) * dt;
                    p.y += (p.vy + MathUtils.cos(p.phase * 0.7f) * 16f) * dt;
                    break;
                case PETAL:
                    p.x += (p.vx + MathUtils.sin(p.phase * 0.8f) * 30f) * dt;
                    p.y += p.vy * dt;
                    p.rot += p.rotV * dt;
                    break;
                case SPARK:
                    p.vy -= 240f * dt;
                    p.x += p.vx * dt;
                    p.y += p.vy * dt;
                    break;
                default:
                    p.x += p.vx * dt;
                    p.y += p.vy * dt;
            }
            if (p.x < -120 || p.x > w + 120 || p.y < -140 || p.y > h + 140) p.alive = false;
        }
    }

    public void draw(SpriteBatch b) {
        int blendSrc = b.getBlendSrcFunc(), blendDst = b.getBlendDstFunc();
        b.setBlendFunction(com.badlogic.gdx.graphics.GL20.GL_SRC_ALPHA, com.badlogic.gdx.graphics.GL20.GL_ONE);
        for (int i = 0; i < pool.size; i++) {
            P p = pool.get(i);
            if (!p.alive) continue;
            float t = p.life / p.maxLife;
            float fade = t > 0.75f ? (1f - t) / 0.25f : Math.min(1f, t / 0.3f);
            float twinkle = p.kind == Kind.FIREFLY || p.kind == Kind.STAR
                    ? 0.55f + 0.45f * MathUtils.sin(p.phase * 1.7f) : 1f;
            float a = fade * twinkle;
            if (p.kind == Kind.PETAL) {
                b.setBlendFunction(blendSrc, blendDst);
                b.setColor(p.color.r, p.color.g, p.color.b, a * 0.9f);
                b.draw(petal, p.x - p.size / 2, p.y - p.size / 2, p.size / 2, p.size / 2,
                        p.size, p.size, 1f, 1f, p.rot, 0, 0, petal.getWidth(), petal.getHeight(), false, false);
                b.setBlendFunction(com.badlogic.gdx.graphics.GL20.GL_SRC_ALPHA, com.badlogic.gdx.graphics.GL20.GL_ONE);
            } else {
                Texture tex = p.kind == Kind.SPARK ? spark : glow;
                b.setColor(p.color.r, p.color.g, p.color.b, a * 0.75f);
                b.draw(tex, p.x - p.size / 2, p.y - p.size / 2, p.size, p.size);
            }
        }
        b.setColor(Color.WHITE);
        b.setBlendFunction(blendSrc, blendDst);
    }

    public void clear() {
        for (int i = 0; i < pool.size; i++) pool.get(i).alive = false;
    }
}
