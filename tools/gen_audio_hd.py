#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""HD lossless soundtrack for "Pour Lohen".

The shipped OGG tracks (tools/gen_audio.py) are short loops kept small for the
lightweight build. This generator produces the *studio* tier used by the full
build: long-form, through-composed arrangements of the same themes, rendered at
48 kHz / 24-bit stereo and stored as FLAC (lossless).

Everything is synthesised from scratch with numpy: no samples, no third-party
material, no licensing question. Each track is a genuinely different piece of
music -- different progression, melody, register and instrumentation -- and all
of them are played by the game (one per chapter, plus title, cutscene and
letter cues).

Usage:  python3 tools/gen_audio_hd.py [--fast]
Output: android/assets/audio/hd/<name>.flac
"""
import math
import os
import sys

import numpy as np
import soundfile as sf

SR = 48000
OUT = "android/assets/audio/hd"
FAST = "--fast" in sys.argv
rng = np.random.default_rng(1404)
os.makedirs(OUT, exist_ok=True)

NAMES = {'C': 0, 'C#': 1, 'D': 2, 'D#': 3, 'E': 4, 'F': 5,
         'F#': 6, 'G': 7, 'G#': 8, 'A': 9, 'A#': 10, 'B': 11}


def note_freq(name):
    name = (name.replace('Bb', 'A#').replace('Eb', 'D#').replace('Ab', 'G#')
                .replace('Db', 'C#').replace('Gb', 'F#'))
    n = NAMES[name[:-1]]
    octave = int(name[-1])
    return 440.0 * (2 ** ((n - 9) / 12 + (octave - 4)))


def env(n, a, d, r, sustain=0.6):
    a, d, r = max(1, int(a * SR)), max(1, int(d * SR)), max(1, int(r * SR))
    s_len = max(0, n - a - d - r)
    return np.concatenate([
        np.linspace(0, 1, a),
        np.linspace(1, sustain, d),
        np.full(s_len, sustain),
        np.linspace(sustain, 0, r),
    ])[:n]


def piano(freq, dur, amp=0.3, detune=0.0022):
    n = int(dur * SR)
    t = np.arange(n) / SR
    sig = np.zeros(n)
    for k, w in ((1, 1.0), (2, 0.42), (3, 0.19), (4, 0.11), (5, 0.06), (6, 0.03)):
        f = freq * k
        if f > SR / 2.2:
            break
        sig += w * np.sin(2 * np.pi * f * t + rng.uniform(0, 2 * np.pi))
        sig += 0.5 * w * np.sin(2 * np.pi * f * (1 + detune) * t)
    sig *= env(n, 0.006, dur * 0.3, dur * 0.65, 0.42)
    return sig * amp / 2.4


def bell(freq, dur, amp=0.25):
    n = int(dur * SR)
    t = np.arange(n) / SR
    sig = np.zeros(n)
    for k, w in ((1, 1.0), (2.76, 0.52), (5.4, 0.26), (8.93, 0.12)):
        f = freq * k
        if f > SR / 2.2:
            break
        sig += w * np.sin(2 * np.pi * f * t) * np.exp(-t * (1.4 + k * 0.5))
    return sig * amp / 1.8


def strings(freqs, dur, amp=0.1):
    """Slow bowed pad with gentle chorus."""
    n = int(dur * SR)
    t = np.arange(n) / SR
    sig = np.zeros(n)
    for f in freqs:
        for det in (-0.004, 0.0, 0.0045):
            vib = 1 + 0.0015 * np.sin(2 * np.pi * (4.3 + rng.uniform(-0.6, 0.6)) * t)
            sig += np.sin(2 * np.pi * f * (1 + det) * t * vib)
            sig += 0.3 * np.sin(2 * np.pi * 2 * f * (1 + det) * t)
    sig *= env(n, dur * 0.28, dur * 0.2, dur * 0.42, 0.85)
    return sig * amp / max(1, len(freqs) * 2)


def harp(freq, dur, amp=0.18):
    n = int(dur * SR)
    t = np.arange(n) / SR
    sig = (np.sin(2 * np.pi * freq * t) + 0.3 * np.sin(2 * np.pi * freq * 2 * t)
           + 0.12 * np.sin(2 * np.pi * freq * 3 * t))
    return sig * np.exp(-t * 2.6) * amp


def reverb(x, decay=2.6, wet=0.36):
    out = x.copy()
    for delay_ms, g in ((37, 0.80), (71, 0.68), (113, 0.56), (167, 0.46),
                        (223, 0.36), (311, 0.27)):
        d = int(SR * delay_ms / 1000.0)
        tail = np.zeros_like(x)
        tail[d:] = x[:-d] * g * math.exp(-delay_ms / (decay * 1000.0))
        out += tail * wet
    return out


def mix_into(buf, sig, at):
    i = int(at * SR)
    j = min(len(buf), i + len(sig))
    if i < len(buf):
        buf[i:j] += sig[:j - i]


def normalise(x, peak=0.86):
    m = np.max(np.abs(x))
    return x if m < 1e-9 else x * (peak / m)


def widen(mono_l, mono_r):
    """Build a stereo pair with a short Haas delay for natural width."""
    d = int(SR * 0.012)
    left = np.concatenate([mono_l, np.zeros(d)])
    right = np.concatenate([np.zeros(d), mono_r])
    return np.stack([left, right], axis=1)


def write(name, left, right, sr=SR):
    stereo = widen(left, right)
    stereo = normalise(stereo)
    path = os.path.join(OUT, name + ".flac")
    sf.write(path, stereo, sr, format='FLAC', subtype='PCM_24')
    size = os.path.getsize(path)
    print("  %-22s %6.1f s  %6.1f MiB" % (name, len(stereo) / sr, size / 1048576), flush=True)
    return size


# ------------------------------------------------------------------ composer
def compose(sections, style="piano", bass=True, decay=2.8, swell=True, reps=3):
    """sections: list of (chords, bars_seconds, melody) blocks played in order.

    The block list is played `reps` times, but never identically: each pass
    changes the melodic register, the arpeggio density and the bass voicing,
    so a 4-minute cue is four minutes of distinct audio, not a loop repeated.
    """
    total = sum(len(ch) * dur for ch, dur, _ in sections) * reps + 6
    if FAST:
        total = min(total, 20)
    buf_l = np.zeros(int(total * SR))
    buf_r = np.zeros(int(total * SR))
    t = 0.0
    voice = {"piano": piano, "bell": bell, "harp": harp}[style]
    passes = [(0, bass, 10), (12, not bass, 8), (-12, bass, 12), (12, bass, 10)]
    for rep in range(reps):
        semis, use_bass, steps = passes[rep % len(passes)]
        shift = 2 ** (semis / 12.0)
        for chords, dur, melody in sections:
            for ci, chord in enumerate(chords):
                if t > total - 2:
                    break
                freqs = [note_freq(n) for n in chord]
                padsig = strings(freqs, dur + 1.6, 0.13 if swell else 0.09)
                mix_into(buf_l, padsig, t)
                mix_into(buf_r, padsig * 0.94, t + 0.004)
                for st in range(steps):
                    f = freqs[st % len(freqs)] * (2 if st >= len(freqs) * 1.6 else 1)
                    sig = voice(f, dur / steps * 3.4, 0.15)
                    pan = 0.5 + 0.38 * math.sin(st * 1.1 + rep)
                    mix_into(buf_l, sig * (1 - pan * 0.5), t + st * dur / steps)
                    mix_into(buf_r, sig * (0.5 + pan * 0.5), t + st * dur / steps)
                if use_bass:
                    b = piano(freqs[0] / 2, dur * 0.95, 0.2)
                    mix_into(buf_l, b, t)
                    mix_into(buf_r, b, t)
                if melody and ci < len(melody):
                    mt = t
                    for (nname, nd) in melody[ci]:
                        if nname:
                            m = voice(note_freq(nname) * shift, nd * 2.1, 0.27)
                            mix_into(buf_l, m * 0.92, mt)
                            mix_into(buf_r, m, mt + 0.006)
                        mt += nd
                t += dur
    return reverb(buf_l, decay, 0.38), reverb(buf_r, decay * 1.05, 0.38)


Dm = ["D3", "A3", "F4"]
TRACKS = []


def add(name, builder):
    TRACKS.append((name, builder))


add("hd_title", lambda: compose([
    ([["D3", "A3", "F#4"], ["B2", "F#3", "D4"], ["G2", "D3", "B3"], ["A2", "E3", "C#4"]], 4.2,
     [[("F#5", 1.2), ("E5", 0.6), ("D5", 1.6), (None, 0.8)],
      [("D5", 1.2), ("C#5", 0.6), ("B4", 1.6), (None, 0.8)],
      [("B4", 1.2), ("D5", 1.2), ("G5", 1.2), (None, 0.6)],
      [("F#5", 1.8), ("E5", 0.6), ("C#5", 1.8)]]),
    ([["G2", "D3", "B3"], ["D3", "A3", "F#4"], ["E3", "B3", "G4"], ["A2", "E3", "C#4"],
      ["D3", "A3", "F#4"], ["B2", "F#3", "D4"], ["G2", "D3", "B3"], ["A2", "E3", "C#4"]], 4.2,
     [[("B4", 2.0), ("D5", 2.0)], [("A4", 2.0), ("F#5", 2.0)],
      [("G5", 1.4), ("F#5", 1.4), ("E5", 1.4)], [("E5", 2.2), ("C#5", 2.0)],
      [("D5", 2.4), ("A4", 1.8)], [("F#5", 2.0), ("D5", 2.0)],
      [("G5", 1.6), ("B5", 1.6), ("A5", 1.0)], [("F#5", 3.2), (None, 1.0)]]),
], decay=3.0))

add("hd_ch1_seuil", lambda: compose([
    ([["A2", "E3", "C4"], ["F2", "C3", "A3"], ["G2", "D3", "B3"], ["E3", "B3", "G#4"]], 4.8,
     [[("E5", 1.8), ("C5", 1.2), (None, 1.8)], [("A4", 1.8), ("C5", 1.2), (None, 1.8)],
      [("D5", 1.4), ("B4", 1.4), ("G4", 2.0)], [("B4", 2.4), ("G#4", 2.4)]]),
    ([["A2", "E3", "C4"], ["D3", "A3", "F4"], ["F2", "C3", "A3"], ["E3", "B3", "G#4"],
      ["A2", "E3", "C4"], ["G2", "D3", "B3"]], 4.8,
     [[("C5", 2.4), ("E5", 2.4)], [("F5", 2.0), ("D5", 2.4)],
      [("A5", 2.0), ("E5", 2.4)], [("G#4", 3.0), (None, 1.6)],
      [("E5", 2.2), ("A4", 2.2)], [("B4", 2.6), (None, 2.0)]]),
], decay=3.4))

add("hd_ch2_jardin", lambda: compose([
    ([["A2", "E3", "C4"], ["F2", "C3", "A3"], ["D3", "A3", "F4"], ["E3", "B3", "G#4"]], 4.5,
     [[("E5", 1.5), ("C5", 1.0), (None, 2.0)], [("A4", 1.5), ("C5", 1.0), (None, 2.0)],
      [("D5", 1.0), ("F5", 1.0), ("E5", 1.5), (None, 1.0)], [("B4", 2.0), ("G#4", 2.0)]]),
    ([["C3", "G3", "E4"], ["A2", "E3", "C4"], ["F2", "C3", "A3"], ["G2", "D3", "B3"],
      ["A2", "E3", "C4"], ["E3", "B3", "G#4"], ["A2", "E3", "C4"]], 4.5,
     [[("G5", 1.6), ("E5", 1.6), (None, 1.2)], [("E5", 1.6), ("C5", 1.6), (None, 1.2)],
      [("C5", 1.8), ("A4", 1.8)], [("D5", 1.8), ("B4", 1.8)],
      [("E5", 2.4), ("A5", 1.8)], [("G#5", 2.0), ("B4", 2.0)], [("A4", 3.4), (None, 1.0)]]),
], decay=3.2))

add("hd_ch3_biblio", lambda: compose([
    ([["C4", "G4", "E5"], ["A3", "E4", "C5"], ["F3", "C4", "A4"], ["G3", "D4", "B4"]], 3.8,
     [[("G5", 1.0), ("E5", 1.0), ("C6", 1.8)], [("A5", 1.0), ("G5", 1.0), ("E5", 1.8)],
      [("F5", 1.0), ("A5", 1.0), ("C6", 1.8)], [("B5", 1.9), ("G5", 1.9)]]),
    ([["E4", "B4", "G5"], ["C4", "G4", "E5"], ["D4", "A4", "F5"], ["G3", "D4", "B4"],
      ["C4", "G4", "E5"], ["A3", "E4", "C5"]], 3.8,
     [[("B5", 1.9), ("G5", 1.9)], [("C6", 1.9), ("E5", 1.9)],
      [("D6", 1.6), ("A5", 1.2), ("F5", 1.0)], [("B5", 2.0), ("D6", 1.8)],
      [("G5", 2.0), ("C6", 1.8)], [("E5", 3.8)]]),
], style="bell", bass=False, decay=3.6))

add("hd_ch4_ciel", lambda: compose([
    ([["D3", "A3", "F#4"], ["A2", "E3", "C#4"], ["B2", "F#3", "D4"], ["G2", "D3", "B3"]], 5.2,
     [[("A5", 2.4), ("F#5", 2.4)], [("E5", 2.4), ("C#5", 2.4)],
      [("D5", 2.6), ("F#5", 2.2)], [("B4", 3.0), ("G4", 2.0)]]),
    ([["D3", "A3", "F#4"], ["F#3", "C#4", "A4"], ["G2", "D3", "B3"], ["A2", "E3", "C#4"],
      ["D3", "A3", "F#4"]], 5.2,
     [[("D6", 2.6), ("A5", 2.4)], [("C#6", 2.6), ("F#5", 2.4)],
      [("B5", 2.6), ("D6", 2.2)], [("A5", 3.0), ("E5", 2.0)], [("F#5", 4.0), (None, 1.0)]]),
], style="harp", decay=4.0))

add("hd_ch5_musique", lambda: compose([
    ([["C4", "G4", "E5"], ["F3", "C4", "A4"], ["G3", "D4", "B4"], ["C4", "G4", "E5"]], 3.4,
     [[("E6", 0.9), ("C6", 0.9), ("G5", 1.6)], [("F6", 0.9), ("C6", 0.9), ("A5", 1.6)],
      [("D6", 0.9), ("B5", 0.9), ("G5", 1.6)], [("C6", 1.7), ("E6", 1.7)]]),
    ([["A3", "E4", "C5"], ["F3", "C4", "A4"], ["D4", "A4", "F5"], ["G3", "D4", "B4"],
      ["C4", "G4", "E5"], ["E4", "B4", "G5"], ["C4", "G4", "E5"]], 3.4,
     [[("C6", 1.7), ("A5", 1.7)], [("A5", 1.7), ("F5", 1.7)],
      [("F6", 1.1), ("D6", 1.1), ("A5", 1.2)], [("B5", 1.7), ("G5", 1.7)],
      [("E6", 1.7), ("C6", 1.7)], [("G6", 1.7), ("B5", 1.7)], [("C6", 3.4)]]),
], style="bell", bass=False, decay=3.2))

add("hd_ch6_phare", lambda: compose([
    ([["F2", "C3", "A3"], ["Bb2", "F3", "D4"], ["C3", "G3", "E4"], ["A2", "E3", "C4"]], 5.0,
     [[("A4", 2.2), ("C5", 1.6), (None, 1.2)], [("D5", 2.2), ("C5", 1.6), (None, 1.2)],
      [("E5", 2.2), ("G5", 1.6), (None, 1.2)], [("E5", 1.8), ("C5", 1.6), ("A4", 1.6)]]),
    ([["D3", "A3", "F4"], ["Bb2", "F3", "D4"], ["C3", "G3", "E4"], ["F2", "C3", "A3"],
      ["Bb2", "F3", "D4"], ["C3", "G3", "E4"], ["F2", "C3", "A3"]], 5.0,
     [[("F5", 2.4), ("E5", 2.2)], [("D5", 2.8), ("F5", 2.0)],
      [("G5", 2.4), ("E5", 2.4)], [("F5", 3.2), ("C5", 1.8)],
      [("D5", 2.6), ("A5", 2.2)], [("C6", 2.6), ("G5", 2.2)], [("F5", 4.2), (None, 0.8)]]),
], decay=4.2))

add("hd_letter", lambda: compose([
    ([["F2", "C3", "A3"], ["Bb2", "F3", "D4"], ["C3", "G3", "E4"], ["A2", "E3", "C4"],
      ["D3", "A3", "F4"], ["Bb2", "F3", "D4"], ["C3", "G3", "E4"], ["F2", "C3", "A3"]], 5.4,
     [[("A4", 2.2), ("C5", 1.7), (None, 1.5)], [("D5", 2.2), ("C5", 1.7), (None, 1.5)],
      [("E5", 2.2), ("G5", 1.7), (None, 1.5)], [("E5", 1.7), ("C5", 1.7), ("A4", 2.0)],
      [("F5", 2.2), ("E5", 1.7), (None, 1.5)], [("D5", 2.7), ("F5", 1.7), (None, 1.0)],
      [("G5", 2.2), ("E5", 2.2), (None, 1.0)], [("F5", 3.4), (None, 2.0)]]),
    ([["F2", "C3", "A3"], ["A2", "E3", "C4"], ["Bb2", "F3", "D4"], ["C3", "G3", "E4"],
      ["F2", "C3", "A3"], ["Bb2", "F3", "D4"], ["F2", "C3", "A3"]], 5.4,
     [[("C6", 2.6), ("A5", 2.4)], [("A5", 2.6), ("E5", 2.4)],
      [("D6", 2.6), ("F5", 2.4)], [("E6", 2.6), ("G5", 2.4)],
      [("C6", 2.8), ("F5", 2.4)], [("D6", 2.8), ("A5", 2.4)], [("F5", 4.4), (None, 1.0)]]),
], decay=4.4))

add("hd_cutscene_night", lambda: compose([
    ([["A2", "E3", "C4"], ["G2", "D3", "B3"], ["F2", "C3", "A3"], ["E3", "B3", "G#4"],
      ["A2", "E3", "C4"], ["D3", "A3", "F4"]], 5.6,
     [[("E5", 2.8), ("A4", 2.6)], [("D5", 2.8), ("B4", 2.6)],
      [("C5", 2.8), ("A4", 2.6)], [("G#4", 3.2), ("B4", 2.2)],
      [("E5", 3.0), ("C5", 2.4)], [("A4", 4.4), (None, 1.0)]]),
], style="harp", decay=4.6))

add("hd_cutscene_dawn", lambda: compose([
    ([["D3", "A3", "F#4"], ["G2", "D3", "B3"], ["A2", "E3", "C#4"], ["D3", "A3", "F#4"],
      ["B2", "F#3", "D4"], ["G2", "D3", "B3"], ["D3", "A3", "F#4"]], 5.6,
     [[("F#5", 2.8), ("A5", 2.6)], [("B5", 2.8), ("G5", 2.6)],
      [("A5", 2.8), ("E5", 2.6)], [("D6", 3.0), ("F#5", 2.4)],
      [("D5", 2.8), ("B4", 2.6)], [("G5", 3.0), ("B5", 2.4)], [("D5", 4.4), (None, 1.0)]]),
], decay=4.2))


def main():
    total = 0
    print("HD soundtrack -> %s (48 kHz / 24-bit FLAC)" % OUT)
    for name, builder in TRACKS:
        l, r = builder()
        total += write(name, l, r)
    print("TOTAL hd audio: %.1f MiB" % (total / 1048576))


if __name__ == "__main__":
    main()
