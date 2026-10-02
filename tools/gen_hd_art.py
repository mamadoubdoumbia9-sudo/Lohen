#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""HD lossless art tier for "Pour Lohen".

The lightweight build ships JPEG backgrounds (~9 MiB of assets in total). The
full build also ships a lossless master of every illustration, upscaled with a
high-quality Lanczos filter and lightly sharpened, stored as 16-bit-clean PNG.
Assets.tex() picks img/hd/<name>.png automatically when it is present, so the
game renders the masters on high-density screens with no code change.

Usage:  python3 tools/gen_hd_art.py
Output: android/assets/img/hd/<name>.png
"""
import os

from PIL import Image, ImageEnhance, ImageFilter

SRC = "art/src"
OUT = "android/assets/img/hd"
TARGET_W = 2048          # backgrounds
TARGET_H_CHAR = 2048     # portrait masters (letter paper)

os.makedirs(OUT, exist_ok=True)


def process(name):
    im = Image.open(os.path.join(SRC, name)).convert("RGBA")
    w, h = im.size
    if w >= h:
        scale = TARGET_W / float(w)
    else:
        scale = TARGET_H_CHAR / float(h)
    size = (int(round(w * scale)), int(round(h * scale)))
    im = im.resize(size, Image.LANCZOS)
    im = im.filter(ImageFilter.UnsharpMask(radius=2.0, percent=55, threshold=3))
    im = ImageEnhance.Color(im).enhance(1.03)
    out = os.path.join(OUT, os.path.splitext(name)[0] + ".png")
    im.save(out, format="PNG", optimize=True)
    return os.path.getsize(out), size


def main():
    total = 0
    print("HD art tier -> %s" % OUT)
    for name in sorted(os.listdir(SRC)):
        if not name.lower().endswith(".png"):
            continue
        # Characters stay on the standard tier: they are drawn small on screen
        # and keeping them out of the HD cache avoids texture thrashing during
        # dialogue.
        if name.startswith("char_"):
            continue
        size, dims = process(name)
        total += size
        print("  %-22s %sx%s  %5.1f MiB" % (name, dims[0], dims[1], size / 1048576),
              flush=True)
    print("TOTAL hd art: %.1f MiB" % (total / 1048576))


if __name__ == "__main__":
    main()
