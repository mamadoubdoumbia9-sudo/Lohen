package com.esteban.lohen.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

import com.esteban.lohen.LohenGame;
import com.esteban.lohen.game.Content;
import com.esteban.lohen.puzzle.Puzzle;
import com.esteban.lohen.ui.Dialogue;
import com.esteban.lohen.ui.Fx;
import com.esteban.lohen.ui.TouchButton;
import com.esteban.lohen.ui.UiKit;

/**
 * The exploration screen: one illustrated place, interactive hotspots, a
 * narrative layer and one puzzle. Everything is driven by chapters.json.
 */
public class ChapterScreen extends BaseScreen {
    private enum Phase { INTRO, EXPLORE, PUZZLE, OUTRO, REWARD }

    private final Content.Chapter chapter;
    private final Dialogue dialogue = new Dialogue();
    private Puzzle puzzle;
    private Phase phase = Phase.INTRO;

    private final TouchButton solveBtn, journalBtn, hintBtn, backBtn, continueBtn;
    private final Array<TouchButton> hud = new Array<TouchButton>();

    private float t;
    private float titleCard = 3.2f;
    private boolean journalOpen;
    private boolean hintOpen;
    private int visitedCount;
    private float solvedTimer = -1f;
    private float rewardAnim;
    private Fx.Kind ambientKind = Fx.Kind.DUST;
    private Content.Hotspot pending;

    public ChapterScreen(LohenGame game, int chapterIndex) {
        super(game);
        this.chapter = game.content.chapter(chapterIndex);
        game.state.unlockChapter(chapterIndex);
        Gdx.app.log("Milestone", "chapter-enter " + chapterIndex);

        solveBtn = new TouchButton("", UiKit.W / 2f - 320f, 150f, 640f, 104f);
        solveBtn.primary = true;
        solveBtn.visible = false;
        solveBtn.label = chapter.puzzlePrompt;

        journalBtn = new TouchButton("Journal", UiKit.W - 250f, UiKit.H - 110f, 210f, 84f);
        hintBtn = new TouchButton("Indice", UiKit.W - 480f, UiKit.H - 110f, 200f, 84f);
        backBtn = new TouchButton("Menu", 40f, UiKit.H - 110f, 170f, 84f);
        continueBtn = new TouchButton("Continuer", UiKit.W / 2f - 220f, 120f, 440f, 104f);
        continueBtn.primary = true;
        continueBtn.visible = false;
        hud.addAll(journalBtn, hintBtn, backBtn);

        try { ambientKind = Fx.Kind.valueOf(chapter.ambient); } catch (Exception e) { ambientKind = Fx.Kind.DUST; }

        for (Content.Dialog d : chapter.intro) dialogue.push(d.speaker, d.text, d.portrait);
        dialogue.speedMultiplier = game.state.textSpeed;
        game.audio.playChapterMusic(chapter.index, chapter.music);
        game.state.currentChapter = chapter.index;
        game.state.save();
        Gdx.app.log("Chapter", "entered " + chapter.id);
    }

    // ------------------------------------------------------------------ loop
    @Override
    public void render(float delta) {
        float dt = Math.min(delta, 1 / 20f);
        t += dt;
        if (titleCard > 0) titleCard -= dt;
        updateFades(dt);
        clear();

        dialogue.speedMultiplier = game.state.textSpeed;
        dialogue.update(dt);
        for (TouchButton b : hud) b.update(dt);
        solveBtn.update(dt);
        continueBtn.update(dt);

        if (!game.state.reducedMotion) {
            if (ambientKind == Fx.Kind.SPARK) game.fx.ambient(Fx.Kind.DUST, dt, 2.4f, UiKit.W, UiKit.H);
            else game.fx.ambient(ambientKind, dt, ambientKind == Fx.Kind.FIREFLY ? 2.2f : 1.4f, UiKit.W, UiKit.H);
        }
        game.fx.update(dt, UiKit.W, UiKit.H);

        if (phase == Phase.PUZZLE && puzzle != null) {
            puzzle.update(dt);
            if (puzzle.isSolved() && solvedTimer < 0) solvedTimer = 1.6f;
        }
        if (solvedTimer > 0) {
            solvedTimer -= dt;
            if (solvedTimer <= 0) {
                solvedTimer = -1f;
                enterOutro();
            }
        }
        if (phase == Phase.REWARD) rewardAnim = Math.min(1f, rewardAnim + dt * 1.6f);

        begin();
        drawScene(dt);
        drawFades();
        end();
    }

