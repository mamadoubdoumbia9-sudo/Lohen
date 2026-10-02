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

    /** HD lossless cue names, by chapter index (1..6). See tools/gen_audio_hd.py. */
    private static final String[] HD_CHAPTER = {
            "hd_title", "hd_ch1_seuil", "hd_ch2_jardin", "hd_ch3_biblio",
            "hd_ch4_ciel", "hd_ch5_musique", "hd_ch6_phare"
    };

    /** Resolves to the lossless 48 kHz cue when the full build shipped it. */
    public static String hd(String name) {
        if (name == null) return null;
        String path = "audio/hd/" + name + ".flac";
        return com.badlogic.gdx.Gdx.files.internal(path).exists() ? path : null;
    }

    public void playChapterMusic(int chapterIndex, String fallback) {
        String name = (chapterIndex >= 0 && chapterIndex < HD_CHAPTER.length)
                ? HD_CHAPTER[chapterIndex] : null;
        String hd = hd(name);
        playMusic(hd != null ? hd : fallback);
    }

    public void playCue(String hdName, String fallback) {
        String hd = hd(hdName);
        playMusic(hd != null ? hd : fallback);
    }

    // ------------------------------------------------------------- voice-over
    private Music voice;

    /** Plays a narration clip (voice/<name>.flac) over the music, if shipped. */
    public void speak(String name) {
        stopVoice();
        String path = "voice/" + name + ".flac";
        com.badlogic.gdx.files.FileHandle fh = com.badlogic.gdx.Gdx.files.internal(path);
        if (!fh.exists()) return;
        try {
            voice = com.badlogic.gdx.Gdx.audio.newMusic(fh);
            voice.setVolume(Math.min(1f, state.sfxVolume + 0.15f));
            voice.play();
        } catch (Throwable t) {
            com.badlogic.gdx.Gdx.app.error("Audio", "voice " + name + ": " + t);
            voice = null;
        }
    }

    public boolean isSpeaking() {
        try { return voice != null && voice.isPlaying(); } catch (Throwable t) { return false; }
    }

    public void pauseVoice() { if (voice != null) { try { voice.pause(); } catch (Throwable ignored) { } } }

    public void resumeVoice() { if (voice != null) { try { voice.play(); } catch (Throwable ignored) { } } }

    public void stopVoice() {
        if (voice != null) {
            try { voice.stop(); voice.dispose(); } catch (Throwable ignored) { }
            voice = null;
        }
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

    @Override public void dispose() { stopVoice(); stopMusic(); }
}
