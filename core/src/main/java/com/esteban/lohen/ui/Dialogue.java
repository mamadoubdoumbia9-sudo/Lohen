package com.esteban.lohen.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

/**
 * Narrative box with a typewriter reveal. A tap completes the current line
 * instantly, a second tap advances: never a forced wait, never a forced skip.
 */
public class Dialogue {
    public static class Line {
        public final String speaker;   // null = narration
        public final String text;
        public final String portrait;  // asset path or null

        public Line(String speaker, String text, String portrait) {
            this.speaker = speaker;
            this.text = text;
            this.portrait = portrait;
        }
    }

    private final Array<Line> queue = new Array<Line>();
    private int index = -1;
    private float revealed = 0f;
    private float appear = 0f;
    public boolean active = false;
    private float charsPerSecond = 42f;
    public float speedMultiplier = 1f;

    public void push(String speaker, String text) { push(speaker, text, null); }

    public void push(String speaker, String text, String portrait) {
        queue.add(new Line(speaker, text, portrait));
        if (!active) { active = true; index = 0; revealed = 0f; }
    }

    public void clear() {
        queue.clear();
        index = -1;
        active = false;
        revealed = 0f;
        appear = 0f;
    }

    public boolean isActive() { return active && index >= 0 && index < queue.size; }

    public Line current() { return isActive() ? queue.get(index) : null; }

    public boolean isLineComplete() {
        Line l = current();
        return l == null || revealed >= l.text.length();
    }

    /** @return true if the whole conversation just ended. */
    public boolean advance() {
        if (!isActive()) return false;
        if (!isLineComplete()) {
            revealed = current().text.length();
            return false;
        }
        index++;
        revealed = 0f;
        if (index >= queue.size) {
            clear();
            return true;
        }
        return false;
    }

    public void update(float dt) {
        if (!isActive()) { appear = Math.max(0f, appear - dt * 4f); return; }
        appear = Math.min(1f, appear + dt * 5f);
        Line l = current();
        if (revealed < l.text.length()) {
            revealed = Math.min(l.text.length(), revealed + dt * charsPerSecond * speedMultiplier);
        }
    }

    public void draw(SpriteBatch b, UiKit ui, Texture portraitTex) {
        if (appear <= 0.01f) return;
        Line l = current();
        if (l == null) return;

        float a = MathUtils.clamp(appear, 0f, 1f);
        float boxH = 260f;
        float boxY = 36f;
        float boxX = 90f;
        float boxW = UiKit.W - 180f;

        if (portraitTex != null) {
            float ph = 620f * a;
            float pw = ph * portraitTex.getWidth() / (float) portraitTex.getHeight();
            b.setColor(1f, 1f, 1f, a);
            b.draw(portraitTex, UiKit.W - pw - 40f, boxY + boxH - 60f, pw, ph);
            b.setColor(Color.WHITE);
        }

        ui.drawPanel(b, boxX, boxY, boxW, boxH, a * 0.96f);

        float textX = boxX + 44f;
        float textY = boxY + boxH - 48f;
        if (l.speaker != null) {
            ui.h1.setColor(UiKit.GOLD.r, UiKit.GOLD.g, UiKit.GOLD.b, a);
            ui.layout.setText(ui.h1, l.speaker);
            ui.h1.draw(b, ui.layout, textX, textY);
            ui.h1.setColor(Color.WHITE);
            textY -= 56f;
        }

        String shown = l.text.substring(0, MathUtils.clamp((int) revealed, 0, l.text.length()));
        BitmapFont f = l.speaker == null ? ui.script : ui.body;
        f.setColor(UiKit.CREAM.r, UiKit.CREAM.g, UiKit.CREAM.b, a);
        ui.wrapped(b, f, shown, textX, textY, boxW - 88f - (portraitTex != null ? 260f : 0f));
        f.setColor(Color.WHITE);

        if (isLineComplete()) {
            float blink = 0.45f + 0.55f * MathUtils.sin(appear * 3f + System.nanoTime() / 400000000f);
            ui.small.setColor(UiKit.ROSE.r, UiKit.ROSE.g, UiKit.ROSE.b, a * blink);
            ui.centered(b, ui.small, "toucher pour continuer", UiKit.W / 2f, boxY + 34f);
            ui.small.setColor(Color.WHITE);
        }
    }
}
