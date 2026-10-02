#!/usr/bin/env bash
# Full scripted playthrough of "Pour Lohen" on a real Android emulator:
# install -> launch -> explore -> solve the six puzzles -> read the letter.
# Captures screenshots + logcat as evidence. Virtual game coordinates
# (1600x900, origin bottom-left) are mapped to real device pixels at runtime.
set -u

# an adb call that hangs (flaky emulator IPC) must never freeze the run;
# 60s is generous for taps/logcat, 90s for APK install and screencaps
adb() { timeout 90 command adb "$@"; }

PKG=com.esteban.lohen
ACT=$PKG/.android.AndroidLauncher
OUT=/tmp/qa
mkdir -p "$OUT"

adb wait-for-device
boot=$SECONDS
while [ $SECONDS -lt $((boot+420)) ]; do
  [ "$(adb shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')" = "1" ] && break
  sleep 5
done
echo "boot_completed after $((SECONDS-boot))s"
adb shell input keyevent 82 || true

SIZE=$(adb shell wm size | tr -d '\r' | awk '{print $3}')
DW=${SIZE%x*}; DH=${SIZE#*x}
LW=$DH; LH=$DW                      # the activity is locked to landscape
SCALE=$(awk -v w="$LW" -v h="$LH" 'BEGIN{sw=w/1600; sh=h/900; print (sw<sh?sw:sh)}')
OX=$(awk -v w="$LW" -v s="$SCALE" 'BEGIN{print (w-1600*s)/2}')
OY=$(awk -v h="$LH" -v s="$SCALE" 'BEGIN{print (h-900*s)/2}')
echo "device ${DW}x${DH} -> landscape ${LW}x${LH} scale=$SCALE offset=$OX,$OY"

dx() { awk -v x="$1" -v s="$SCALE" -v o="$OX" 'BEGIN{printf "%d", o+x*s}'; }
dy() { awk -v y="$1" -v s="$SCALE" -v o="$OY" 'BEGIN{printf "%d", o+(900-y)*s}'; }

tapv() { adb shell input tap "$(dx "$1")" "$(dy "$2")"; }
# normalised hotspot coordinates straight out of chapters.json
taphs() { tapv "$(awk -v v="$1" 'BEGIN{print v*1600}')" "$(awk -v v="$2" 'BEGIN{print (1-v)*900}')"; }
swipev() { adb shell input swipe "$(dx "$1")" "$(dy "$2")" "$(dx "$3")" "$(dy "$4")" "${5:-700}"; }

shot() { sleep 1; adb exec-out screencap -p > "$OUT/$1.png"; echo "  shot $1"; }

# one dialogue line = complete the typewriter, then advance
line() { for _ in $(seq 1 "${1:-1}"); do tapv 800 110; sleep 0.8; tapv 800 110; sleep 0.9; done; }

echo "== install =="
adb install -r -g android/build/outputs/apk/debug/android-debug.apk || exit 1
adb logcat -c
adb shell am start -n "$ACT"
sleep 20
shot 01-title
adb shell pidof $PKG > /dev/null || { echo "!! died at boot"; adb logcat -d > "$OUT/logcat.txt"; exit 1; }

echo "== chapitre 1 : Le Seuil =="
tapv 350 482                       # "Commencer le voyage"
sleep 4
shot 02-ch1-titlecard
tapv 800 450 ; sleep 1             # dismiss the title card
line 5                             # intro
shot 03-ch1-explore
taphs 0.42 0.62 ; sleep 1 ; shot 04-ch1-hotspot-fenetre ; line 2
taphs 0.15 0.13 ; sleep 1 ; shot 05-ch1-hotspot-bureau  ; line 3
taphs 0.86 0.86 ; sleep 1 ; line 2
shot 06-ch1-ready
tapv 1395 790 ; sleep 2 ; shot 07-journal ; tapv 800 450 ; sleep 1
tapv 1180 790 ; sleep 2 ; shot 08-hint    ; tapv 800 450 ; sleep 1
tapv 800 202 ; sleep 2             # start the puzzle
shot 09-ch1-puzzle-order
# start [2,3,0,1] -> target [1,0,2,3] : swap (0,3) (1,2) (2,3)
tapv 800 679 ; sleep 1 ; tapv 800 271 ; sleep 1
tapv 800 543 ; sleep 1 ; tapv 800 407 ; sleep 1
tapv 800 407 ; sleep 1 ; tapv 800 271 ; sleep 1
shot 10-ch1-puzzle-arranged
tapv 800 124 ; sleep 3             # "Vérifier"
shot 11-ch1-solved
line 3                             # outro
sleep 2 ; shot 12-ch1-reward
tapv 800 172 ; sleep 4             # "Continuer le voyage"

echo "== chapitre 2 : Le Jardin des Lanternes =="
shot 13-ch2-titlecard
tapv 800 450 ; sleep 1
line 3
taphs 0.10 0.22 ; sleep 1 ; line 2
taphs 0.68 0.33 ; sleep 1 ; line 3
shot 14-ch2-explore
tapv 800 202 ; sleep 2
shot 15-ch2-puzzle-lanterns
# riddle order: 2,4,1,3,0
taphs 0.498 0.545 ; sleep 1.2
taphs 0.625 0.473 ; sleep 1.2
taphs 0.374 0.618 ; sleep 1.2
taphs 0.573 0.503 ; sleep 1.2
shot 16-ch2-lanterns-mid
taphs 0.172 0.752 ; sleep 3
shot 17-ch2-solved
line 3
sleep 2 ; shot 18-ch2-reward
tapv 800 172 ; sleep 4

echo "== chapitre 3 : La Bibliotheque =="
tapv 800 450 ; sleep 1
line 3
taphs 0.84 0.74 ; sleep 1 ; line 2
taphs 0.60 0.13 ; sleep 1 ; line 2
tapv 800 202 ; sleep 2
shot 19-ch3-puzzle-riddle
# compose E T O I L E : the six tiles of the first row, left to right
for X in 485 611 737 863 989 1115; do tapv $X 275 ; sleep 0.9 ; done
sleep 3
shot 20-ch3-solved
line 3
sleep 2 ; shot 21-ch3-reward
tapv 800 172 ; sleep 4

echo "== chapitre 4 : Le Ciel =="
tapv 800 450 ; sleep 1
line 3
taphs 0.78 0.45 ; sleep 1 ; line 2
tapv 800 202 ; sleep 2
shot 22-ch4-puzzle-constellation
# multi-point drag through the eight stars and back to the first
# Star positions come from chapters.json as normalised {x, y} with y measured
# from the TOP, while the puzzle places them at (1-y)*900 in virtual space
# (origin bottom-left). The virtual Y values below are already converted.
D=$(dx 480); E=$(dy 525.2)
adb shell input motionevent DOWN $D $E ; sleep 0.4
for P in "326.4 398.5" "249.6 256.0" "326.4 145.1" "480 200.5" "633.6 145.1" "710.4 256.0" "633.6 398.5" "480 525.2"; do
  set -- $P
  adb shell input motionevent MOVE "$(dx "$1")" "$(dy "$2")" ; sleep 0.45
done
adb shell input motionevent UP "$(dx 480)" "$(dy 525.2)" ; sleep 2
shot 23-ch4-solved
line 3
sleep 2 ; shot 24-ch4-reward
tapv 800 172 ; sleep 4

echo "== chapitre 5 : La Boite a Musique =="
tapv 800 450 ; sleep 1
line 3
taphs 0.30 0.10 ; sleep 1 ; line 2
tapv 800 202 ; sleep 2
shot 25-ch5-puzzle-melody
tapv 800 218 ; sleep 6             # listen to the melody
shot 26-ch5-listening
# replay 2,0,3,1,4,2
tapv 800 452 ; sleep 0.9
tapv 280 400 ; sleep 0.9
tapv 1060 478 ; sleep 0.9
tapv 540 426 ; sleep 0.9
tapv 1320 504 ; sleep 0.9
tapv 800 452 ; sleep 3
shot 27-ch5-solved
line 3
sleep 2 ; shot 28-ch5-reward
tapv 800 172 ; sleep 4

echo "== chapitre 6 : Le Phare =="
tapv 800 450 ; sleep 1
line 3
taphs 0.52 0.65 ; sleep 1 ; line 3
taphs 0.86 0.25 ; sleep 1 ; line 2
shot 29-ch6-explore
tapv 800 202 ; sleep 2
shot 30-ch6-puzzle-code
# drag soleil, lune, coeur, etoile into slots 1..4
swipev 697 290 464 575 900 ; sleep 1.2
swipev 491 290 688 575 900 ; sleep 1.2
swipev 1109 290 912 575 900 ; sleep 1.2
shot 31-ch6-code-mid
swipev 903 290 1136 575 900 ; sleep 3
shot 32-ch6-chest-open
line 1
sleep 2 ; shot 33-ch6-reward
tapv 800 172 ; sleep 5             # "Ouvrir la lettre"

echo "== la lettre =="
T0=$SECONDS
shot 34-letter-opening
# 13 paragraphs, each waiting for Esteban's recorded voice to finish;
# shots matter less than reaching letter-end, so stop photographing and
# jump to the verification when the step budget is close to spent
for i in 35 36 37 38 39 40 41 42 43; do
  [ $((SECONDS-T0)) -gt 240 ] && { echo "  (budget guard: skipping remaining letter shots)"; break; }
  sleep 18 ; shot "$i-letter"
done
sleep 25 ; shot 44-letter-closing
sleep 25 ; shot 45-letter-end

echo "== logs =="
adb logcat -d > "$OUT/logcat.txt"
grep -F "Milestone" "$OUT/logcat.txt" | sed "s/.*Milestone[: ]*//" | awk "!seen[\$0]++" > "$OUT/milestones.txt" || true
echo "--- milestones reached:"; cat "$OUT/milestones.txt"
MISSING=""
for M in "chapter-enter 1" "chapter-complete 1" "chapter-enter 2" "chapter-complete 2" \
         "chapter-enter 3" "chapter-complete 3" "chapter-enter 4" "chapter-complete 4" \
         "chapter-enter 5" "chapter-complete 5" "chapter-enter 6" "chapter-complete 6" \
         "letter-open" "letter-end"; do
  grep -qF "$M" "$OUT/milestones.txt" || MISSING="$MISSING|$M"
done
if [ -n "$MISSING" ]; then
  echo "MISSING MILESTONES:$MISSING" | tr "|" "\n" > "$OUT/missing-milestones.txt"
  cat "$OUT/missing-milestones.txt"
else
  echo "all milestones reached" > "$OUT/missing-milestones.txt"
fi
grep -E " (I|E|W) (Lohen|Chapter|Content|AndroidRuntime)" "$OUT/logcat.txt" | head -60 > "$OUT/logcat-highlights.txt" || true
grep -c "FATAL EXCEPTION" "$OUT/logcat.txt" > "$OUT/fatal-count.txt" || echo 0 > "$OUT/fatal-count.txt"
if adb shell pidof $PKG > /dev/null; then echo alive > "$OUT/result.txt"; else echo died > "$OUT/result.txt"; fi
cat "$OUT/result.txt" "$OUT/fatal-count.txt"
exit 0
