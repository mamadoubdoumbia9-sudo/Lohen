package com.esteban.lohen.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.utils.Disposable;

/**
 * Visual design system: typography, palette, panels and buttons.
 * Fonts are rasterised at device resolution then scaled back to the 1600x900
 * virtual space so text stays crisp on any screen.
 */
public class UiKit implements Disposable {
    public static final float W = 1600f, H = 900f;

    // palette (romantic nocturne)
    public static final Color INK = new Color(0.102f, 0.071f, 0.149f, 1f);
    public static final Color PLUM = new Color(0.192f, 0.118f, 0.251f, 1f);
    public static final Color ROSE = new Color(0.894f, 0.572f, 0.651f, 1f);
    public static final Color GOLD = new Color(0.949f, 0.800f, 0.541f, 1f);
    public static final Color CREAM = new Color(0.976f, 0.949f, 0.906f, 1f);
    public static final Color MOON = new Color(0.682f, 0.761f, 0.890f, 1f);
    public static final Color DEEP = new Color(0.055f, 0.051f, 0.106f, 1f);

    public BitmapFont title, h1, body, small, script;
    public Texture white, panelTex, panelLightTex, vignetteTex;
    public NinePatch panel, panelLight;
    public final GlyphLayout layout = new GlyphLayout();

    private final FreeTypeFontGenerator genSerif, genBold, genItalic, genSans;

    public UiKit() {
        float deviceH = Math.max(360, Gdx.graphics.getHeight());
        float ratio = Math.min(3f, Math.max(1f, deviceH / H));
        float scaleBack = 1f / ratio;

        genSerif = gen("font/serif.ttf");
        genBold = gen("font/serif-bold.ttf");
        genItalic = gen("font/serif-italic.ttf");
        genSans = gen("font/sans.ttf");

        title = make(genBold, (int) (104 * ratio), CREAM, 3 * ratio, scaleBack);
        h1 = make(genBold, (int) (62 * ratio), CREAM, 2 * ratio, scaleBack);
        body = make(genSerif, (int) (46 * ratio), CREAM, 2 * ratio, scaleBack);
        small = make(genSans, (int) (28 * ratio), CREAM, 1.5f * ratio, scaleBack);
        script = make(genItalic, (int) (50 * ratio), CREAM, 2 * ratio, scaleBack);

        Pixmap p = new Pixmap(2, 2, Pixmap.Format.RGBA8888);
        p.setColor(Color.WHITE);
        p.fill();
        white = new Texture(p);
        p.dispose();

        panelTex = roundedRect(64, 24, new Color(0.08f, 0.06f, 0.12f, 0.88f), new Color(0.949f, 0.800f, 0.541f, 0.55f));
        panel = new NinePatch(panelTex, 26, 26, 26, 26);
        panelLightTex = roundedRect(64, 24, new Color(0.16f, 0.11f, 0.21f, 0.94f), new Color(0.894f, 0.572f, 0.651f, 0.7f));
        panelLight = new NinePatch(panelLightTex, 26, 26, 26, 26);
        vignetteTex = vignette(256);
    }

    private FreeTypeFontGenerator gen(String path) {
        return new FreeTypeFontGenerator(Gdx.files.internal(path));
    }

    private BitmapFont make(FreeTypeFontGenerator g, int size, Color c, float shadow, float scaleBack) {
        FreeTypeFontGenerator.FreeTypeFontParameter p = new FreeTypeFontGenerator.FreeTypeFontParameter();
        p.size = Math.max(8, size);
        p.color = new Color(c);
        p.shadowColor = new Color(0f, 0f, 0f, 0.55f);
        p.shadowOffsetX = (int) shadow;
        p.shadowOffsetY = (int) shadow;
        p.minFilter = Texture.TextureFilter.Linear;
        p.magFilter = Texture.TextureFilter.Linear;
        p.characters = FreeTypeFontGenerator.DEFAULT_CHARS
                + "\u00e0\u00e2\u00e4\u00e9\u00e8\u00ea\u00eb\u00ef\u00ee\u00f4\u00f6\u00f9\u00fb\u00fc\u00ff\u00e7\u0153\u00e6"
                + "\u00c0\u00c2\u00c4\u00c9\u00c8\u00ca\u00cb\u00cf\u00ce\u00d4\u00d6\u00d9\u00db\u00dc\u0178\u00c7\u0152\u00c6"
                + "\u00ab\u00bb\u2026\u2019\u2018\u201c\u201d\u2013\u2014\u2022\u00b7\u2665\u2605\u263e\u2726";
        BitmapFont f = g.generateFont(p);
        f.getData().setScale(scaleBack);
        f.getData().markupEnabled = false;
        return f;
    }

