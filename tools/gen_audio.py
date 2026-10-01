"""Original soundtrack & SFX generator for 'Pour Lohen'.
Everything here is synthesised from scratch with numpy -> 100% original content,
no third-party samples, no licensing issues. Output: OGG/Vorbis (libGDX friendly).
"""
import numpy as np, soundfile as sf, os, math

SR = 44100
OUT = "android/assets/audio"
os.makedirs(OUT, exist_ok=True)
rng = np.random.default_rng(1404)

def note_freq(name):
    name = name.replace('Bb','A#').replace('Eb','D#').replace('Ab','G#').replace('Db','C#').replace('Gb','F#')
    names = {'C':0,'C#':1,'D':2,'D#':3,'E':4,'F':5,'F#':6,'G':7,'G#':8,'A':9,'A#':10,'B':11}
    n = names[name[:-1]]; octave = int(name[-1])
    return 440.0 * (2 ** ((n - 9) / 12 + (octave - 4)))

def env(n, a, d, s, r, sustain=0.6):
    a, d, r = max(1, int(a*SR)), max(1, int(d*SR)), max(1, int(r*SR))
    s_len = max(0, n - a - d - r)
    out = np.concatenate([
        np.linspace(0, 1, a),
        np.linspace(1, sustain, d),
        np.full(s_len, sustain),
        np.linspace(sustain, 0, r)])
    return out[:n] if len(out) >= n else np.pad(out, (0, n - len(out)))

def piano(freq, dur, amp=0.3, detune=0.002):
    n = int(dur * SR); t = np.arange(n) / SR
    harm = [(1, 1.0), (2, 0.42), (3, 0.22), (4, 0.11), (5, 0.06), (6, 0.03)]
    sig = np.zeros(n)
    for h, a in harm:
        decay = np.exp(-t * (1.6 + 0.55 * h))
        sig += a * decay * np.sin(2 * np.pi * freq * h * t + rng.random() * 6.28)
        sig += a * 0.5 * decay * np.sin(2 * np.pi * freq * h * (1 + detune) * t)
    sig *= env(n, 0.004, 0.08, 0.0, min(0.4, dur * 0.5), 0.35)
    return amp * sig / 2.4

def bell(freq, dur, amp=0.25):
    n = int(dur * SR); t = np.arange(n) / SR
    mod = np.sin(2 * np.pi * freq * 2.76 * t) * np.exp(-t * 3.2) * 3.0
    sig = np.sin(2 * np.pi * freq * t + mod) * np.exp(-t * 2.1)
    sig += 0.3 * np.sin(2 * np.pi * freq * 3.01 * t) * np.exp(-t * 4.5)
    return amp * sig

def pad(freqs, dur, amp=0.12):
    n = int(dur * SR); t = np.arange(n) / SR
    sig = np.zeros(n)
    for f in freqs:
        for d in (-0.004, 0.0, 0.005):
            lfo = 1 + 0.0015 * np.sin(2 * np.pi * 0.22 * t + f)
            sig += np.sin(2 * np.pi * f * (1 + d) * t * lfo)
            sig += 0.3 * np.sin(2 * np.pi * f * 2 * (1 + d) * t)
    sig /= (len(freqs) * 3)
    sig *= env(n, 1.4, 0.8, 0.0, 1.6, 0.85)
    return amp * sig

def reverb(x, decay=2.2, wet=0.33):
    ir_len = int(SR * decay)
    ir = rng.normal(0, 1, ir_len) * np.exp(-np.arange(ir_len) / (SR * decay / 4.5))
    ir[:int(SR*0.01)] *= np.linspace(0, 1, int(SR*0.01))
    ir /= np.abs(ir).sum() / 12
    n = len(x) + ir_len
    N = 1 << (n - 1).bit_length()
    y = np.fft.irfft(np.fft.rfft(x, N) * np.fft.rfft(ir, N), N)[:len(x)]
    return (1 - wet) * x + wet * y

def mix_into(buf, sig, at):
    i = int(at * SR)
    end = min(len(buf), i + len(sig))
    if end > i:
        buf[i:end] += sig[:end - i]

def normalise(x, peak=0.82):
    m = np.max(np.abs(x))
    return x * (peak / m) if m > 0 else x

def loopify(x, fade=1.5):
    f = int(fade * SR)
    x = x.copy()
    head = x[:f] * np.linspace(0, 1, f)
    x[:f] = head + x[-f:] * np.linspace(1, 0, f)
    return x[:-f]

def write(name, data, sr=SR):
    data = normalise(data)
    path = f"{OUT}/{name}.ogg"
    sf.write(path, data.astype(np.float32), sr, format='OGG', subtype='VORBIS')
    print(name, round(os.path.getsize(path)/1024), "KB", round(len(data)/sr, 1), "s")

# ---------------------------------------------------------------- music
def track(prog, dur_per_chord, melody, style="piano", bass=True, reverb_decay=2.4):
    total = len(prog) * dur_per_chord + 3
    buf = np.zeros(int(total * SR))
    t = 0.0
    for ci, chord in enumerate(prog):
        freqs = [note_freq(n) for n in chord]
        mix_into(buf, pad(freqs, dur_per_chord + 1.2), t)
        # arpeggio
        steps = 8
        for s in range(steps):
            f = freqs[s % len(freqs)] * (2 if s >= len(freqs) * 1.5 else 1)
            gen = piano if style == "piano" else bell
            mix_into(buf, gen(f, dur_per_chord / steps * 3.2, 0.16), t + s * dur_per_chord / steps)
        if bass:
            mix_into(buf, piano(freqs[0] / 2, dur_per_chord * 0.9, 0.22), t)
        # melody line
        if melody and ci < len(melody):
            mt = t
            for (nname, nd) in melody[ci]:
                if nname:
                    gen = piano if style == "piano" else bell
                    mix_into(buf, gen(note_freq(nname), nd * 1.9, 0.26), mt)
                mt += nd
        t += dur_per_chord
    return loopify(reverb(buf, reverb_decay, 0.38))

