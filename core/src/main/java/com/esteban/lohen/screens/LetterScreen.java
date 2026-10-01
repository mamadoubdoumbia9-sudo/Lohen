package com.esteban.lohen.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

import com.esteban.lohen.LohenGame;
import com.esteban.lohen.game.Content;
import com.esteban.lohen.ui.Fx;
import com.esteban.lohen.ui.TouchButton;
import com.esteban.lohen.ui.UiKit;

/**
 * The final scene: Esteban's letter, revealed progressively on a real sheet of
 * paper. Nothing is skippable by accident — a tap only pauses or resumes the
 * reading, and the closing lines are shown exactly as written.
 */
public class LetterScreen extends BaseScreen {
    private enum Stage { OPENING, READING, CLOSING_1, CLOSING_2, SIGNED, END }

    private final Content.Letter letter;
    private final Array<String> page = new Array<String>();
    private int nextParagraph;
    private float revealed;             // characters revealed in the current paragraph
    private int revealingIndex = -1;    // index in `page` currently typing
    private Stage stage = Stage.OPENING;
    private boolean paused;
    private float t, stageTime, pageAlpha = 1f, turning;
    private float closingAlpha1, closingAlpha2, signAlpha, endAlpha;

    private final TouchButton pauseBtn, menuBtn, replayBtn;

    private static final float PAPER_W = 980f, PAPER_H = 820f;
    private static final float PAPER_X = (UiKit.W - PAPER_W) / 2f, PAPER_Y = 50f;
    private static final float TEXT_W = PAPER_W - 180f;
    private static final float TEXT_TOP = PAPER_Y + PAPER_H - 120f;
    private static final float TEXT_BOTTOM = PAPER_Y + 110f;
    private static final float CPS = 34f;   // deliberately calm reading pace

    public LetterScreen(LohenGame game) {
        super(game);
        letter = game.content.letter;
        game.audio.playMusic("audio/music_letter.ogg");
        game.state.letterUnlocked = true;
        game.state.save();
        pauseBtn = new TouchButton("Pause", UiKit.W - 250f, UiKit.H - 110f, 210f, 84f);
        menuBtn = new TouchButton("Menu", 40f, UiKit.H - 110f, 170f, 84f);
        replayBtn = new TouchButton("Relire la lettre", UiKit.W / 2f - 230f, 150f, 460f, 100f);
        replayBtn.primary = true;
        replayBtn.visible = false;
    }

    @Override
    public void render(float delta) {
        float dt = Math.min(delta, 1 / 20f);
        t += dt;
        stageTime += dt;
        updateFades(dt);
        clear();

        pauseBtn.update(dt);
        menuBtn.update(dt);
        replayBtn.update(dt);
        if (!game.state.reducedMotion) {
            game.fx.ambient(Fx.Kind.DUST, dt, 1.2f, UiKit.W, UiKit.H);
            if (stage.ordinal() >= Stage.CLOSING_1.ordinal())
                game.fx.ambient(Fx.Kind.PETAL, dt, 1.1f, UiKit.W, UiKit.H);
        }
        game.fx.update(dt, UiKit.W, UiKit.H);

        if (!paused) advance(dt);

        begin();
        drawBackground();
        drawPaper();
        game.fx.draw(game.batch);
        game.ui.drawVignette(game.batch, 0.85f);
        if (stage != Stage.END) {
            pauseBtn.label = paused ? "Reprendre" : "Pause";
            pauseBtn.draw(game.batch, game.ui);
        }
        menuBtn.draw(game.batch, game.ui);
        replayBtn.draw(game.batch, game.ui);
        drawFades();
        end();
    }

