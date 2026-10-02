package com.esteban.lohen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreetypeFontLoader;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ObjectMap;

/** Central asset registry. Textures/audio are streamed in by the AssetManager. */
public class Assets implements Disposable {
    public final AssetManager manager = new AssetManager(new InternalFileHandleResolver());

    public static final String[] BACKGROUNDS = {
            "img/bg_title.jpg", "img/bg_ch1_seuil.jpg", "img/bg_ch2_jardin.jpg",
            "img/bg_ch3_biblio.jpg", "img/bg_ch4_ciel.jpg", "img/bg_ch5_musique.jpg",
            "img/bg_ch6_phare.jpg", "img/paper_letter.jpg"
    };
    public static final String[] SPRITES = {
            "img/char_lohen.png", "img/char_esteban.png", "img/heart.png",
            "img/glow.png", "img/spark.png", "img/petal.png",
            "img/sym_soleil.png", "img/sym_lune.png", "img/sym_coeur.png",
            "img/sym_etoile.png", "img/sym_note.png"
    };
    public static final String[] MUSIC = {
            "audio/music_title.ogg", "audio/music_night.ogg",
            "audio/music_memory.ogg", "audio/music_letter.ogg"
    };
    public static final String[] SOUNDS = {
            "audio/sfx_tap.ogg", "audio/sfx_found.ogg", "audio/sfx_wrong.ogg",
            "audio/sfx_unlock.ogg", "audio/sfx_page.ogg", "audio/sfx_sparkle.ogg",
            "audio/note_1.ogg", "audio/note_2.ogg", "audio/note_3.ogg",
            "audio/note_4.ogg", "audio/note_5.ogg"
    };

    private final ObjectMap<String, Texture> textureCache = new ObjectMap<String, Texture>();

    public void queueAll() {
        manager.setLoader(FreeTypeFontGenerator.class,
                new com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGeneratorLoader(new InternalFileHandleResolver()));
        manager.setLoader(BitmapFont.class, ".ttf", new FreetypeFontLoader(new InternalFileHandleResolver()));

        for (String b : BACKGROUNDS) manager.load(b, Texture.class);
        for (String s : SPRITES) manager.load(s, Texture.class);
        for (String m : MUSIC) manager.load(m, Music.class);
        for (String s : SOUNDS) manager.load(s, Sound.class);
    }

    public boolean update() { return manager.update(16); }
    public float progress() { return manager.getProgress(); }

    /**
     * HD tier: the full build ships 2048px lossless PNG masters under img/hd/.
     * When one exists for the requested image it is used instead of the
     * compressed version, transparently for every screen.
     */
    private String hdPath(String path) {
        if (!path.startsWith("img/") || path.startsWith("img/hd/")) return path;
        String name = path.substring(4);
        if (!name.startsWith("bg_") && !name.startsWith("paper_")) return path;
        int dot = name.lastIndexOf('.');
        if (dot > 0) name = name.substring(0, dot);
        String hd = "img/hd/" + name + ".png";
        Boolean known = hdCache.get(hd);
        if (known == null) {
            known = Gdx.files.internal(hd).exists();
            hdCache.put(hd, known);
        }
        return known ? hd : path;
    }

    private final java.util.HashMap<String, Boolean> hdCache = new java.util.HashMap<String, Boolean>();
    private final com.badlogic.gdx.utils.Array<String> hdLru = new com.badlogic.gdx.utils.Array<String>();
    private static final int HD_LIVE = 2;

    /** Keeps at most HD_LIVE lossless masters resident: they are 2048px RGBA. */
    private void trimHdCache(String justLoaded) {
        hdLru.removeValue(justLoaded, false);
        hdLru.add(justLoaded);
        while (hdLru.size > HD_LIVE) {
            String old = hdLru.removeIndex(0);
            Texture t = textureCache.remove(old);
            if (t != null) t.dispose();
        }
    }

    public Texture tex(String rawPath) {
        String path = hdPath(rawPath);
        if (manager.isLoaded(path, Texture.class)) {
            Texture t = manager.get(path, Texture.class);
            t.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            return t;
        }
        Texture cached = textureCache.get(path);
        if (cached != null) return cached;
        try {
            Texture t = new Texture(Gdx.files.internal(path), true);
            t.setFilter(Texture.TextureFilter.MipMapLinearLinear, Texture.TextureFilter.Linear);
            textureCache.put(path, t);
            if (path.startsWith("img/hd/")) trimHdCache(path);
            return t;
        } catch (Throwable e) {
            Gdx.app.error("Assets", "missing texture " + path, e);
            Pixmap pm = new Pixmap(4, 4, Pixmap.Format.RGBA8888);
            pm.setColor(0.1f, 0.07f, 0.15f, 1f); pm.fill();
            Texture t = new Texture(pm); pm.dispose();
            textureCache.put(path, t);
            return t;
        }
    }

    private final java.util.HashMap<String, Music> extraMusic = new java.util.HashMap<String, Music>();

    public Sound sound(String path) {
        if (manager.isLoaded(path, Sound.class)) return manager.get(path, Sound.class);
        return null;
    }

    public Music music(String path) {
        if (manager.isLoaded(path, Music.class)) return manager.get(path, Music.class);
        Music extra = extraMusic.get(path);
        if (extra != null) return extra;
        // HD tier tracks are streamed on demand instead of preloaded: they are
        // large lossless files and only one plays at a time.
        com.badlogic.gdx.files.FileHandle fh = com.badlogic.gdx.Gdx.files.internal(path);
        if (!fh.exists()) return null;
        try {
            Music m = com.badlogic.gdx.Gdx.audio.newMusic(fh);
            extraMusic.put(path, m);
            return m;
        } catch (Throwable t) {
            com.badlogic.gdx.Gdx.app.error("Assets", "music " + path + ": " + t);
            return null;
        }
    }

    @Override public void dispose() {
        for (Music m : extraMusic.values()) { try { m.dispose(); } catch (Throwable ignored) { } }
        extraMusic.clear();
        for (Texture t : textureCache.values()) t.dispose();
        textureCache.clear();
        manager.dispose();
    }
}