    private void drawScene(float dt) {
        Texture bg = game.assets.tex(chapter.background);
        float k = 1.03f + 0.01f * MathUtils.sin(t * 0.2f);
        float w = UiKit.W * k, h = UiKit.H * k;
        game.batch.setColor(1, 1, 1, 1);
        game.batch.draw(bg, (UiKit.W - w) / 2f, (UiKit.H - h) / 2f, w, h);

        float dim = 0f;
        if (phase == Phase.PUZZLE && puzzle != null) dim = puzzle.dim();
        else if (phase == Phase.INTRO || phase == Phase.OUTRO) dim = 0.22f;
        if (dim > 0) game.ui.rect(game.batch, 0, 0, UiKit.W, UiKit.H, UiKit.DEEP, dim);

        if (phase == Phase.EXPLORE) drawHotspots();

        game.fx.draw(game.batch);
        game.ui.drawVignette(game.batch, 0.78f);

        if (phase == Phase.PUZZLE && puzzle != null) {
            drawPuzzleFrame();
            puzzle.draw(game.batch);
        }

        // HUD
        if (phase == Phase.EXPLORE || phase == Phase.PUZZLE) {
            for (TouchButton b : hud) b.draw(game.batch, game.ui);
            drawProgress();
        }
        solveBtn.draw(game.batch, game.ui);
        continueBtn.draw(game.batch, game.ui);

        if (dialogue.isActive()) {
            Texture portrait = null;
            Dialogue.Line l = dialogue.current();
            if (l != null && l.portrait != null) portrait = game.assets.tex(l.portrait);
            dialogue.draw(game.batch, game.ui, portrait);
        }

        if (titleCard > 0) drawTitleCard();
        if (journalOpen) drawJournal();
        if (hintOpen) drawHint();
        if (phase == Phase.REWARD) drawReward();
    }

    private void drawHotspots() {
        Texture glow = game.assets.tex("img/glow.png");
        int srcF = game.batch.getBlendSrcFunc(), dstF = game.batch.getBlendDstFunc();
        game.batch.setBlendFunction(com.badlogic.gdx.graphics.GL20.GL_SRC_ALPHA, com.badlogic.gdx.graphics.GL20.GL_ONE);
        for (int i = 0; i < chapter.hotspots.size; i++) {
            Content.Hotspot hs = chapter.hotspots.get(i);
            float x = hs.x * UiKit.W, y = (1f - hs.y) * UiKit.H;
            float pulse = 0.5f + 0.5f * MathUtils.sin(t * 1.9f + i * 1.3f);
            float a = hs.visited ? 0.12f : 0.26f + 0.2f * pulse;
            float s = (hs.visited ? 120f : 190f + 25f * pulse);
            game.batch.setColor(hs.visited ? 0.6f : 1f, hs.visited ? 0.7f : 0.86f, hs.visited ? 0.9f : 0.6f, a);
            game.batch.draw(glow, x - s / 2, y - s / 2, s, s);
        }
        game.batch.setColor(Color.WHITE);
        game.batch.setBlendFunction(srcF, dstF);

        for (int i = 0; i < chapter.hotspots.size; i++) {
            Content.Hotspot hs = chapter.hotspots.get(i);
            if (hs.visited) continue;
            float x = hs.x * UiKit.W, y = (1f - hs.y) * UiKit.H;
            float a = 0.55f + 0.45f * MathUtils.sin(t * 1.9f + i * 1.3f);
            game.ui.small.setColor(UiKit.CREAM.r, UiKit.CREAM.g, UiKit.CREAM.b, a * 0.9f);
            game.ui.centered(game.batch, game.ui.small, hs.label, x, y - 110f);
            game.ui.small.setColor(Color.WHITE);
        }
    }

    private void drawProgress() {
        String s = "Chapitre " + chapter.index + "/6 · " + chapter.title;
        game.ui.small.setColor(UiKit.MOON.r, UiKit.MOON.g, UiKit.MOON.b, 0.9f);
        game.ui.small.draw(game.batch, s, 240f, UiKit.H - 52f);
        game.ui.small.setColor(Color.WHITE);
        if (phase == Phase.EXPLORE) {
            String ex = visitedCount + "/" + chapter.requiredHotspots + " découvertes";
            game.ui.small.setColor(UiKit.ROSE);
            game.ui.small.draw(game.batch, ex, 240f, UiKit.H - 92f);
            game.ui.small.setColor(Color.WHITE);
        }
    }