    // ------------------------------------------------------------ progression
    private void advance(float dt) {
        switch (stage) {
            case OPENING:
                if (stageTime > 2.2f) { stage = Stage.READING; stageTime = 0; pushNextParagraph(); }
                break;
            case READING:
                if (turning > 0) {
                    turning -= dt;
                    pageAlpha = turning > 0.35f ? Math.max(0f, (turning - 0.35f) / 0.35f)
                            : Math.min(1f, 1f - turning / 0.35f);
                    if (turning <= 0) pageAlpha = 1f;
                    return;
                }
                if (revealingIndex >= 0 && revealingIndex < page.size) {
                    String s = page.get(revealingIndex);
                    if (revealed < s.length()) {
                        revealed = Math.min(s.length(), revealed + dt * CPS * game.state.textSpeed);
                        return;
                    }
                    if (stageTime > 1.1f) pushNextParagraph();
                }
                break;
            case CLOSING_1:
                closingAlpha1 = Math.min(1f, closingAlpha1 + dt * 0.5f);
                if (stageTime > 6.5f) { stage = Stage.CLOSING_2; stageTime = 0; game.audio.sparkle(); }
                break;
            case CLOSING_2:
                closingAlpha2 = Math.min(1f, closingAlpha2 + dt * 0.5f);
                if (stageTime > 6.0f) { stage = Stage.SIGNED; stageTime = 0; }
                break;
            case SIGNED:
                signAlpha = Math.min(1f, signAlpha + dt * 0.6f);
                if (stageTime > 4.5f) {
                    stage = Stage.END;
                    stageTime = 0;
                    replayBtn.visible = true;
                    game.state.finished = true;
                    game.state.save();
                }
                break;
            default:
                endAlpha = Math.min(1f, endAlpha + dt * 0.8f);
        }
    }

    private void pushNextParagraph() {
        stageTime = 0;
        if (nextParagraph >= letter.paragraphs.size) {
            stage = Stage.CLOSING_1;
            stageTime = 0;
            page.clear();
            game.audio.page();
            game.fx.burst(Fx.Kind.SPARK, UiKit.W / 2f, UiKit.H / 2f, 26);
            return;
        }
        String next = letter.paragraphs.get(nextParagraph);
        if (!fits(next)) {
            page.clear();
            turning = 0.7f;
            game.audio.page();
        }
        page.add(next);
        revealingIndex = page.size - 1;
        revealed = 0f;
        nextParagraph++;
    }

    private boolean fits(String candidate) {
        float h = 0;
        for (String s : page) h += game.ui.measureWrapped(game.ui.body, s, TEXT_W) + 28f;
        h += game.ui.measureWrapped(game.ui.body, candidate, TEXT_W) + 28f;
        return h < (TEXT_TOP - TEXT_BOTTOM);
    }

    // ---------------------------------------------------------------- drawing
    private void drawBackground() {
        Texture bg = game.assets.tex("img/bg_ch6_phare.jpg");
        float k = 1.05f + 0.01f * MathUtils.sin(t * 0.18f);
        float w = UiKit.W * k, h = UiKit.H * k;
        game.batch.setColor(1, 1, 1, 1);
        game.batch.draw(bg, (UiKit.W - w) / 2f, (UiKit.H - h) / 2f, w, h);
        game.ui.rect(game.batch, 0, 0, UiKit.W, UiKit.H, UiKit.DEEP, 0.55f);
    }

