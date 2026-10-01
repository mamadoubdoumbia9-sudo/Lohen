package com.esteban.lohen.puzzle;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.JsonValue;

import com.esteban.lohen.ui.UiKit;

/**
 * Chapter 1 — chronological ordering of four memories.
 * Interaction: tap a card to pick it up, tap another to swap them. The order is
 * checked automatically once every card has been moved at least once, or via
 * the "Vérifier" area. Fully accessible: large targets, no drag required.
 */
public class OrderPuzzle extends Puzzle {
    private String[] items;
    private int[] solution;
    private int[] order;
    private int selected = -1;
    private final float cardX = 250f, cardW = 1100f, cardH = 118f, gap = 18f;
    private float baseY;
    private float checkY, checkH = 96f;

    @Override
    protected void setup() {
        JsonValue arr = cfg.get("items");
        items = new String[arr.size];
        int i = 0;
        for (JsonValue v = arr.child; v != null; v = v.next) items[i++] = v.asString();
        JsonValue sol = cfg.get("solution");
        solution = new int[sol.size];
        i = 0;
        for (JsonValue v = sol.child; v != null; v = v.next) solution[i++] = v.asInt();
        order = new int[items.length];
        // deterministic, never already-solved starting arrangement
        for (int k = 0; k < items.length; k++) order[k] = (k + 2) % items.length;
        baseY = 520f;
        checkY = baseY - items.length * (cardH + gap) - 40f;
    }

    private float cardY(int slot) { return baseY - slot * (cardH + gap); }

    @Override
    public void touchDown(float x, float y) {
        if (solved) return;
        for (int s = 0; s < order.length; s++) {
            float cy = cardY(s);
            if (x >= cardX && x <= cardX + cardW && y >= cy && y <= cy + cardH) {
                if (selected == -1) {
                    selected = s;
                    audio.tap();
                } else if (selected == s) {
                    selected = -1;
                    audio.tap();
                } else {
                    int tmp = order[s];
                    order[s] = order[selected];
                    order[selected] = tmp;
                    selected = -1;
                    audio.sfx("audio/sfx_tap.ogg", 0.8f, 1.2f);
                }
                return;
            }
        }
        if (x >= cardX + cardW / 2f - 180 && x <= cardX + cardW / 2f + 180
                && y >= checkY && y <= checkY + checkH) {
            check();
        }
    }

    private void check() {
        boolean ok = true;
        for (int i = 0; i < order.length; i++) if (order[i] != solution[i]) { ok = false; break; }
        if (ok) succeed(UiKit.W / 2f, baseY - 100f);
        else fail();
    }

    @Override
    public void draw(SpriteBatch b) {
        float shake = feedback > 0 ? MathUtils.sin(time * 42f) * 10f * feedback : 0f;

        for (int s = 0; s < order.length; s++) {
            float cy = cardY(s);
            boolean sel = selected == s;
            float x = cardX + (sel ? MathUtils.sin(time * 5f) * 6f : 0f) + shake;
            if (sel) ui.drawPanelLight(b, x, cy, cardW, cardH, 1f);
            else ui.drawPanel(b, x, cy, cardW, cardH, 0.95f);

            // slot number
            ui.h1.setColor(UiKit.GOLD);
            ui.h1.draw(b, String.valueOf(s + 1), x + 34f, cy + cardH - 28f);
            ui.h1.setColor(Color.WHITE);

            ui.body.setColor(sel ? UiKit.GOLD : UiKit.CREAM);
            ui.wrapped(b, ui.body, items[order[s]], x + 110f, cy + cardH - 26f, cardW - 150f);
            ui.body.setColor(Color.WHITE);
        }

        if (!solved) {
            float bx = cardX + cardW / 2f - 180f;
            ui.drawPanelLight(b, bx, checkY, 360f, checkH, 1f);
            ui.body.setColor(UiKit.GOLD);
            ui.centered(b, ui.body, "Vérifier", cardX + cardW / 2f, checkY + checkH / 2f + 16f);
            ui.body.setColor(Color.WHITE);

            ui.small.setColor(UiKit.MOON);
            ui.centered(b, ui.small,
                    selected == -1 ? "Touche un souvenir, puis un autre pour les échanger."
                            : "Touche un second souvenir pour l'échanger.",
                    UiKit.W / 2f, checkY - 36f);
            ui.small.setColor(Color.WHITE);
        }
    }
}
