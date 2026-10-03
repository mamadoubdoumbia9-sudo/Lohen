# KNOWN_ISSUES — Pour Lohen

Honest list of what is imperfect, verified against the real build. Nothing
here is speculative: each entry was observed in the emulator QA runs or in the
build logs stored under `build-reports/`.

## Open

| # | Issue | Impact | Workaround / plan |
|---|---|---|---|
| 1 | The QA playthrough drives taps from hard-coded virtual coordinates. On device sizes far from the QA profile (very tall tablets), the scripted taps are off — playing by hand is unaffected | Human testing, or per-device coordinate scaling in `ci/qa_playthrough.sh`. |
| 2 | The APK is signed with the **Android debug key**, not a release key. | Android shows "unknown source"; the app cannot go on the Play Store as is. | Expected for a personal gift: enable "install unknown apps" once. A release key can be added to `android/build.gradle` later. |
| 3 | The full build ships two asset tiers (standard + HD). On devices with little RAM the HD texture tier costs ~15 MB of VRAM per background. | Possible slowdown on very old phones (Android 5–6 era). | The LRU keeps at most 2 HD textures alive; if a device struggles, deleting `img/hd/` from the APK is enough to fall back silently. |
| 4 | Cutscene playback decodes 20 JPEG frames per second on a worker thread. | On a low-end CPU the cutscene can drop frames (it never blocks: the renderer keeps the last decoded frame). | A "Passer" button appears after 2 s; cutscenes are never mandatory to progress. |
| 5 | The scripted QA playthrough drives the game through `adb` taps at fixed virtual coordinates. | If a layout changes, the script taps the wrong place and the run stalls — this happened once in chapter 2 before the lantern fix. | Screenshots are always inspected visually, never trusted by filename alone. |
| 6 | No iOS / desktop packaging. | Android only. | Out of scope; the core module is platform-agnostic if needed later. |

## Fixed

| Issue | Fix |
|---|---|
| Chapter 2: tapping a lantern lit the wrong one when two lanterns were close (lanterns 3 and 4 sit 87 px apart in virtual space, the hit test accepted the first match within 110 px) — the QA playthrough was blocked at 3/5 lanterns and never reached the letter. | `LanternPuzzle` now computes the **nearest** lantern within a 150 px reach and only acts on that one. |
| Chapter 4: the scripted heart was invisible to the game — the QA script converted the normalised star Y coordinates as `y*900` instead of `(1-y)*900`, and the puzzle's hit test returned the first star within reach instead of the nearest (two heart stars are 135 px apart). | Script coordinates corrected; `ConstellationPuzzle` now picks the **nearest** star; the QA window over the letter was extended because each paragraph now waits for Esteban's recorded voice to finish. |
| Chapter 4 hint text said "la plus basse" while the first star to touch is actually the topmost one. | Hint reworded to "l'étoile la plus haute… descends par la gauche". |
| QA evidence was ~30 MB of PNGs per run, and the evidence push could fail when another workflow pushed in between. | Screenshots are committed as 50% JPEGs and the push step retries its rebase. |
| QA pass/fail depended on humans inspecting screenshots. | The game logs `Milestone` tags (chapter-enter/complete ×6, puzzle-solved, letter-open, letter-end) and the CI asserts every one of them in logcat — all 14 green on the latest run. |
| Emulator QA runs died early and left stale screenshots from the previous checkout, hiding the true failure point; an adb wrapper of mine also mis-used `command` under `timeout` (not an executable), making every adb call fail instantly. | Purge `build-reports/qa` before collecting; resolve the adb binary once (`ADBBIN=$(command -v adb)`) and tee the whole script to a committed `script-log.txt`. |
| Order puzzle slots and chapter hotspots did not line up with the illustrations. | Coordinates realigned on the real artwork, verified on screenshots. |
| Local Gradle/Android builds impossible in the sandbox (no access to `dl.google.com`, `maven.google.com`, Maven Central). | Every build and every emulator test runs on GitHub Actions; the APK and the reports are committed back to the branch. |
| Heavy content could not be versioned (GitHub refuses files > 100 MB). | The content is generated on the runner from small Python generators and the resulting APK is published as a GitHub **Release** asset. |

## Deliberate design decisions (not bugs)

- **No skip on the final letter.** A tap pauses or resumes the reading; there
  is no way to jump to the end. The two closing lines are rendered verbatim
  from `data/letter.json` and the save is only marked `finished` at the very
  end.
- **No virtual joystick, no free movement.** Everything is tap, long press,
  swipe and drag & drop, as required.
- **Hints never solve a puzzle.** They re-word the clue and point at where to
  look.
