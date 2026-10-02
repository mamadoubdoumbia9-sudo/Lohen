package com.esteban.lohen.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

import com.esteban.lohen.LohenGame;
import com.esteban.lohen.ui.Fx;
import com.esteban.lohen.ui.TouchButton;
import com.esteban.lohen.ui.UiKit;

/** Title screen: continue / new journey / chapters / settings. */
public class TitleScreen extends BaseScreen {
    private final Array<TouchButton> buttons = new Array<TouchButton>();
    private TouchButton continueBtn, newBtn, chaptersBtn, settingsBtn;
    private float t;
    private boolean confirmingNew;

    public TitleScreen(LohenGame game) {
        super(game);
        float bw = 460f, bh = 104f, x = 120f;
        float y = 430f;
        continueBtn = new TouchButton("Continuer", x, y, bw, bh);
        continueBtn.primary = true;
        newBtn = new TouchButton("Commencer le voyage", x, y - 124f, bw, bh);
        chaptersBtn = new TouchButton("Chapitres", x, y - 248f, bw / 2f - 10f, bh);
        settingsBtn = new TouchButton("Réglages", x + bw / 2f + 10f, y - 248f, bw / 2f - 10f, bh);
        buttons.addAll(continueBtn, newBtn, chaptersBtn, settingsBtn);
        continueBtn.visible = game.state.hasSave();
        if (!continueBtn.visible) {
            newBtn.primary = true;
            newBtn.bounds.y = y;
            chaptersBtn.bounds.y = y - 124f;
            settingsBtn.bounds.y = y - 124f;
        }
        chaptersBtn.enabled = game.state.chapter > 1;
    }

    @Override
    public void render(float delta) {
        float dt = Math.min(delta, 1 / 20f);
        t += dt;
        updateFades(dt);
        clear();

        game.fx.ambient(Fx.Kind.FIREFLY, dt, 1.6f, UiKit.W, UiKit.H);
        game.fx.ambient(Fx.Kind.PETAL, dt, 0.5f, UiKit.W, UiKit.H);
        game.fx.update(dt, UiKit.W, UiKit.H);
        for (TouchButton b : buttons) b.update(dt);

        begin();
        Texture bg = game.assets.tex("img/bg_title.jpg");
        float k = 1.04f + 0.012f * MathUtils.sin(t * 0.25f);   // slow breathing ken-burns
        float w = UiKit.W * k, h = UiKit.H * k;
        game.batch.setColor(1, 1, 1, 1);
        game.batch.draw(bg, (UiKit.W - w) / 2f, (UiKit.H - h) / 2f, w, h);
        game.fx.draw(game.batch);
        game.ui.drawVignette(game.batch, 0.9f);

        // title block
        game.ui.rect(game.batch, 90f, 600f, 740f, 4f, UiKit.GOLD, 0.55f);
        game.ui.title.setColor(UiKit.CREAM);
        game.ui.title.draw(game.batch, "Pour Lohen", 118f, 760f);
        game.ui.title.setColor(Color.WHITE);
        game.ui.script.setColor(UiKit.ROSE);
        game.ui.script.draw(game.batch, "un cadeau d'Esteban, en six endroits", 124f, 660f);
        game.ui.script.setColor(Color.WHITE);

        Texture heart = game.assets.tex("img/heart.png");
        float hs = 86f + 6f * MathUtils.sin(t * 2.2f);
        game.batch.setColor(1f, 1f, 1f, 0.95f);
        game.batch.draw(heart, 860f, 690f, hs, hs);
        game.batch.setColor(Color.WHITE);

        for (TouchButton b : buttons) b.draw(game.batch, game.ui);

        if (confirmingNew) {
            game.ui.rect(game.batch, 0, 0, UiKit.W, UiKit.H, UiKit.DEEP, 0.72f);
            game.ui.drawPanel(game.batch, 340f, 300f, 920f, 320f, 1f);
            game.ui.h1.setColor(UiKit.GOLD);
            game.ui.centered(game.batch, game.ui.h1, "Recommencer depuis le début ?", UiKit.W / 2f, 560f);
            game.ui.h1.setColor(Color.WHITE);
            game.ui.body.setColor(UiKit.CREAM);
            game.ui.wrappedCentered(game.batch, game.ui.body,
                    "Ta progression actuelle sera effacée.", UiKit.W / 2f, 490f, 800f);
            game.ui.body.setColor(Color.WHITE);
            game.ui.drawPanelLight(game.batch, 420f, 330f, 340f, 96f, 1f);
            game.ui.drawPanel(game.batch, 840f, 330f, 340f, 96f, 1f);
            game.ui.body.setColor(UiKit.GOLD);
            game.ui.centered(game.batch, game.ui.body, "Oui, recommencer", 590f, 394f);
            game.ui.body.setColor(UiKit.CREAM);
            game.ui.centered(game.batch, game.ui.body, "Annuler", 1010f, 394f);
            game.ui.body.setColor(Color.WHITE);
        }

        game.ui.small.setColor(UiKit.MOON.r, UiKit.MOON.g, UiKit.MOON.b, 0.75f);
        game.ui.centered(game.batch, game.ui.small,
                "jeu tactile · aucun joystick · touche, glisse, découvre", UiKit.W / 2f, 60f);
        game.ui.small.setColor(Color.WHITE);

        drawFades();
        end();
    }

    @Override
    protected void onTouchDown(float x, float y) {
        if (confirmingNew) return;
        for (TouchButton b : buttons) b.touchDown(x, y);
    }

    @Override
    protected void onTouchUp(float x, float y) {
        if (confirmingNew) {
            if (y >= 330f && y <= 426f) {
                if (x >= 420f && x <= 760f) {
                    game.audio.tap();
                    game.state.reset();
                    confirmingNew = false;
                    startChapter(1);
                    return;
                }
                if (x >= 840f && x <= 1180f) {
                    game.audio.tap();
                    confirmingNew = false;
                    return;
                }
            }
            return;
        }
        if (continueBtn.touchUp(x, y)) {
            game.audio.tap();
            startChapter(game.state.finished ? 6 : game.state.currentChapter);
        } else if (newBtn.touchUp(x, y)) {
            game.audio.tap();
            if (game.state.hasSave()) confirmingNew = true;
            else { game.state.reset(); startChapter(1); }
        } else if (chaptersBtn.touchUp(x, y)) {
            game.audio.tap();
            fadeTo(new Runnable() { public void run() { game.switchTo(new ChapterSelectScreen(game)); } });
        } else if (settingsBtn.touchUp(x, y)) {
            game.audio.tap();
            fadeTo(new Runnable() { public void run() { game.switchTo(new SettingsScreen(game)); } });
        }
    }

    private void startChapter(final int index) {
        fadeTo(new Runnable() {
            public void run() {
                game.fx.clear();
                if (index == 1) {
                    game.switchTo(new CutsceneScreen(game, "intro", new Runnable() {
                        public void run() { game.switchTo(new ChapterScreen(game, index)); }
                    }));
                } else {
                    game.switchTo(new ChapterScreen(game, index));
                }
            }
        });
    }

    @Override
    protected void onBack() {
        if (confirmingNew) confirmingNew = false;
    }
}