    private void drawPuzzleFrame() {
        String riddle = puzzle.riddle();
        float top = UiKit.H - 150f;
        game.ui.h1.setColor(UiKit.GOLD);
        game.ui.centered(game.batch, game.ui.h1, chapter.title, UiKit.W / 2f, top + 60f);
        game.ui.h1.setColor(Color.WHITE);
        if (riddle != null) {
            game.ui.drawPanel(game.batch, 180f, top - 160f, UiKit.W - 360f, 180f, 0.92f);
            game.ui.script.setColor(UiKit.CREAM);
            game.ui.wrappedCentered(game.batch, game.ui.script, riddle, UiKit.W / 2f, top - 10f, UiKit.W - 440f);
            game.ui.script.setColor(Color.WHITE);
        } else {
            game.ui.script.setColor(UiKit.CREAM);
            game.ui.centered(game.batch, game.ui.script, puzzle.prompt(), UiKit.W / 2f, top - 10f);
            game.ui.script.setColor(Color.WHITE);
        }
    }

    private void drawTitleCard() {
        float a = MathUtils.clamp(titleCard > 2.4f ? (3.2f - titleCard) / 0.8f : Math.min(1f, titleCard / 0.8f), 0f, 1f);
        game.ui.rect(game.batch, 0, 0, UiKit.W, UiKit.H, UiKit.DEEP, a * 0.55f);
        game.ui.title.setColor(UiKit.CREAM.r, UiKit.CREAM.g, UiKit.CREAM.b, a);
        game.ui.centered(game.batch, game.ui.title, chapter.title, UiKit.W / 2f, UiKit.H / 2f + 40f);
        game.ui.title.setColor(Color.WHITE);
        game.ui.script.setColor(UiKit.ROSE.r, UiKit.ROSE.g, UiKit.ROSE.b, a);
        game.ui.centered(game.batch, game.ui.script, chapter.subtitle, UiKit.W / 2f, UiKit.H / 2f - 40f);
        game.ui.script.setColor(Color.WHITE);
    }

    private void drawJournal() {
        game.ui.rect(game.batch, 0, 0, UiKit.W, UiKit.H, UiKit.DEEP, 0.82f);
        game.ui.drawPanel(game.batch, 140f, 90f, UiKit.W - 280f, UiKit.H - 220f, 1f);
        game.ui.h1.setColor(UiKit.GOLD);
        game.ui.centered(game.batch, game.ui.h1, "Journal", UiKit.W / 2f, UiKit.H - 180f);
        game.ui.h1.setColor(Color.WHITE);

        float y = UiKit.H - 270f;
        game.ui.body.setColor(UiKit.ROSE);
        game.ui.body.draw(game.batch, "Signes reçus", 220f, y);
        game.ui.body.setColor(Color.WHITE);
        y -= 56f;
        float sx = 220f;
        for (Content.Chapter c : game.content.chapters) {
            if (c.rewardSymbol == null || !game.state.has("reward_" + c.id)) continue;
            Texture icon = game.assets.tex("img/sym_" + c.rewardSymbol + ".png");
            game.batch.setColor(1, 1, 1, 1);
            game.batch.draw(icon, sx, y - 90f, 96f, 96f);
            if (c.rewardNumber > 0) {
                game.ui.small.setColor(UiKit.GOLD);
                game.ui.centered(game.batch, game.ui.small, String.valueOf(c.rewardNumber), sx + 48f, y - 104f);
                game.ui.small.setColor(Color.WHITE);
            }
            sx += 140f;
        }
        if (sx == 220f) {
            game.ui.small.setColor(UiKit.MOON);
            game.ui.small.draw(game.batch, "Aucun signe pour l'instant.", 220f, y - 30f);
            game.ui.small.setColor(Color.WHITE);
        }

        y -= 170f;
        game.ui.body.setColor(UiKit.ROSE);
        game.ui.body.draw(game.batch, "Souvenirs retrouvés", 220f, y);
        game.ui.body.setColor(Color.WHITE);
        y -= 50f;
        int shown = 0;
        for (String m : game.state.memories) {
            if (shown >= 7) break;
            game.ui.small.setColor(UiKit.CREAM);
            game.ui.small.draw(game.batch, "· " + m, 240f, y);
            game.ui.small.setColor(Color.WHITE);
            y -= 40f;
            shown++;
        }
        if (shown == 0) {
            game.ui.small.setColor(UiKit.MOON);
            game.ui.small.draw(game.batch, "Touche les objets lumineux pour retrouver nos souvenirs.", 240f, y);
            game.ui.small.setColor(Color.WHITE);
        }

        game.ui.small.setColor(UiKit.MOON);
        game.ui.centered(game.batch, game.ui.small, "toucher pour fermer", UiKit.W / 2f, 140f);
        game.ui.small.setColor(Color.WHITE);
    }

