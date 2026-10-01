package com.esteban.lohen.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.Vector3;

import com.esteban.lohen.LohenGame;
import com.esteban.lohen.ui.UiKit;

/** Common plumbing: virtual viewport, touch unprojection and fade transitions. */
public abstract class BaseScreen extends ScreenAdapter {
    protected final LohenGame game;
    protected float fadeIn = 1f;        // 1 = black, 0 = fully visible
    protected float fadeOut = 0f;
    protected Runnable afterFadeOut;
    private final Vector3 tmp = new Vector3();

    protected BaseScreen(LohenGame game) {
        this.game = game;
    }

    protected float touchX(int screenX, int screenY) {
        tmp.set(screenX, screenY, 0);
        game.viewport.unproject(tmp);
        return tmp.x;
    }

    protected float touchY(int screenX, int screenY) {
        tmp.set(screenX, screenY, 0);
        game.viewport.unproject(tmp);
        return tmp.y;
    }

    protected void fadeTo(Runnable action) {
        if (afterFadeOut != null) return;
        afterFadeOut = action;
    }

    @Override
    public void show() {
        Gdx.input.setCatchKey(Input.Keys.BACK, true);
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override public boolean touchDown(int x, int y, int pointer, int button) {
                if (pointer > 0 || busy()) return false;
                onTouchDown(touchX(x, y), touchY(x, y));
                return true;
            }
            @Override public boolean touchDragged(int x, int y, int pointer) {
                if (pointer > 0 || busy()) return false;
                onTouchDragged(touchX(x, y), touchY(x, y));
                return true;
            }
            @Override public boolean touchUp(int x, int y, int pointer, int button) {
                if (pointer > 0 || busy()) return false;
                onTouchUp(touchX(x, y), touchY(x, y));
                return true;
            }
            @Override public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.BACK || keycode == Input.Keys.ESCAPE) {
                    onBack();
                    return true;
                }
                return false;
            }
        });
    }

    protected boolean busy() { return afterFadeOut != null || fadeIn > 0.9f; }

    protected void onTouchDown(float x, float y) { }

    protected void onTouchDragged(float x, float y) { }

    protected void onTouchUp(float x, float y) { }

    protected void onBack() { }

    protected void clear() {
        Gdx.gl.glClearColor(UiKit.DEEP.r, UiKit.DEEP.g, UiKit.DEEP.b, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }

    protected void updateFades(float dt) {
        if (fadeIn > 0) fadeIn = Math.max(0, fadeIn - dt * 1.1f);
        if (afterFadeOut != null) {
            fadeOut = Math.min(1f, fadeOut + dt * 1.6f);
            if (fadeOut >= 1f) {
                Runnable r = afterFadeOut;
                afterFadeOut = null;
                r.run();
            }
        }
    }

    protected void drawFades() {
        float a = Math.max(fadeIn, fadeOut);
        if (a > 0.001f) {
            game.ui.rect(game.batch, 0, 0, UiKit.W, UiKit.H, UiKit.DEEP, a);
        }
    }

    protected void begin() {
        game.viewport.apply();
        game.batch.setProjectionMatrix(game.viewport.getCamera().combined);
        game.batch.begin();
    }

    protected void end() {
        game.batch.end();
    }
}
