package com.esteban.lohen.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Array;

import com.esteban.lohen.LohenGame;
import com.esteban.lohen.game.Content;
import com.esteban.lohen.ui.TouchButton;
import com.esteban.lohen.ui.UiKit;

/** Replay any place already discovered, plus direct access to the letter. */
public class ChapterSelectScreen extends BaseScreen {
    private final Array<TouchButton> cards = new Array<TouchButton>();
    private final TouchButton back, letterBtn;

    public ChapterSelectScreen(LohenGame game) {
        super(game);
        float cw = 440f, ch = 150f;
        for (int i = 0; i < game.content.chapters.size; i++) {
            Content.Chapter c = game.content.chapters.get(i);
            int col = i % 3, row = i / 3;
            TouchButton b = new TouchButton(c.index + ". " + c.title,
                    140f + col * (cw + 40f), 520f - row * (ch + 36f), cw, ch);
            b.enabled = c.index <= game.state.chapter;
            cards.add(b);
        }
        back = new TouchButton("Retour", 40f, UiKit.H - 110f, 190f, 84f);
        letterBtn = new TouchButton("Relire la lettre", UiKit.W / 2f - 230f, 140f, 460f, 96f);
        letterBtn.primary = true;
        letterBtn.visible = game.state.letterUnlocked;
    }

    @Override
    public void render(float delta) {
        float dt = Math.min(delta, 1 / 20f);
        updateFades(dt);
        clear();
        for (TouchButton b : cards) b.update(dt);
        back.update(dt);
        letterBtn.update(dt);

        begin();
        Texture bg = game.assets.tex("img/bg_title.jpg");
        game.batch.setColor(1, 1, 1, 1);
        game.batch.draw(bg, 0, 0, UiKit.W, UiKit.H);
        game.ui.rect(game.batch, 0, 0, UiKit.W, UiKit.H, UiKit.DEEP, 0.7f);
        game.ui.drawVignette(game.batch, 0.8f);

        game.ui.h1.setColor(UiKit.GOLD);
        game.ui.centered(game.batch, game.ui.h1, "Les six endroits", UiKit.W / 2f, UiKit.H - 120f);
        game.ui.h1.setColor(Color.WHITE);

        for (int i = 0; i < cards.size; i++) {
            TouchButton b = cards.get(i);
            b.draw(game.batch, game.ui);
            Content.Chapter c = game.content.chapters.get(i);
            game.ui.small.setColor(b.enabled ? UiKit.ROSE : UiKit.MOON);
            game.ui.centered(game.batch, game.ui.small,
                    b.enabled ? c.subtitle : "pas encore découvert",
                    b.bounds.x + b.bounds.width / 2f, b.bounds.y + 34f);
            game.ui.small.setColor(Color.WHITE);
        }

        back.draw(game.batch, game.ui);
        letterBtn.draw(game.batch, game.ui);
        drawFades();
        end();
    }

    @Override
    protected void onTouchDown(float x, float y) {
        for (TouchButton b : cards) b.touchDown(x, y);
        back.touchDown(x, y);
        letterBtn.touchDown(x, y);
    }

    @Override
    protected void onTouchUp(float x, float y) {
        if (back.touchUp(x, y)) {
            game.audio.tap();
            fadeTo(new Runnable() { public void run() { game.switchTo(new TitleScreen(game)); } });
            return;
        }
        if (letterBtn.visible && letterBtn.touchUp(x, y)) {
            game.audio.tap();
            fadeTo(new Runnable() { public void run() { game.switchTo(new LetterScreen(game)); } });
            return;
        }
        for (int i = 0; i < cards.size; i++) {
            if (cards.get(i).touchUp(x, y)) {
                final int index = game.content.chapters.get(i).index;
                game.audio.tap();
                fadeTo(new Runnable() {
                    public void run() { game.fx.clear(); game.switchTo(new ChapterScreen(game, index)); }
                });
                return;
            }
        }
    }

    @Override
    protected void onBack() {
        fadeTo(new Runnable() { public void run() { game.switchTo(new TitleScreen(game)); } });
    }
}