    private void drawHint() {
        game.ui.rect(game.batch, 0, 0, UiKit.W, UiKit.H, UiKit.DEEP, 0.78f);
        game.ui.drawPanelLight(game.batch, 260f, 300f, UiKit.W - 520f, 320f, 1f);
        game.ui.h1.setColor(UiKit.GOLD);
        game.ui.centered(game.batch, game.ui.h1, "Un indice d'Esteban", UiKit.W / 2f, 560f);
        game.ui.h1.setColor(Color.WHITE);
        String text = phase == Phase.PUZZLE && puzzle != null
                ? puzzle.hint()
                : "Touche les endroits qui brillent : chacun cache un souvenir. Il t'en faut "
                + chapter.requiredHotspots + " pour continuer.";
        game.ui.body.setColor(UiKit.CREAM);
        game.ui.wrappedCentered(game.batch, game.ui.body, text, UiKit.W / 2f, 490f, UiKit.W - 640f);
        game.ui.body.setColor(Color.WHITE);
        game.ui.small.setColor(UiKit.MOON);
        game.ui.centered(game.batch, game.ui.small, "toucher pour fermer", UiKit.W / 2f, 330f);
        game.ui.small.setColor(Color.WHITE);
    }

    private void drawReward() {
        float a = rewardAnim;
        game.ui.rect(game.batch, 0, 0, UiKit.W, UiKit.H, UiKit.DEEP, 0.7f * a);
        if (chapter.rewardSymbol != null) {
            Texture icon = game.assets.tex("img/sym_" + chapter.rewardSymbol + ".png");
            float s = 260f * (0.7f + 0.3f * a) * (1f + 0.04f * MathUtils.sin(t * 2.4f));
            game.batch.setColor(1, 1, 1, a);
            game.batch.draw(icon, UiKit.W / 2f - s / 2, UiKit.H / 2f - s / 2 + 70f, s, s);
            game.batch.setColor(Color.WHITE);
        }
        game.ui.h1.setColor(UiKit.GOLD.r, UiKit.GOLD.g, UiKit.GOLD.b, a);
        game.ui.centered(game.batch, game.ui.h1, chapter.rewardText == null ? "" : chapter.rewardText,
                UiKit.W / 2f, UiKit.H / 2f - 130f);
        game.ui.h1.setColor(Color.WHITE);
    }

    // ----------------------------------------------------------------- input
    @Override
    protected void onTouchDown(float x, float y) {
        if (journalOpen || hintOpen) return;
        for (TouchButton b : hud) b.touchDown(x, y);
        solveBtn.touchDown(x, y);
        continueBtn.touchDown(x, y);
        if (phase == Phase.PUZZLE && puzzle != null && !dialogue.isActive()) puzzle.touchDown(x, y);
    }

    @Override
    protected void onTouchDragged(float x, float y) {
        if (journalOpen || hintOpen) return;
        if (phase == Phase.PUZZLE && puzzle != null && !dialogue.isActive()) puzzle.touchDragged(x, y);
    }

