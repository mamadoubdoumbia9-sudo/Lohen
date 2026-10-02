#!/usr/bin/env python3
"""Pre-rendered cutscene renderer for "Pour Lohen".

Each chapter opens with a real animated cutscene. libGDX has no built-in video
decoder on Android, so the cutscenes ship as frame sequences that the game
streams at 15 fps (see core/.../screens/CutsceneScreen.java).

Every frame is genuinely different: slow camera travelling with parallax
(the illustration is split into depth slices), animated volumetric light,
breathing colour grade, drifting particles and a cross-dissolve to the next
illustration. Nothing is duplicated, nothing is padding.

Usage:  python3 tools/gen_cutscenes.py [--fast]
Output: android/assets/cutscene/<id>/f####.jpg  + manifest.json
"""
import json
import math
import os
import sys

import numpy as np
from PIL import Image, ImageEnhance, ImageFilter

SRC = "art/src"
OUT = "android/assets/cutscene"
W, H = 1920, 1080
FPS = 20
QUALITY = 96
FAST = "--fast" in sys.argv

rng = np.random.default_rng(7)

# id, source illustration(s), seconds, camera move, mood
SCENES = [
    ("intro",  ["bg_title.png", "bg_ch1_seuil.png"], 14, (0.00, 0.06, 1.14, 1.00), "night"),
    ("ch2",    ["bg_ch1_seuil.png", "bg_ch2_jardin.png"], 13, (-0.05, 0.00, 1.10, 1.18), "night"),
    ("ch3",    ["bg_ch2_jardin.png", "bg_ch3_biblio.png"], 13, (0.06, -0.04, 1.16, 1.02), "warm"),
    ("ch4",    ["bg_ch3_biblio.png", "bg_ch4_ciel.png"], 14, (0.00, -0.08, 1.04, 1.20), "night"),
    ("ch5",    ["bg_ch4_ciel.png", "bg_ch5_musique.png"], 13, (-0.06, 0.03, 1.18, 1.04), "warm"),
    ("ch6",    ["bg_ch5_musique.png", "bg_ch6_phare.png"], 14, (0.04, 0.05, 1.02, 1.20), "dawn"),
    ("letter", ["bg_ch6_phare.png", "paper_letter.png"], 15, (0.00, 0.00, 1.20, 1.00), "dawn"),
]


# Narration shown over each cutscene. Timings in seconds from the first frame.
CAPTIONS = {
    "intro": [[1.4, "Il y a des maisons qui n'existent que la nuit."],
              [5.6, "Celle-ci t'attendait depuis longtemps."],
              [10.0, "Pousse la porte, Lohen."]],
    "ch2": [[1.2, "Derrière le seuil, un jardin que personne n'a jamais vu."],
            [6.0, "Les lanternes se souviennent de l'ordre des soirs."]],
    "ch3": [[1.2, "Plus loin, une bibliothèque où chaque livre est un souvenir."],
            [6.4, "Il en manque un. Celui qu'il n'a jamais osé écrire."]],
    "ch4": [[1.2, "Le toit s'ouvre. Le ciel descend."],
            [6.0, "Tu te souviens de cette nuit-là ? Lui, oui."]],
    "ch5": [[1.2, "Une boîte à musique, laissée ouverte."],
            [6.2, "Six notes, toujours les mêmes, depuis des mois."]],
    "ch6": [[1.2, "Au bout du chemin, le phare s'allume enfin."],
            [6.0, "Quatre symboles. Une seule serrure."],
            [10.4, "Il est presque minuit."]],
    "letter": [[1.6, "Dans le coffre, il n'y avait pas de trésor."],
               [6.4, "Juste une feuille pliée en quatre,"],
               [10.2, "et une écriture que tu reconnais."]],
}

GRADES = {
    "night": ((0.86, 0.90, 1.12), 0.92),
    "warm":  ((1.10, 1.00, 0.88), 1.00),
    "dawn":  ((1.12, 1.02, 0.95), 1.06),
}