    private void drawPaper() {
        Texture paper = game.assets.tex("img/paper_letter.jpg");
        float open = stage == Stage.OPENING
                ? Interpolation.swingOut.apply(MathUtils.clamp(stageTime / 1.6f, 0f, 1f)) : 1f;
        float pw = PAPER_W * (0.82f + 0.18f * open);
        float ph = PAPER_H * (0.82f + 0.18f * open);
        float px = UiKit.W / 2f - pw / 2f;
        float py = PAPER_Y + (PAPER_H - ph) / 2f;
        game.batch.setColor(1f, 1f, 1f, 0.97f * open);
        game.batch.draw(paper, px, py, pw, ph);
        game.batch.setColor(Color.WHITE);

        if (stage == Stage.OPENING) {
            game.ui.script.setColor(0.25f, 0.16f, 0.2f, open);
            game.ui.centered(game.batch, game.ui.script, letter.title, UiKit.W / 2f, UiKit.H / 2f + 40f);
            game.ui.script.setColor(Color.WHITE);
            return;
        }

        Color inkCol = new Color(0.22f, 0.13f, 0.17f, 1f);

        if (stage == Stage.READING) {
            float y = TEXT_TOP;
            for (int i = 0; i < page.size; i++) {
                String s = page.get(i);
                String shown = i == revealingIndex
                        ? s.substring(0, MathUtils.clamp((int) revealed, 0, s.length())) : s;
                game.ui.body.setColor(inkCol.r, inkCol.g, inkCol.b, pageAlpha);
                float h = game.ui.wrapped(game.batch, game.ui.body, shown, PAPER_X + 90f, y, TEXT_W);
                game.ui.body.setColor(Color.WHITE);
                y -= game.ui.measureWrapped(game.ui.body, s, TEXT_W) + 28f;
            }
            if (paused) {
                game.ui.small.setColor(UiKit.CREAM);
                game.ui.centered(game.batch, game.ui.small, "lecture en pause — touche pour reprendre",
                        UiKit.W / 2f, PAPER_Y - 10f);
                game.ui.small.setColor(Color.WHITE);
            }
            return;
        }

        // closing lines — locked wording, centred, large, calm
        Texture heart = game.assets.tex("img/heart.png");
        if (stage.ordinal() >= Stage.CLOSING_1.ordinal()) {
            float a = closingAlpha1;
            String l1 = letter.closing.get(0);
            game.ui.title.setColor(inkCol.r, inkCol.g, inkCol.b, a);
            float wText = game.ui.textWidth(game.ui.title, l1);
            float hs = 84f + 5f * MathUtils.sin(t * 2.0f);
            float totalW = wText + 24f + hs;
            float sx = UiKit.W / 2f - totalW / 2f;
            game.ui.title.draw(game.batch, l1, sx, UiKit.H / 2f + 150f);
            game.ui.title.setColor(Color.WHITE);
            game.batch.setColor(1f, 1f, 1f, a);
            game.batch.draw(heart, sx + wText + 24f, UiKit.H / 2f + 150f - hs + 6f, hs, hs);
            game.batch.setColor(Color.WHITE);
        }
        if (stage.ordinal() >= Stage.CLOSING_2.ordinal()) {
            float a = closingAlpha2;
            game.ui.h1.setColor(inkCol.r, inkCol.g, inkCol.b, a);
            game.ui.wrappedCentered(game.batch, game.ui.h1, letter.closing.get(1),
                    UiKit.W / 2f, UiKit.H / 2f + 20f, TEXT_W);
            game.ui.h1.setColor(Color.WHITE);
        }
        if (stage.ordinal() >= Stage.SIGNED.ordinal()) {
            game.ui.script.setColor(inkCol.r, inkCol.g, inkCol.b, signAlpha);
            game.ui.centered(game.batch, game.ui.script, letter.signature,
                    UiKit.W / 2f + 240f, PAPER_Y + 180f);
            game.ui.script.setColor(Color.WHITE);
        }
        if (stage == Stage.END) {
            game.ui.small.setColor(UiKit.CREAM.r, UiKit.CREAM.g, UiKit.CREAM.b, endAlpha * 0.9f);
            game.ui.centered(game.batch, game.ui.small,
                    "fin · merci d'avoir fait tout ce chemin", UiKit.W / 2f, PAPER_Y - 10f);
            game.ui.small.setColor(Color.WHITE);
        }
    }

    // ----------------------------------------------------------------- input
    @Override
    protected void onTouchDown(float x, float y) {
        pauseBtn.touchDown(x, y);
        menuBtn.touchDown(x, y);
        replayBtn.touchDown(x, y);
    }

    @Override
    protected void onTouchUp(float x, float y) {
        if (menuBtn.touchUp(x, y)) {
            game.audio.tap();
            fadeTo(new Runnable() { public void run() { game.switchTo(new TitleScreen(game)); } });
            return;
        }
        if (replayBtn.visible && replayBtn.touchUp(x, y)) {
            game.audio.page();
            restart();
            return;
        }
        if (pauseBtn.touchUp(x, y)) { paused = !paused; game.audio.tap(); return; }
        // a tap on the letter itself only pauses/resumes: the text is never skipped
        if (stage == Stage.READING) paused = !paused;
    }

    private void restart() {
        page.clear();
        nextParagraph = 0;
        revealed = 0;
        revealingIndex = -1;
        stage = Stage.OPENING;
        stageTime = 0;
        closingAlpha1 = closingAlpha2 = signAlpha = endAlpha = 0;
        paused = false;
        replayBtn.visible = false;
    }

    @Override
    protected void onBack() {
        fadeTo(new Runnable() { public void run() { game.switchTo(new TitleScreen(game)); } });
    }
}
