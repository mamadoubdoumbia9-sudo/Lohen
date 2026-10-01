package com.esteban.lohen;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonValue;

import com.esteban.lohen.game.Content;

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

/**
 * Deterministic tests over the real content files shipped in the APK.
 * They guarantee every chapter is playable and that each puzzle is solvable.
 */
public class ContentTest {
    private static Content content;

    private static File asset(String rel) {
        File f = new File("../android/assets/" + rel);
        if (!f.exists()) f = new File("android/assets/" + rel);
        return f;
    }

    @BeforeClass
    public static void loadContent() {
        content = Content.load(new FileHandle(asset("data/chapters.json")),
                new FileHandle(asset("data/letter.json")));
    }

    @Test
    public void contentValidates() {
        String problems = content.validate();
        assertEquals("content validation problems:\n" + problems, "", problems);
    }

    @Test
    public void sixChaptersInOrder() {
        assertEquals(6, content.chapters.size);
        for (int i = 0; i < 6; i++) assertEquals(i + 1, content.chapters.get(i).index);
    }

    @Test
    public void everyReferencedAssetExists() {
        for (Content.Chapter c : content.chapters) {
            assertTrue("missing " + c.background, asset(c.background).exists());
            if (c.music != null) assertTrue("missing " + c.music, asset(c.music).exists());
            for (Content.Dialog d : c.intro) if (d.portrait != null)
                assertTrue("missing " + d.portrait, asset(d.portrait).exists());
            for (Content.Hotspot h : c.hotspots)
                for (Content.Dialog d : h.lines) if (d.portrait != null)
                    assertTrue("missing " + d.portrait, asset(d.portrait).exists());
            if (c.rewardSymbol != null && !"lettre".equals(c.rewardSymbol) && !"note".equals(c.rewardSymbol))
                assertTrue("missing symbol " + c.rewardSymbol,
                        asset("img/sym_" + c.rewardSymbol + ".png").exists());
        }
    }

    @Test
    public void everyPuzzleIsSolvable() {
        for (Content.Chapter c : content.chapters) {
            JsonValue p = c.puzzle;
            String type = p.getString("type");
            assertTrue(c.id + ": hint required", p.getString("hint", "").length() > 10);
            if ("order".equals(type)) {
                int n = p.get("items").size;
                int[] sol = p.get("solution").asIntArray();
                assertEquals(c.id + ": solution length", n, sol.length);
                assertTrue(c.id + ": solution must be a permutation", isPermutation(sol, n));
            } else if ("lanterns".equals(type)) {
                int n = p.get("lanterns").size;
                int[] sol = p.get("solution").asIntArray();
                assertEquals(c.id + ": every lantern used", n, sol.length);
                assertTrue(c.id + ": solution must be a permutation", isPermutation(sol, n));
            } else if ("riddle".equals(type)) {
                String answer = p.getString("answer").toUpperCase();
                String pool = p.getString("letters").toUpperCase();
                StringBuilder remaining = new StringBuilder(pool);
                for (char ch : answer.toCharArray()) {
                    int idx = remaining.indexOf(String.valueOf(ch));
                    assertTrue(c.id + ": letter '" + ch + "' not available", idx >= 0);
                    remaining.deleteCharAt(idx);
                }
                assertTrue(c.id + ": needs decoy letters", pool.length() > answer.length());
            } else if ("constellation".equals(type)) {
                int n = p.get("stars").size;
                int[] path = p.get("path").asIntArray();
                assertTrue(c.id + ": path too short", path.length >= n);
                Set<Integer> seen = new HashSet<Integer>();
                for (int idx : path) {
                    assertTrue(c.id + ": star index out of range", idx >= 0 && idx < n);
                    seen.add(idx);
                }
                assertEquals(c.id + ": every star must be used", n, seen.size());
                assertEquals(c.id + ": the shape must close", path[0], path[path.length - 1]);
            } else if ("melody".equals(type)) {
                int[] seq = p.get("sequence").asIntArray();
                assertTrue(c.id + ": melody too short", seq.length >= 4);
                for (int n : seq) assertTrue(c.id + ": note out of range", n >= 0 && n < 5);
            } else if ("code".equals(type)) {
                JsonValue syms = p.get("symbols");
                String[] sol = p.get("solution").asStringArray();
                assertEquals(c.id + ": slots mismatch", syms.size, sol.length);
                for (String s : sol) {
                    boolean found = false;
                    for (JsonValue v = syms.child; v != null; v = v.next) if (v.asString().equals(s)) found = true;
                    assertTrue(c.id + ": unknown symbol " + s, found);
                }
            } else {
                throw new AssertionError("unknown puzzle type " + type);
            }
        }
    }

    /** The final code can only be deduced if every symbol carries a distinct number. */
    @Test
    public void finalCodeIsDeducibleFromCollectedClues() {
        Content.Chapter last = content.chapter(6);
        String[] solution = last.puzzle.get("solution").asStringArray();
        Set<Integer> numbers = new HashSet<Integer>();
        for (String sym : solution) {
            Content.Chapter source = null;
            for (Content.Chapter c : content.chapters) if (sym.equals(c.rewardSymbol)) source = c;
            assertTrue("symbol " + sym + " is never given to the player", source != null);
            assertTrue("symbol " + sym + " carries no number", source.rewardNumber > 0);
            numbers.add(source.rewardNumber);
        }
        assertEquals("numbers must be distinct", solution.length, numbers.size());
        // order of the solution must be the ascending order of the numbers
        int previous = 0;
        for (String sym : solution) {
            for (Content.Chapter c : content.chapters) {
                if (sym.equals(c.rewardSymbol)) {
                    assertTrue("solution is not sorted by number", c.rewardNumber > previous);
                    previous = c.rewardNumber;
                }
            }
        }
    }

    @Test
    public void lockedClosingLinesAreExact() {
        assertEquals(2, content.letter.closing.size);
        assertEquals("je t'aime", content.letter.closing.get(0));
        assertEquals("j'espère que tu as apprécié mon cadeau", content.letter.closing.get(1));
    }

    @Test
    public void letterIsARealLetter() {
        int chars = 0;
        for (String p : content.letter.paragraphs) chars += p.length();
        assertTrue("letter should be a substantial text, got " + chars, chars > 1200);
        assertEquals("Esteban", content.letter.signature);
    }

    @Test
    public void everyChapterHasEnoughToExplore() {
        for (Content.Chapter c : content.chapters) {
            assertTrue(c.id + ": needs at least 2 hotspots", c.hotspots.size >= 2);
            assertTrue(c.id + ": requiredHotspots must be reachable",
                    c.requiredHotspots >= 1 && c.requiredHotspots <= c.hotspots.size);
            int withDialog = 0;
            for (Content.Hotspot h : c.hotspots) if (h.lines.size > 0) withDialog++;
            assertEquals(c.id + ": every hotspot needs narration", c.hotspots.size, withDialog);
        }
    }

    private static boolean isPermutation(int[] values, int n) {
        Set<Integer> seen = new HashSet<Integer>();
        for (int v : values) {
            if (v < 0 || v >= n) return false;
            seen.add(v);
        }
        return seen.size() == n;
    }
}