    @Override
    protected void onTouchUp(float x, float y) {
        if (journalOpen) { journalOpen = false; game.audio.tap(); return; }
        if (hintOpen) { hintOpen = false; game.audio.tap(); return; }

        if (journalBtn.touchUp(x, y)) { journalOpen = true; game.audio.page(); return; }
        if (hintBtn.touchUp(x, y)) { hintOpen = true; game.audio.page(); return; }
        if (backBtn.touchUp(x, y)) {
            game.audio.tap();
            fadeTo(new Runnable() { public void run() { game.switchTo(new TitleScreen(game)); } });
            return;
        }

        if (titleCard > 0.3f) { titleCard = 0.3f; return; }

        if (dialogue.isActive()) {
            boolean ended = dialogue.advance();
            game.audio.sfx("audio/sfx_tap.ogg", 0.35f, 1.3f);
            if (ended) onDialogueFinished();
            return;
        }

        if (continueBtn.visible && continueBtn.touchUp(x, y)) { advanceAfterOutro(); return; }
        if (solveBtn.visible && solveBtn.touchUp(x, y)) { startPuzzle(); return; }

        if (phase == Phase.EXPLORE) {
            for (Content.Hotspot hs : chapter.hotspots) {
                float hx = hs.x * UiKit.W, hy = (1f - hs.y) * UiKit.H;
                float r = Math.max(110f, hs.r * UiKit.W);
                if ((x - hx) * (x - hx) + (y - hy) * (y - hy) < r * r) {
                    openHotspot(hs, hx, hy);
                    return;
                }
            }
        } else if (phase == Phase.PUZZLE && puzzle != null) {
            puzzle.touchUp(x, y);
        }
    }

    @Override
    protected void onBack() {
        if (journalOpen) { journalOpen = false; return; }
        if (hintOpen) { hintOpen = false; return; }
        fadeTo(new Runnable() { public void run() { game.switchTo(new TitleScreen(game)); } });
    }

    // ------------------------------------------------------------- game flow
    private void openHotspot(Content.Hotspot hs, float hx, float hy) {
        game.audio.found();
        game.fx.burst(Fx.Kind.SPARK, hx, hy, 18);
        if (!hs.visited) {
            hs.visited = true;
            visitedCount++;
            if (hs.memory != null) game.state.addMemory(hs.memory);
            if (hs.clue != null) game.state.addClue(hs.clue);
            game.state.flag(chapter.id + "_" + hs.id);
        }
        pending = hs;
        for (Content.Dialog d : hs.lines) dialogue.push(d.speaker, d.text, d.portrait);
    }

    private void onDialogueFinished() {
        if (phase == Phase.INTRO) {
            phase = Phase.EXPLORE;
            updateSolveButton();
        } else if (phase == Phase.EXPLORE) {
            pending = null;
            updateSolveButton();
        } else if (phase == Phase.OUTRO) {
            phase = Phase.REWARD;
            rewardAnim = 0f;
            continueBtn.visible = true;
            continueBtn.label = chapter.index < 6 ? "Continuer le voyage" : "Ouvrir la lettre";
            game.audio.sparkle();
            game.fx.burst(Fx.Kind.SPARK, UiKit.W / 2f, UiKit.H / 2f, 30);
        }
    }

    private void updateSolveButton() {
        boolean ready = visitedCount >= chapter.requiredHotspots;
        solveBtn.visible = phase == Phase.EXPLORE && ready;
    }

    private void startPuzzle() {
        game.audio.page();
        phase = Phase.PUZZLE;
        solveBtn.visible = false;
        puzzle = Puzzle.create(chapter.puzzle.getString("type"));
        puzzle.init(chapter.puzzle, game.ui, game.fx, game.audio, game.assets);
        Gdx.app.log("Chapter", "puzzle started: " + chapter.puzzle.getString("type"));
    }

    private void enterOutro() {
        phase = Phase.OUTRO;
        game.state.flag(chapter.id + "_solved");
        if (chapter.rewardSymbol != null) game.state.flag("reward_" + chapter.id);
        game.state.unlockChapter(Math.min(6, chapter.index + 1));
        for (Content.Dialog d : chapter.outro) dialogue.push(d.speaker, d.text, d.portrait);
        if (chapter.outro.size == 0) onDialogueFinished();
    }

    private void advanceAfterOutro() {
        Gdx.app.log("Milestone", "chapter-complete " + chapter.index);
        game.audio.tap();
        continueBtn.visible = false;
        if (chapter.index < 6) {
            final int next = chapter.index + 1;
            fadeTo(new Runnable() {
                public void run() {
                    game.fx.clear();
                    game.switchTo(new CutsceneScreen(game, "ch" + next, new Runnable() {
                        public void run() { game.switchTo(new ChapterScreen(game, next)); }
                    }));
                }
            });
        } else {
            game.state.letterUnlocked = true;
            game.state.save();
            fadeTo(new Runnable() {
                public void run() {
                    game.fx.clear();
                    game.switchTo(new CutsceneScreen(game, "letter", new Runnable() {
                        public void run() { game.switchTo(new LetterScreen(game)); }
                    }));
                }
            });
        }
    }
}
