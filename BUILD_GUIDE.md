# Build & installation — Pour Lohen

## Installer le jeu (le plus simple)

1. Télécharge `dist/PourLohen-debug.apk` depuis ce dépôt (branche `arena/01a0f998-lohen`).
2. Sur le téléphone Android : Paramètres → Sécurité → autoriser « Installer des applications inconnues » pour ton navigateur / gestionnaire de fichiers.
3. Ouvre le fichier APK et appuie sur « Installer ».
4. Lance **Pour Lohen**. Le jeu est en mode paysage, plein écran, et fonctionne hors ligne.

Android 5.0 (API 21) minimum · ABI : armeabi-v7a, arm64-v8a, x86, x86_64 · ~12 Mo · aucune permission requise.

Par ADB :

```bash
adb install -r dist/PourLohen-debug.apk
adb shell am start -n com.esteban.lohen/.android.AndroidLauncher
```

## Reconstruire l'APK

Pré-requis : JDK 17, Android SDK (compileSdk 35, build-tools), Gradle 8.11.1.

```bash
gradle :core:test              # 8 tests de contenu / jouabilité
gradle :android:assembleDebug  # -> android/build/outputs/apk/debug/android-debug.apk
```

Le dépôt n'embarque pas de `gradle-wrapper.jar` : la CI utilise `gradle/actions/setup-gradle@v4`
avec Gradle 8.11.1. En local, n'importe quel Gradle 8.9+ fonctionne.

## Chaîne d'intégration continue

| Workflow | Fichier | Rôle |
|---|---|---|
| Android Build | `.github/workflows/android.yml` | tests unitaires + `assembleDebug`, pousse l'APK dans `dist/` et le rapport dans `build-reports/` |
| Android Emulator QA | `.github/workflows/android-emulator-test.yml` | installe l'APK sur un émulateur Android réel (API 30, x86_64), joue le scénario `ci/qa_playthrough.sh`, pousse captures d'écran + logcat dans `build-reports/qa/` |

Les deux workflows renvoient leurs artefacts **dans la branche** (et non dans les artefacts GitHub),
ce qui rend chaque build vérifiable directement depuis le dépôt.

## Regénérer les assets

```bash
python3 tools/gen_audio.py     # musique + bruitages originaux (numpy -> OGG/Vorbis)
```

Les illustrations sont dans `art/src/` (sources haute résolution) et exportées dans
`android/assets/img/`.
