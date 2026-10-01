package com.esteban.lohen;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.Disposable;

/** Music crossfade + sfx playback, driven by the player's volume settings. */
public class AudioManager implements Disposable {
    private final Assets assets;
    private final GameState state;
    private Music current;
    private String currentPath = "";
    private float fade = 1f;
    private boolean fadingOut = false;
    private String pending;

    public AudioManager(Assets assets, GameState state) {
        this.assets = assets; this.state = state;
    }

    public void playMusic(String path) {
        if (path == null) { stopMusic(); return; }
        if (path.equals(currentPath) && current != null && current.isPlaying()) return;
        if (current != null && current.isPlaying()) { pending = path; fadingOut = true; return; }
        start(path);
    }

    private void start(String path) {
        Music m = assets.music(path);
        if (m == null) return;
        current = m; currentPath = path; fade = 0f; fadingOut = false;
        m.setLooping(true);
        m.setVolume(0f);
        try { m.play(); } catch (Throwable ignored) { }
    }

    public void stopMusic() {
        if (current != null) { try { current.stop(); } catch (Throwable ignored) { } }
        current = null; currentPath = "";
    }

    public void update(float dt) {
        if (current == null) return;
        if (fadingOut) {
            fade = Math.max(0f, fade - dt * 1.4f);
            current.setVolume(fade * state.musicVolume);
            if (fade <= 0f) {
                current.stop();
                String p = pending; pending = null; current = null; currentPath = "";
                if (p != null) start(p);
            }
        } else if (fade < 1f) {
            fade = Math.min(1f, fade + dt * 0.7f);
            current.setVolume(fade * state.musicVolume);
        } else {
            current.setVolume(state.musicVolume);
        }
    }

    public void sfx(String path) { sfx(path, 1f, 1f); }

    public void sfx(String path, float volume, float pitch) {
        Sound s = assets.sound(path);
        if (s == null) return;
        try { s.play(volume * state.sfxVolume, pitch, 0f); } catch (Throwable ignored) { }
    }

    public void tap() { sfx("audio/sfx_tap.ogg", 0.6f, 1f); }
    public void found() { sfx("audio/sfx_found.ogg", 0.9f, 1f); }
    public void wrong() { sfx("audio/sfx_wrong.ogg", 0.6f, 1f); }
    public void unlock() { sfx("audio/sfx_unlock.ogg", 1f, 1f); }
    public void page() { sfx("audio/sfx_page.ogg", 0.8f, 1f); }
    public void sparkle() { sfx("audio/sfx_sparkle.ogg", 0.7f, 1f); }
    public void note(int index) { sfx("audio/note_" + (1 + (index % 5)) + ".ogg", 0.9f, 1f); }

    @Override public void dispose() { stopMusic(); }
}