# Title: D major, hopeful and open
write("music_title", track(
    [["D3","A3","F#4"], ["B2","F#3","D4"], ["G2","D3","B3"], ["A2","E3","C#4"]] * 2,
    4.0,
    [[("F#5",1.0),("E5",0.5),("D5",1.5),(None,1.0)],
     [("D5",1.0),("C#5",0.5),("B4",1.5),(None,1.0)],
     [("B4",1.0),("D5",1.0),("G5",1.0),(None,1.0)],
     [("F#5",1.5),("E5",0.5),("C#5",2.0)]] * 2))

# Night garden: A minor, mysterious
write("music_night", track(
    [["A2","E3","C4"], ["F2","C3","A3"], ["D3","A3","F4"], ["E3","B3","G#4"]] * 2,
    4.5,
    [[("E5",1.5),("C5",1.0),(None,2.0)],
     [("A4",1.5),("C5",1.0),(None,2.0)],
     [("D5",1.0),("F5",1.0),("E5",1.5),(None,1.0)],
     [("B4",2.0),("G#4",2.0)]] * 2, reverb_decay=3.0))

# Memory: music box bells, C major
write("music_memory", track(
    [["C4","G4","E5"], ["A3","E4","C5"], ["F3","C4","A4"], ["G3","D4","B4"]] * 2,
    3.6,
    [[("G5",0.9),("E5",0.9),("C6",1.8)],
     [("A5",0.9),("G5",0.9),("E5",1.8)],
     [("F5",0.9),("A5",0.9),("C6",1.8)],
     [("B5",1.8),("G5",1.8)]] * 2, style="bell", bass=False))

# Letter: F major, warm and tender, slow
write("music_letter", track(
    [["F2","C3","A3"], ["Bb2","F3","D4"], ["C3","G3","E4"], ["A2","E3","C4"],
     ["D3","A3","F4"], ["Bb2","F3","D4"], ["C3","G3","E4"], ["F2","C3","A3"]],
    5.0,
    [[("A4",2.0),("C5",1.5),(None,1.5)],
     [("D5",2.0),("C5",1.5),(None,1.5)],
     [("E5",2.0),("G5",1.5),(None,1.5)],
     [("E5",1.5),("C5",1.5),("A4",2.0)],
     [("F5",2.0),("E5",1.5),(None,1.5)],
     [("D5",2.5),("F5",1.5),(None,1.0)],
     [("G5",2.0),("E5",2.0),(None,1.0)],
     [("F5",3.0),(None,2.0)]], reverb_decay=3.4))

# ---------------------------------------------------------------- sfx
def short(sig):  # tidy tail
    return normalise(reverb(sig, 1.2, 0.25))

n = int(0.22 * SR); t = np.arange(n) / SR
tap = np.sin(2*np.pi*620*t)*np.exp(-t*26) + 0.4*np.sin(2*np.pi*1240*t)*np.exp(-t*34)
write("sfx_tap", short(tap * 0.5))

found = np.zeros(int(1.0*SR))
for i, f in enumerate([note_freq("E5"), note_freq("G#5"), note_freq("B5")]):
    mix_into(found, bell(f, 0.9, 0.35), i*0.08)
write("sfx_found", short(found))

n2 = int(0.5*SR); t2 = np.arange(n2)/SR
wrong = (np.sin(2*np.pi*196*t2) + 0.5*np.sin(2*np.pi*185*t2)) * np.exp(-t2*5) * 0.35
write("sfx_wrong", short(wrong))

unlock = np.zeros(int(2.2*SR))
for i, nm in enumerate(["C5","E5","G5","C6","E6"]):
    mix_into(unlock, bell(note_freq(nm), 1.6, 0.3), i*0.13)
mix_into(unlock, piano(note_freq("C3"), 2.0, 0.25), 0.0)
write("sfx_unlock", short(unlock))

n3 = int(0.65*SR); t3 = np.arange(n3)/SR
noise = rng.normal(0, 1, n3)
k = 400
sm = np.convolve(noise, np.hanning(k)/np.sum(np.hanning(k)), mode='same')
page = (sm - np.convolve(sm, np.ones(60)/60, mode='same')) * np.exp(-t3*6) * 0.9
page *= np.clip(np.sin(np.pi*t3/0.65), 0, 1)
write("sfx_page", short(page*0.8))

sparkle = np.zeros(int(1.4*SR))
for i in range(9):
    f = note_freq(["C6","E6","G6","B6","D7"][i % 5]) * (1 + rng.normal(0, 0.004))
    mix_into(sparkle, bell(f, 0.7, 0.16), i*0.055 + rng.random()*0.03)
write("sfx_sparkle", short(sparkle))

# five melody-puzzle notes (pentatonic, pleasant in any order)
for i, nm in enumerate(["D4","F4","G4","A4","C5"]):
    s = np.zeros(int(1.6*SR))
    mix_into(s, bell(note_freq(nm), 1.4, 0.45), 0)
    mix_into(s, piano(note_freq(nm), 1.2, 0.18), 0)
    write(f"note_{i+1}", short(s))
