package com.esteban.lohen.puzzle;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.JsonValue;

import com.esteban.lohen.ui.UiKit;

/**
 * Chapter 6 — the final lock. Four symbols collected across the whole journey
 * must be dragged into the four slots, ordered by the numbers engraved on them.
 * Real drag-and-drop: pick a token up, move it, drop it on a slot.
 */
public class CodePuzzle extends Puzzle {
    private String[] symbols;     // tokens available (order shuffled deterministically)
    private String[] solution;
    private String[] slots;
    private int dragIndex = -1;   // index into symbols currently held
    private float dragX, dragY;
    private int dragFromSlot = -1;

    private final float slotW = 190f, slotH = 210f, slotGap = 34f;
    private float slotsY = 470f;
    private final float tokenSize = 150f;
    private float tokensY = 215f;

    @Override
    protected void setup() {
        JsonValue sym = cfg.get("symbols");
        symbols = new String[sym.size];
        int i = 0;
        for (JsonValue v = sym.child; v != null; v = v.next) symbols[i++] = v.asString();
        JsonValue sol = cfg.get("solution");
        solution = new String[sol.size];
        i = 0;
        for (JsonValue v = sol.child; v != null; v = v.next) solution[i++] = v.asString();
        slots = new String[solution.length];
        // deterministic non-solved presentation order
        String[] shuffled = new String[symbols.length];
        for (int k = 0; k < symbols.length; k++) shuffled[k] = symbols[(k * 3 + 1) % symbols.length];
        symbols = shuffled;
    }

    private float slotX(int i) {
        float total = slots.length * slotW + (slots.length - 1) * slotGap;
        return (UiKit.W - total) / 2f + i * (slotW + slotGap);
    }

    private float tokenX(int i) {
        int n = symbols.length;
        float total = n * tokenSize + (n - 1) * 56f;
        return (UiKit.W - total) / 2f + i * (tokenSize + 56f);
    }

    private boolean placed(String s) {
        for (String v : slots) if (s.equals(v)) return true;
        return false;
    }

    private Texture icon(String name) { return assets.tex("img/sym_" + name + ".png"); }

    @Override
    public void touchDown(float x, float y) {
        if (solved) return;
        dragIndex = -1;
        dragFromSlot = -1;
        for (int i = 0; i < slots.length; i++) {
            float bx = slotX(i);
            if (slots[i] != null && x >= bx && x <= bx + slotW && y >= slotsY && y <= slotsY + slotH) {
                for (int k = 0; k < symbols.length; k++) {
                    if (symbols[k].equals(slots[i])) { dragIndex = k; break; }
                }
                dragFromSlot = i;
                slots[i] = null;
                dragX = x; dragY = y;
                audio.tap();
                return;
            }
        }
        for (int i = 0; i < symbols.length; i++) {
            if (placed(symbols[i])) continue;
            float bx = tokenX(i);
            if (x >= bx && x <= bx + tokenSize && y >= tokensY && y <= tokensY + tokenSize) {
                dragIndex = i;
                dragX = x; dragY = y;
                audio.tap();
                return;
            }
        }
    }

    @Override
    public void touchDragged(float x, float y) {
        if (dragIndex >= 0) { dragX = x; dragY = y; }
    }

    @Override
    public void touchUp(float x, float y) {
        if (dragIndex < 0) return;
        for (int i = 0; i < slots.length; i++) {
            float bx = slotX(i);
            if (x >= bx - 20 && x <= bx + slotW + 20 && y >= slotsY - 40 && y <= slotsY + slotH + 40) {
                if (slots[i] == null) {
                    slots[i] = symbols[dragIndex];
                    audio.sfx("audio/sfx_tap.ogg", 0.9f, 0.9f);
                    dragIndex = -1;
                    dragFromSlot = -1;
                    checkIfFull();
                    return;
                }
            }
        }
        if (dragFromSlot >= 0) slots[dragFromSlot] = symbols[dragIndex];
        dragIndex = -1;
        dragFromSlot = -1;
    }

    private void checkIfFull() {
        for (String s : slots) if (s == null) return;
        boolean ok = true;
        for (int i = 0; i < slots.length; i++) if (!slots[i].equals(solution[i])) ok = false;
        if (ok) succeed(UiKit.W / 2f, slotsY + slotH / 2f);
        else {
            fail();
            for (int i = 0; i < slots.length; i++) slots[i] = null;
        }
    }

    @Override
    public void draw(SpriteBatch b) {
        float shake = feedback > 0 ? MathUtils.sin(time * 38f) * 10f * feedback : 0f;

        for (int i = 0; i < slots.length; i++) {
            float bx = slotX(i) + shake;
            if (slots[i] != null) ui.drawPanelLight(b, bx, slotsY, slotW, slotH, 1f);
            else ui.drawPanel(b, bx, slotsY, slotW, slotH, 0.9f);
            ui.small.setColor(UiKit.MOON);
            ui.centered(b, ui.small, String.valueOf(i + 1), bx + slotW / 2f, slotsY + 34f);
            ui.small.setColor(Color.WHITE);
            if (slots[i] != null) {
                Texture t = icon(slots[i]);
                float s = 130f * (solved ? 1f + 0.06f * MathUtils.sin(time * 3f) : 1f);
                b.setColor(1f, 1f, 1f, 1f);
                b.draw(t, bx + slotW / 2f - s / 2, slotsY + slotH / 2f - s / 2 + 10f, s, s);
            }
        }

        if (!solved) {
            for (int i = 0; i < symbols.length; i++) {
                if (placed(symbols[i]) || i == dragIndex) continue;
                float bx = tokenX(i);
                b.setColor(1f, 1f, 1f, 0.95f);
                b.draw(icon(symbols[i]), bx, tokensY, tokenSize, tokenSize);
            }
            if (dragIndex >= 0) {
                b.setColor(1f, 1f, 1f, 1f);
                b.draw(icon(symbols[dragIndex]), dragX - tokenSize / 2, dragY - tokenSize / 2, tokenSize, tokenSize);
            }
            ui.small.setColor(UiKit.MOON);
            ui.centered(b, ui.small, "Fais glisser chaque signe dans une case.", UiKit.W / 2f, 150f);
            ui.small.setColor(Color.WHITE);
        }
        b.setColor(Color.WHITE);
    }
}