def load(name):
    im = Image.open(os.path.join(SRC, name)).convert("RGB")
    # crop to 16:9 then work at 1.35x so the camera has room to travel
    w, h = im.size
    target = W / H
    if w / h > target:
        nw = int(h * target)
        im = im.crop(((w - nw) // 2, 0, (w - nw) // 2 + nw, h))
    else:
        nh = int(w / target)
        im = im.crop((0, (h - nh) // 2, w, (h - nh) // 2 + nh))
    return im.resize((int(W * 1.35), int(H * 1.35)), Image.LANCZOS)


def depth_slices(im):
    """Split an illustration into three depth planes (far / mid / near).

    Depth is approximated from luminance + vertical position, which works well
    on these night paintings: the sky is far, the foreground is dark and low.
    """
    a = np.asarray(im).astype(np.float32) / 255.0
    lum = a @ np.array([0.299, 0.587, 0.114], dtype=np.float32)
    hgt, wid = lum.shape
    yy = np.linspace(1.0, 0.0, hgt, dtype=np.float32)[:, None]
    depth = np.clip(0.55 * lum + 0.45 * yy, 0, 1)
    depth = np.asarray(Image.fromarray((depth * 255).astype(np.uint8)).filter(
        ImageFilter.GaussianBlur(14))).astype(np.float32) / 255.0
    planes = []
    for lo, hi in ((0.62, 1.01), (0.34, 0.62), (-0.01, 0.34)):
        m = np.clip((depth - lo) / max(1e-3, (hi - lo)), 0, 1)
        m = np.minimum(m, np.clip((hi - depth) / 0.12 + 1, 0, 1))
        mask = Image.fromarray((m * 255).astype(np.uint8)).filter(ImageFilter.GaussianBlur(8))
        layer = im.copy()
        layer.putalpha(mask)
        planes.append(layer)
    return planes


def light_overlay(size, t, mood):
    """Animated volumetric light: a slowly breathing warm pool + soft rays."""
    w, h = size
    sw, sh = w // 6, h // 6
    yy, xx = np.mgrid[0:sh, 0:sw].astype(np.float32)
    cx = sw * (0.5 + 0.18 * math.sin(t * 0.6))
    cy = sh * (0.34 + 0.07 * math.cos(t * 0.45))
    r = np.sqrt(((xx - cx) / (sw * 0.62)) ** 2 + ((yy - cy) / (sh * 0.62)) ** 2)
    glow = np.clip(1.0 - r, 0, 1) ** 2.1
    rays = 0.5 + 0.5 * np.sin(np.arctan2(yy - cy, xx - cx) * 7.0 + t * 0.9)
    field = glow * (0.72 + 0.28 * rays)
    field *= 0.55 + 0.1 * math.sin(t * 1.7)
    col = (255, 214, 158) if mood != "night" else (190, 206, 255)
    arr = np.zeros((sh, sw, 4), np.uint8)
    arr[..., 0], arr[..., 1], arr[..., 2] = col
    arr[..., 3] = (np.clip(field, 0, 1) * 150).astype(np.uint8)
    return Image.fromarray(arr).resize((w, h), Image.BILINEAR)


class Particles:
    def __init__(self, n, w, h, kind):
        self.p = np.zeros((n, 5), np.float32)       # x, y, vx, vy, size
        self.p[:, 0] = rng.uniform(0, w, n)
        self.p[:, 1] = rng.uniform(0, h, n)
        self.kind = kind
        if kind == "petal":
            self.p[:, 2] = rng.uniform(-26, -8, n)
            self.p[:, 3] = rng.uniform(10, 26, n)
            self.p[:, 4] = rng.uniform(5, 11, n)
        else:
            self.p[:, 2] = rng.uniform(-7, 7, n)
            self.p[:, 3] = rng.uniform(-9, 3, n)
            self.p[:, 4] = rng.uniform(2.5, 6.5, n)
        self.w, self.h = w, h
        self.phase = rng.uniform(0, 6.28, n)

    def step(self, dt, t):
        self.p[:, 0] += (self.p[:, 2] + np.sin(self.phase + t * 2.0) * 9) * dt
        self.p[:, 1] += self.p[:, 3] * dt
        self.p[:, 0] %= self.w
        self.p[:, 1] %= self.h

    def draw(self, img, t):
        d = np.asarray(img).astype(np.float32)
        for i in range(self.p.shape[0]):
            x, y, _, _, s = self.p[i]
            tw = 0.45 + 0.55 * math.sin(self.phase[i] + t * 3.1)
            x0, y0 = int(x - s), int(y - s)
            x1, y1 = int(x + s), int(y + s)
            if x0 < 0 or y0 < 0 or x1 >= d.shape[1] or y1 >= d.shape[0]:
                continue
            yy, xx = np.mgrid[y0:y1, x0:x1]
            r = np.sqrt((xx - x) ** 2 + (yy - y) ** 2) / max(1e-3, s)
            a = (np.clip(1 - r, 0, 1) ** 2)[..., None] * tw
            col = np.array([255, 226, 170], np.float32) if self.kind != "petal" \
                else np.array([248, 196, 208], np.float32)
            d[y0:y1, x0:x1] = d[y0:y1, x0:x1] * (1 - a) + col * a
        return Image.fromarray(np.clip(d, 0, 255).astype(np.uint8))


def ease(t):
    return t * t * (3 - 2 * t)


def render_scene(sid, sources, seconds, cam, mood):
    frames = max(2, int(seconds * FPS))
    if FAST:
        frames = min(frames, 6)
    outdir = os.path.join(OUT, sid)
    os.makedirs(outdir, exist_ok=True)

    imgs = [load(s) for s in sources]
    planes = [depth_slices(im) for im in imgs]
    tint, exposure = GRADES[mood]
    dx0, dy0, z0, z1 = cam
    parts = Particles(70 if not FAST else 10, W, H, "petal" if mood == "warm" else "dust")

    written = 0
    for f in range(frames):
        t = f / FPS
        u = ease(f / max(1, frames - 1))
        zoom = z0 + (z1 - z0) * u
        base = Image.new("RGB", (W, H), (6, 5, 12))

        # cross-dissolve: first illustration for 60% of the shot, then blend
        blend = 0.0 if len(imgs) < 2 else np.clip((u - 0.55) / 0.42, 0, 1)

        for which, plane_set in enumerate(planes):
            weight = (1 - blend) if which == 0 else blend
            if weight <= 0.003:
                continue
            comp = Image.new("RGBA", (W, H), (0, 0, 0, 0))
            for depth_i, layer in enumerate(plane_set):
                # nearer planes travel further: real parallax
                par = 1.0 + 0.55 * depth_i
                z = zoom * (1.0 + 0.04 * depth_i)
                lw, lh = int(W * z * 1.0), int(H * z * 1.0)
                sized = layer.resize((lw, lh), Image.BILINEAR)
                ox = (W - lw) / 2 + dx0 * W * par * (u - 0.5) * 2
                oy = (H - lh) / 2 + dy0 * H * par * (u - 0.5) * 2
                comp.alpha_composite(sized, (int(ox), int(oy)))
            if weight >= 0.997:
                base = Image.alpha_composite(base.convert("RGBA"), comp).convert("RGB")
            else:
                cur = Image.alpha_composite(base.convert("RGBA"), comp).convert("RGB")
                base = Image.blend(base, cur, float(weight))

        base = Image.alpha_composite(base.convert("RGBA"),
                                     light_overlay((W, H), t, mood)).convert("RGB")

        arr = np.asarray(base).astype(np.float32)
        arr *= np.array(tint, np.float32)
        arr *= exposure * (0.97 + 0.03 * math.sin(t * 0.8))
        base = Image.fromarray(np.clip(arr, 0, 255).astype(np.uint8))
        base = ImageEnhance.Contrast(base).enhance(1.04)

        parts.step(1.0 / FPS, t)
        base = parts.draw(base, t)

        # cinematic fade in / out
        fade_frames = max(2.0, min(FPS * 1.1, frames * 0.22))
        fade = min(1.0, f / fade_frames, (frames - 1 - f) / fade_frames)
        if fade < 1.0:
            base = Image.blend(Image.new("RGB", (W, H), (6, 5, 12)), base, float(max(0.0, fade)))

        base.save(os.path.join(outdir, "f%04d.jpg" % f), quality=QUALITY, optimize=True,
                  progressive=False)
        written += 1
        if f % 30 == 0:
            print(f"  {sid}: {f}/{frames}", flush=True)
    return written


def main():
    os.makedirs(OUT, exist_ok=True)
    manifest = {"fps": FPS, "width": W, "height": H, "scenes": {}}
    total_bytes = 0
    for sid, sources, seconds, cam, mood in SCENES:
        print(f"== cutscene {sid} ({seconds}s)", flush=True)
        n = render_scene(sid, sources, seconds, cam, mood)
        size = sum(os.path.getsize(os.path.join(OUT, sid, f))
                   for f in os.listdir(os.path.join(OUT, sid)))
        total_bytes += size
        manifest["scenes"][sid] = {"frames": n, "seconds": round(n / FPS, 2),
                                   "bytes": size, "sources": sources,
                                   "captions": CAPTIONS.get(sid, [])}
        print(f"   -> {n} frames, {size/1048576:.1f} MiB", flush=True)
    with open(os.path.join(OUT, "manifest.json"), "w") as fh:
        json.dump(manifest, fh, indent=2)
    print(f"TOTAL cutscenes: {total_bytes/1048576:.1f} MiB")


if __name__ == "__main__":
    main()
