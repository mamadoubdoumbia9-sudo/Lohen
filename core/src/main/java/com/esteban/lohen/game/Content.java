package com.esteban.lohen.game;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

/**
 * Loads the narrative content (chapters, hotspots, puzzles, letter) from JSON
 * data files. Content is kept strictly separate from gameplay logic so the
 * writing can change without touching a single line of code.
 */
public class Content {
    public final Array<Chapter> chapters = new Array<Chapter>();
    public Letter letter;

    public static class Chapter {
        public String id, title, subtitle, background, music, ambient, puzzlePrompt;
        public int index;
        public final Array<Dialog> intro = new Array<Dialog>();
        public final Array<Dialog> outro = new Array<Dialog>();
        public final Array<Hotspot> hotspots = new Array<Hotspot>();
        public int requiredHotspots;
        public JsonValue puzzle;
        public String rewardSymbol, rewardText;
        public int rewardNumber;
    }

    public static class Dialog {
        public String speaker, text, portrait;
    }

    public static class Hotspot {
        public String id, label, memory, clue;
        public float x, y, r;
        public final Array<Dialog> lines = new Array<Dialog>();
        public boolean visited;
    }

    public static class Letter {
        public String title, signature;
        public final Array<String> paragraphs = new Array<String>();
        public final Array<String> closing = new Array<String>();
    }

    public static Content load(FileHandle chaptersFile, FileHandle letterFile) {
        Content c = new Content();
        JsonReader reader = new JsonReader();
        JsonValue root = reader.parse(chaptersFile);
        for (JsonValue jc = root.get("chapters").child; jc != null; jc = jc.next) {
            Chapter ch = new Chapter();
            ch.id = jc.getString("id");
            ch.index = jc.getInt("index");
            ch.title = jc.getString("title");
            ch.subtitle = jc.getString("subtitle", "");
            ch.background = jc.getString("background");
            ch.music = jc.getString("music", null);
            ch.ambient = jc.getString("ambient", "DUST");
            ch.requiredHotspots = jc.getInt("requiredHotspots", 1);
            ch.puzzlePrompt = jc.getString("puzzlePrompt", "");
            readDialogs(jc.get("intro"), ch.intro);
            readDialogs(jc.get("outro"), ch.outro);
            for (JsonValue jh = jc.get("hotspots").child; jh != null; jh = jh.next) {
                Hotspot h = new Hotspot();
                h.id = jh.getString("id");
                h.label = jh.getString("label");
                h.memory = jh.getString("memory", null);
                h.clue = jh.getString("clue", null);
                h.x = jh.getFloat("x");
                h.y = jh.getFloat("y");
                h.r = jh.getFloat("r", 0.09f);
                readDialogs(jh.get("lines"), h.lines);
                ch.hotspots.add(h);
            }
            ch.puzzle = jc.get("puzzle");
            JsonValue rw = jc.get("reward");
            if (rw != null) {
                ch.rewardSymbol = rw.getString("symbol", null);
                ch.rewardNumber = rw.getInt("number", 0);
                ch.rewardText = rw.getString("text", "");
            }
            c.chapters.add(ch);
        }

        JsonValue jl = reader.parse(letterFile);
        Letter l = new Letter();
        l.title = jl.getString("title");
        l.signature = jl.getString("signature");
        for (JsonValue p = jl.get("paragraphs").child; p != null; p = p.next) l.paragraphs.add(p.asString());
        for (JsonValue p = jl.get("closing").child; p != null; p = p.next) l.closing.add(p.asString());
        c.letter = l;
        return c;
    }

    private static void readDialogs(JsonValue arr, Array<Dialog> target) {
        if (arr == null) return;
        for (JsonValue jd = arr.child; jd != null; jd = jd.next) {
            Dialog d = new Dialog();
            d.speaker = jd.getString("s", null);
            d.text = jd.getString("t");
            d.portrait = jd.getString("p", null);
            target.add(d);
        }
    }

    public Chapter chapter(int index1based) {
        for (Chapter c : chapters) if (c.index == index1based) return c;
        return chapters.first();
    }

    /** Validation used by the automated tests and at boot time. */
    public String validate() {
        StringBuilder problems = new StringBuilder();
        if (chapters.size != 6) problems.append("expected 6 chapters, got ").append(chapters.size).append('\n');
        for (Chapter c : chapters) {
            if (c.background == null || c.background.isEmpty()) problems.append(c.id).append(": no background\n");
            if (c.hotspots.size == 0) problems.append(c.id).append(": no hotspots\n");
            if (c.puzzle == null) problems.append(c.id).append(": no puzzle\n");
            if (c.intro.size == 0) problems.append(c.id).append(": no intro\n");
            if (c.requiredHotspots > c.hotspots.size) problems.append(c.id).append(": requiredHotspots too high\n");
            for (Hotspot h : c.hotspots) {
                if (h.x < 0 || h.x > 1 || h.y < 0 || h.y > 1) problems.append(c.id).append('/').append(h.id).append(": hotspot out of bounds\n");
                if (h.lines.size == 0) problems.append(c.id).append('/').append(h.id).append(": no lines\n");
            }
        }
        if (letter == null) {
            problems.append("no letter\n");
        } else {
            if (letter.paragraphs.size < 8) problems.append("letter too short\n");
            if (letter.closing.size != 2) problems.append("letter closing must have exactly 2 lines\n");
            else {
                if (!letter.closing.get(0).equals("je t'aime"))
                    problems.append("locked closing line 1 altered\n");
                if (!letter.closing.get(1).equals("j'espère que tu as apprécié mon cadeau"))
                    problems.append("locked closing line 2 altered\n");
            }
        }
        return problems.toString();
    }
}
