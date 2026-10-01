#!/usr/bin/env bash
# Scripted QA playthrough of "Pour Lohen" on a real Android emulator.
# Installs the APK, launches it, drives it with real touch events and captures
# screenshots + logcat as evidence. Virtual coordinates (1600x900) are mapped to
# the actual device resolution at runtime.
set -u

PKG=com.esteban.lohen
ACT=$PKG/.android.AndroidLauncher
OUT=/tmp/qa
mkdir -p "$OUT"

adb wait-for-device
adb shell input keyevent 82 || true

echo "== device =="
adb shell getprop ro.build.version.release
SIZE=$(adb shell wm size | tr -d '\r' | awk '{print $3}')
DW=${SIZE%x*}; DH=${SIZE#*x}
echo "portrait size: ${DW}x${DH}"
# the activity is locked to landscape -> swap
LW=$DH; LH=$DW
echo "landscape size: ${LW}x${LH}"

# virtual (1600x900, origin bottom-left) -> device pixels (origin top-left)
tapv() {
  local vx=$1 vy=$2
  local scale ox oy px py
  scale=$(awk -v w="$LW" -v h="$LH" 'BEGIN{sw=w/1600; sh=h/900; print (sw<sh?sw:sh)}')
  ox=$(awk -v w="$LW" -v s="$scale" 'BEGIN{print (w-1600*s)/2}')
  oy=$(awk -v h="$LH" -v s="$scale" 'BEGIN{print (h-900*s)/2}')
  px=$(awk -v x="$vx" -v s="$scale" -v o="$ox" 'BEGIN{printf "%d", o+x*s}')
  py=$(awk -v y="$vy" -v s="$scale" -v o="$oy" 'BEGIN{printf "%d", o+(900-y)*s}')
  adb shell input tap "$px" "$py"
}

swipev() {
  local vx1=$1 vy1=$2 vx2=$3 vy2=$4 dur=${5:-800}
  local scale ox oy
  scale=$(awk -v w="$LW" -v h="$LH" 'BEGIN{sw=w/1600; sh=h/900; print (sw<sh?sw:sh)}')
  ox=$(awk -v w="$LW" -v s="$scale" 'BEGIN{print (w-1600*s)/2}')
  oy=$(awk -v h="$LH" -v s="$scale" 'BEGIN{print (h-900*s)/2}')
  p1x=$(awk -v x="$vx1" -v s="$scale" -v o="$ox" 'BEGIN{printf "%d", o+x*s}')
  p1y=$(awk -v y="$vy1" -v s="$scale" -v o="$oy" 'BEGIN{printf "%d", o+(900-y)*s}')
  p2x=$(awk -v x="$vx2" -v s="$scale" -v o="$ox" 'BEGIN{printf "%d", o+x*s}')
  p2y=$(awk -v y="$vy2" -v s="$scale" -v o="$oy" 'BEGIN{printf "%d", o+(900-y)*s}')
  adb shell input swipe "$p1x" "$p1y" "$p2x" "$p2y" "$dur"
}

shot() { sleep 1; adb exec-out screencap -p > "$OUT/$1.png"; echo "shot $1"; }

advance() { # tap the lower centre (dialogue area) n times
  local n=$1
  for _ in $(seq 1 "$n"); do tapv 800 120; sleep 1.1; done
}

echo "== install =="
adb install -r -g android/build/outputs/apk/debug/android-debug.apk || exit 1
adb logcat -c

echo "== launch =="
adb shell am start -n "$ACT"
sleep 18
shot 01-title

# ---- crash check right after boot
if ! adb shell pidof $PKG > /dev/null; then
  echo "!! process died during boot"
  adb logcat -d > "$OUT/logcat.txt"
  exit 1
fi

echo "== start a new journey =="
tapv 350 482     # "Commencer le voyage" (no save -> primary slot)
sleep 3
shot 02-chapter1-title
advance 2        # skip the title card + first narration
shot 03-intro
advance 6        # rest of the intro dialogue
shot 04-explore

echo "== explore hotspots =="
tapv 416 468 ; sleep 2 ; shot 05-hotspot-window ; advance 3
tapv 928 333 ; sleep 2 ; shot 06-hotspot-desk   ; advance 4
tapv 672 657 ; sleep 2 ; shot 07-hotspot-lights ; advance 3
shot 08-after-exploration

echo "== journal + hint =="
tapv 1395 790 ; sleep 2 ; shot 09-journal ; tapv 800 450 ; sleep 1
tapv 1180 790 ; sleep 2 ; shot 10-hint    ; tapv 800 450 ; sleep 1

echo "== puzzle 1: ordering memories =="
tapv 800 202 ; sleep 2 ; shot 11-puzzle-order
# solution order is [1,0,2,3] starting from [2,3,0,1]
tapv 800 520 ; sleep 1 ; tapv 800 384 ; sleep 1    # swap slot1 <-> slot2
shot 12-puzzle-mid
tapv 800 248 ; sleep 1 ; tapv 800 112 ; sleep 1
shot 13-puzzle-attempt

echo "== settings screen =="
adb shell input keyevent 4 ; sleep 2        # back -> title
shot 14-back-to-title
tapv 350 182 ; sleep 2 ; shot 15-settings   # Réglages
adb shell input keyevent 4 ; sleep 2

echo "== chapter select =="
tapv 350 182 ; sleep 2
shot 16-chapters

echo "== logs =="
adb logcat -d > "$OUT/logcat.txt"
grep -iE "FATAL|AndroidRuntime|libGDX|Lohen|Content|Chapter|OutOfMemory" "$OUT/logcat.txt" | head -80 > "$OUT/logcat-highlights.txt" || true

if adb shell pidof $PKG > /dev/null; then
  echo "RESULT: app still alive at the end of the run"
  echo "alive" > "$OUT/result.txt"
else
  echo "RESULT: app died"
  echo "died" > "$OUT/result.txt"
fi
grep -c "FATAL EXCEPTION" "$OUT/logcat.txt" | tee "$OUT/fatal-count.txt"
exit 0
