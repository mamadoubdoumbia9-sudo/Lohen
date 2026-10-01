package com.esteban.lohen.puzzle;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;

import com.esteban.lohen.ui.UiKit;

/**
 * Chapter 3 — a written riddle (devinette) answered by composing a word from
 * letter tiles. Tap a tile to add a letter, tap a filled slot to remove it.
 * The answer is validated as soon as every slot is filled.
 */
public class RiddlePuzzle extends Puzzle {
    private String answer;
    private char[] letters;
    private char[] typed;
    private boolean[] used;
    private int[] fromTile;
    private int filled;

    private final float slotW = 96f, slotH = 118f, slotGap = 16f;
    private float slotsY = 420f;
    private final float tileW = 110f, tileH = 110f, tileGap = 16f;
    private float tilesY = 220f;

    @Override
    protected void setup() {
        answer = cfg.getString("answer").toUpperCase();
        String pool = cfg.getString("letters").toUpperCase();
        letters = pool.toCharArray();
        used = new boolean[letters.length];
        typed = new char[answer.length()];
        fromTile = new int[answer.length()];
        for (int i = 0; i < typed.length; i++) { typed[i] = 0; fromTile[i] = -1; }
    }

    private float slotsStartX() {
        return (UiKit.W - (answer.length() * slotW + (answer.length() - 1) * slotGap)) / 2f;
    }

    private int tilesPerRow() { return Math.min(letters.length, 6); }

    private float tileX(int i) {
        int per = tilesPerRow();
        int col = i % per;
        int rows = (letters.length + per - 1) / per;
        int rowCount = Math.min(per, letters.length - (i / per) * per);
        float rowW = rowCount * tileW + (rowCount - 1) * tileGap;
        return (UiKit.W - rowW) / 2f + col * (tileW + tileGap);
    }

    private float tileY(int i) {
        int per = tilesPerRow();
        int row = i / per;
        return tilesY - row * (tileH + tileGap);
    }

    @Override
    public void touchDown(float x, float y) {
        if (solved) return;
        float sx = slotsStartX();
        for (int i = 0; i < answer.length(); i++) {
            float bx = sx + i * (slotW + slotGap);
            if (x >= bx && x <= bx + slotW && y >= slotsY && y <= slotsY + slotH) {
                if (typed[i] != 0) {
                    used[fromTile[i]] = false;
                    typed[i] = 0;
                    fromTile[i] = -1;
                    filled--;
                    audio.tap();
                }
                return;
            }
        }
        for (int i = 0; i < letters.length; i++) {
            if (used[i]) continue;
            float bx = tileX(i), by = tileY(i);
            if (x >= bx && x <= bx + tileW && y >= by && y <= by + tileH) {
                for (int s = 0; s < typed.length; s++) {
                    if (typed[s] == 0) {
                        typed[s] = letters[i];
                        fromTile[s] = i;
                        used[i] = true;
                        filled++;
                        audio.sfx("audio/sfx_tap.ogg", 0.7f, 1.1f + s * 0.04f);
                        if (filled == typed.length) check();
                        return;
                    }
                }
                return;
            }
        }
    }

    private void check() {
        if (new String(typed).equals(answer)) {
            succeed(UiKit.W / 2f, slotsY + slotH);
        } else {
            fail();
            // gently give the letters back
            for (int i = 0; i < typed.length; i++) {
                if (fromTile[i] >= 0) used[fromTile[i]] = false;
                typed[i] = 0;
                fromTile[i] = -1;
            }
            filled = 0;
        }
    }

    @Override
    public void draw(SpriteBatch b) {
        float shake = feedback > 0 ? MathUtils.sin(time * 40f) * 9f * feedback : 0f;
        float sx = slotsStartX();
        for (int i = 0; i < answer.length(); i++) {
            float bx = sx + i * (slotW + slotGap) + shake;
            boolean full = typed[i] != 0;
            if (full) ui.drawPanelLight(b, bx, slotsY, slotW, slotH, 1f);
            else ui.drawPanel(b, bx, slotsY, slotW, slotH, 0.85f);
            if (full) {
                ui.h1.setColor(solved ? UiKit.GOLD : UiKit.CREAM);
                ui.centered(b, ui.h1, String.valueOf(typed[i]), bx + slotW / 2f, slotsY + slotH / 2f + 22f);
                ui.h1.setColor(Color.WHITE);
            } else {
                ui.rect(b, bx + 22f, slotsY + 20f, slotW - 44f, 4f, UiKit.ROSE, 0.6f);
            }
        }

        if (solved) return;

        for (int i = 0; i < letters.length; i++) {
            if (used[i]) continue;
            float bx = tileX(i), by = tileY(i);
            ui.drawPanel(b, bx, by, tileW, tileH, 0.92f);
            ui.h1.setColor(UiKit.CREAM);
            ui.centered(b, ui.h1, String.valueOf(letters[i]), bx + tileW / 2f, by + tileH / 2f + 22f);
            ui.h1.setColor(Color.WHITE);
        }

        ui.small.setColor(UiKit.MOON);
        ui.centered(b, ui.small, "Touche les lettres pour composer la réponse · touche une case pour l'effacer",
                UiKit.W / 2f, 120f);
        ui.small.setColor(Color.WHITE);
    }
}
