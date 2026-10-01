package com.esteban.lohen.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Rectangle;

/**
 * Contextual touch button. Minimum touch target is enforced at 96x96 virtual
 * units (~9mm on a phone) as required by the UX rules. No joystick, no sticks:
 * every interaction in the game is a tap, a long press or a drag.
 */
public class TouchButton {
    public final Rectangle bounds = new Rectangle();
    public String label;
    public boolean enabled = true;
    public boolean visible = true;
    public boolean primary = false;
    private float press = 0f;
    private boolean down = false;

    public TouchButton(String label, float x, float y, float w, float h) {
        this.label = label;
        bounds.set(x, y, Math.max(96f, w), Math.max(72f, h));
    }

    public boolean hit(float x, float y) {
        return visible && enabled && bounds.contains(x, y);
    }

    public void touchDown(float x, float y) {
        if (hit(x, y)) down = true;
    }

    /** @return true when the button was actually activated by this release. */
    public boolean touchUp(float x, float y) {
        boolean fired = down && hit(x, y);
        down = false;
        return fired;
    }

    public void update(float dt) {
        float target = down ? 1f : 0f;
        press += (target - press) * Math.min(1f, dt * 14f);
    }

    public void draw(SpriteBatch b, UiKit ui) {
        if (!visible) return;
        float sq = Interpolation.smooth.apply(press);
        float inset = 4f * sq;
        float a = enabled ? 1f : 0.45f;
        if (primary) {
            ui.drawPanelLight(b, bounds.x + inset, bounds.y + inset,
                    bounds.width - 2 * inset, bounds.height - 2 * inset, a);
        } else {
            ui.drawPanel(b, bounds.x + inset, bounds.y + inset,
                    bounds.width - 2 * inset, bounds.height - 2 * inset, a);
        }
        BitmapFont f = ui.body;
        Color c = primary ? UiKit.GOLD : UiKit.CREAM;
        f.setColor(c.r, c.g, c.b, a);
        ui.layout.setText(f, label);
        f.draw(b, ui.layout,
                bounds.x + (bounds.width - ui.layout.width) / 2f,
                bounds.y + (bounds.height + ui.layout.height) / 2f);
        f.setColor(Color.WHITE);
    }
}
