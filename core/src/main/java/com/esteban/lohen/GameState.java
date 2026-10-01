package com.esteban.lohen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Persistent player progression. Stored through libGDX Preferences (XML on Android),
 * written atomically by the platform. Versioned so old saves can be migrated.
 */
public class GameState {
    public static final int SAVE_VERSION = 1;
    private static final String PREFS = "pour-lohen-save";

    public int chapter = 1;                 // highest chapter unlocked (1..6)
    public int currentChapter = 1;          // chapter the player is in
    public final Set<String> flags = new LinkedHashSet<String>();   // narrative flags
    public final Set<String> clues = new LinkedHashSet<String>();   // collected clue ids
    public final Set<String> memories = new LinkedHashSet<String>();// gallery entries
    public boolean letterUnlocked = false;
    public boolean finished = false;

    public float musicVolume = 0.7f;
    public float sfxVolume = 0.85f;
    public float textSpeed = 1.0f;          // 0.5 slow .. 2 fast
    public boolean reducedMotion = false;

    private Preferences prefs;

    public void load() {
        try {
            prefs = Gdx.app.getPreferences(PREFS);
        } catch (Throwable t) {
            Gdx.app.error("GameState", "preferences unavailable", t);
            return;
        }
        int v = prefs.getInteger("version", 0);
        if (v == 0) { save(); return; }
        if (v > SAVE_VERSION) { // save from a newer build: start clean rather than crash
            reset(); save(); return;
        }
        chapter = clamp(prefs.getInteger("chapter", 1), 1, 6);
        currentChapter = clamp(prefs.getInteger("currentChapter", 1), 1, 6);
        letterUnlocked = prefs.getBoolean("letterUnlocked", false);
        finished = prefs.getBoolean("finished", false);
        musicVolume = prefs.getFloat("musicVolume", 0.7f);
        sfxVolume = prefs.getFloat("sfxVolume", 0.85f);
        textSpeed = prefs.getFloat("textSpeed", 1f);
        reducedMotion = prefs.getBoolean("reducedMotion", false);
        readSet("flags", flags);
        readSet("clues", clues);
        readSet("memories", memories);
    }

    public void save() {
        if (prefs == null) return;
        prefs.putInteger("version", SAVE_VERSION);
        prefs.putInteger("chapter", chapter);
        prefs.putInteger("currentChapter", currentChapter);
        prefs.putBoolean("letterUnlocked", letterUnlocked);
        prefs.putBoolean("finished", finished);
        prefs.putFloat("musicVolume", musicVolume);
        prefs.putFloat("sfxVolume", sfxVolume);
        prefs.putFloat("textSpeed", textSpeed);
        prefs.putBoolean("reducedMotion", reducedMotion);
        writeSet("flags", flags);
        writeSet("clues", clues);
        writeSet("memories", memories);
        prefs.flush();
    }

    public void reset() {
        chapter = 1; currentChapter = 1;
        flags.clear(); clues.clear(); memories.clear();
        letterUnlocked = false; finished = false;
        save();
    }

    public boolean hasSave() { return prefs != null && prefs.getInteger("version", 0) > 0
            && (chapter > 1 || !flags.isEmpty()); }

    public void flag(String id) { if (flags.add(id)) save(); }
    public boolean has(String id) { return flags.contains(id); }
    public void addClue(String id) { if (clues.add(id)) save(); }
    public void addMemory(String id) { if (memories.add(id)) save(); }

    public void unlockChapter(int c) {
        if (c > chapter) { chapter = Math.min(6, c); }
        currentChapter = Math.min(6, c);
        save();
    }

    private void readSet(String key, Set<String> target) {
        target.clear();
        String raw = prefs.getString(key, "");
        if (raw.isEmpty()) return;
        for (String s : raw.split("\\|")) if (!s.isEmpty()) target.add(s);
    }

    private void writeSet(String key, Set<String> src) {
        StringBuilder sb = new StringBuilder();
        for (String s : src) { if (sb.length() > 0) sb.append('|'); sb.append(s); }
        prefs.putString(key, sb.toString());
    }

    private static int clamp(int v, int lo, int hi) { return v < lo ? lo : (v > hi ? hi : v); }
}