    /** Rounded rectangle with a soft inner fill and a luminous border. */
    private Texture roundedRect(int size, int radius, Color fill, Color border) {
        Pixmap pm = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        pm.setBlending(Pixmap.Blending.None);
        pm.setColor(0, 0, 0, 0);
        pm.fill();
        pm.setColor(fill);
        pm.fillRectangle(radius, 0, size - 2 * radius, size);
        pm.fillRectangle(0, radius, size, size - 2 * radius);
        pm.fillCircle(radius, radius, radius);
        pm.fillCircle(size - radius - 1, radius, radius);
        pm.fillCircle(radius, size - radius - 1, radius);
        pm.fillCircle(size - radius - 1, size - radius - 1, radius);
        pm.setColor(border);
        for (int i = 0; i < 2; i++) {
            pm.drawRectangle(i, i, size - 2 * i, size - 2 * i);
        }
        Texture t = new Texture(pm);
        t.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        pm.dispose();
        return t;
    }

    private Texture vignette(int size) {
        Pixmap pm = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        pm.setBlending(Pixmap.Blending.None);
        float c = (size - 1) / 2f;
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                float dx = (x - c) / c, dy = (y - c) / c;
                float r = (float) Math.sqrt(dx * dx + dy * dy) / 1.41421f;
                float a = (float) Math.pow(Math.max(0, r - 0.42f) / 0.58f, 1.8f) * 0.82f;
                pm.setColor(0f, 0f, 0.02f, a);
                pm.drawPixel(x, y);
            }
        }
        Texture t = new Texture(pm);
        t.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        pm.dispose();
        return t;
    }

    // ---- drawing helpers -------------------------------------------------
    public void rect(SpriteBatch b, float x, float y, float w, float h, Color c) {
        b.setColor(c);
        b.draw(white, x, y, w, h);
        b.setColor(Color.WHITE);
    }

    public void rect(SpriteBatch b, float x, float y, float w, float h, Color c, float alpha) {
        b.setColor(c.r, c.g, c.b, alpha);
        b.draw(white, x, y, w, h);
        b.setColor(Color.WHITE);
    }

    public void drawPanel(SpriteBatch b, float x, float y, float w, float h, float alpha) {
        panel.getColor().a = alpha;
        panel.draw(b, x, y, w, h);
        panel.getColor().a = 1f;
    }

    public void drawPanelLight(SpriteBatch b, float x, float y, float w, float h, float alpha) {
        panelLight.getColor().a = alpha;
        panelLight.draw(b, x, y, w, h);
        panelLight.getColor().a = 1f;
    }

    public void drawVignette(SpriteBatch b, float alpha) {
        b.setColor(1, 1, 1, alpha);
        b.draw(vignetteTex, 0, 0, W, H);
        b.setColor(Color.WHITE);
    }

    public float textWidth(BitmapFont f, String s) {
        layout.setText(f, s);
        return layout.width;
    }

    public void centered(SpriteBatch b, BitmapFont f, String s, float cx, float y) {
        layout.setText(f, s);
        f.draw(b, layout, cx - layout.width / 2f, y);
    }

    public float wrapped(SpriteBatch b, BitmapFont f, String s, float x, float y, float w) {
        layout.setText(f, s, f.getColor(), w, com.badlogic.gdx.utils.Align.left, true);
        f.draw(b, layout, x, y);
        return layout.height;
    }

    public float wrappedCentered(SpriteBatch b, BitmapFont f, String s, float cx, float y, float w) {
        layout.setText(f, s, f.getColor(), w, com.badlogic.gdx.utils.Align.center, true);
        f.draw(b, layout, cx - w / 2f, y);
        return layout.height;
    }

    public float measureWrapped(BitmapFont f, String s, float w) {
        layout.setText(f, s, f.getColor(), w, com.badlogic.gdx.utils.Align.left, true);
        return layout.height;
    }

    @Override
    public void dispose() {
        title.dispose();
        h1.dispose();
        body.dispose();
        small.dispose();
        script.dispose();
        genSerif.dispose();
        genBold.dispose();
        genItalic.dispose();
        genSans.dispose();
        white.dispose();
        panelTex.dispose();
        panelLightTex.dispose();
        vignetteTex.dispose();
    }
}
