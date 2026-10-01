package com.esteban.lohen.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;

import com.esteban.lohen.LohenGame;
import com.esteban.lohen.ui.TouchButton;
import com.esteban.lohen.ui.UiKit;

/** Audio, reading speed and accessibility options. All changes persist. */
public class SettingsScreen extends BaseScreen {
    private final TouchButton back, motionBtn;
    private static final float SLIDER_X = 540f, SLIDER_W = 620f, SLIDER_H = 18f;
    private static final float MUSIC_Y = 640f, SFX_Y = 540f, SPEED_Y = 440f;
    private int dragging = -1;

    public SettingsScreen(LohenGame game) {
        super(game);
        back = new TouchButton("Retour", 40f, UiKit.H - 110f, 190f, 84f);
        motionBtn = new TouchButton("", 540f, 300f, 620f, 92f);
        updateMotionLabel();
    }

    private void updateMotionLabel() {
        motionBtn.label = game.state.reducedMotion ? "Mouvement réduit : activé" : "Mouvement réduit : désactivé";
    }

    @Override
    public void render(float delta) {
        float dt = Math.min(delta, 1 / 20f);
        updateFades(dt);
        clear();
        back.update(dt);
        motionBtn.update(dt);

        begin();
        Texture bg = game.assets.tex("img/bg_title.jpg");
        game.batch.setColor(1, 1, 1, 1);
        game.batch.draw(bg, 0, 0, UiKit.W, UiKit.H);
        game.ui.rect(game.batch, 0, 0, UiKit.W, UiKit.H, UiKit.DEEP, 0.76f);
        game.ui.drawVignette(game.batch, 0.8f);

        game.ui.h1.setColor(UiKit.GOLD);
        game.ui.centered(game.batch, game.ui.h1, "Réglages", UiKit.W / 2f, UiKit.H - 120f);
        game.ui.h1.setColor(Color.WHITE);

        slider("Musique", MUSIC_Y, game.state.musicVolume);
        slider("Effets sonores", SFX_Y, game.state.sfxVolume);
        slider("Vitesse du texte", SPEED_Y, (game.state.textSpeed - 0.5f) / 1.5f);

        motionBtn.draw(game.batch, game.ui);
        game.ui.body.setColor(UiKit.CREAM);
        game.ui.body.draw(game.batch, "Accessibilité", 220f, 360f);
        game.ui.body.setColor(Color.WHITE);

        game.ui.small.setColor(UiKit.MOON);
        game.ui.centered(game.batch, game.ui.small,
                "Le jeu se joue uniquement au toucher : aucun joystick, aucune manette.",
                UiKit.W / 2f, 200f);
        game.ui.small.setColor(Color.WHITE);

        back.draw(game.batch, game.ui);
        drawFades();
        end();
    }

    private void slider(String label, float y, float value) {
        game.ui.body.setColor(UiKit.CREAM);
        game.ui.body.draw(game.batch, label, 220f, y + 18f);
        game.ui.body.setColor(Color.WHITE);
        game.ui.rect(game.batch, SLIDER_X, y, SLIDER_W, SLIDER_H, UiKit.PLUM, 0.9f);
        game.ui.rect(game.batch, SLIDER_X, y, SLIDER_W * MathUtils.clamp(value, 0f, 1f), SLIDER_H, UiKit.GOLD, 1f);
        Texture heart = game.assets.tex("img/heart.png");
        float hx = SLIDER_X + SLIDER_W * MathUtils.clamp(value, 0f, 1f) - 26f;
        game.batch.setColor(1, 1, 1, 1);
        game.batch.draw(heart, hx, y - 18f, 54f, 54f);
        game.batch.setColor(Color.WHITE);
    }

    private int sliderAt(float y) {
        if (Math.abs(y - (MUSIC_Y + SLIDER_H / 2f)) < 56f) return 0;
        if (Math.abs(y - (SFX_Y + SLIDER_H / 2f)) < 56f) return 1;
        if (Math.abs(y - (SPEED_Y + SLIDER_H / 2f)) < 56f) return 2;
        return -1;
    }

    private void apply(int which, float x) {
        float v = MathUtils.clamp((x - SLIDER_X) / SLIDER_W, 0f, 1f);
        if (which == 0) game.state.musicVolume = v;
        else if (which == 1) { game.state.sfxVolume = v; game.audio.tap(); }
        else game.state.textSpeed = 0.5f + v * 1.5f;
    }

    @Override
    protected void onTouchDown(float x, float y) {
        back.touchDown(x, y);
        motionBtn.touchDown(x, y);
        if (x >= SLIDER_X - 40 && x <= SLIDER_X + SLIDER_W + 40) {
            dragging = sliderAt(y);
            if (dragging >= 0) apply(dragging, x);
        }
    }

    @Override
    protected void onTouchDragged(float x, float y) {
        if (dragging >= 0) apply(dragging, x);
    }

    @Override
    protected void onTouchUp(float x, float y) {
        if (dragging >= 0) { dragging = -1; game.state.save(); return; }
        if (back.touchUp(x, y)) {
            game.audio.tap();
            game.state.save();
            fadeTo(new Runnable() { public void run() { game.switchTo(new TitleScreen(game)); } });
            return;
        }
        if (motionBtn.touchUp(x, y)) {
            game.state.reducedMotion = !game.state.reducedMotion;
            game.fx.reducedMotion = game.state.reducedMotion;
            if (game.state.reducedMotion) game.fx.clear();
            updateMotionLabel();
            game.state.save();
            game.audio.tap();
        }
    }

    @Override
    protected void onBack() {
        game.state.save();
        fadeTo(new Runnable() { public void run() { game.switchTo(new TitleScreen(game)); } });
    }
}
