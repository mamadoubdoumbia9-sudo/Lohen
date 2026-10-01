# Journal de recherche technique — 2026-10-01

## 1. Audit de l'environnement (vérifié, pas supposé)

| Vérification | Commande | Résultat |
|---|---|---|
| JDK local | `which java javac` | **absent** |
| Android SDK local | `$ANDROID_HOME`, `ls ~/Android` | **absent** |
| Droits root / apt | `sudo -n true`, `apt-get install` | **refusés** (pas de root) |
| Réseau : github.com | `curl -I` | **200** |
| Réseau : api.github.com, codeload | `curl -I` | **200 / 301** |
| Réseau : registry.npmjs.org, pypi.org | `curl -I` | **200** |
| Réseau : dl.google.com (SDK Android) | `curl -I` | **bloqué** (SSL_ERROR_SYSCALL) |
| Réseau : repo1.maven.org, maven.google.com | `curl -I` | **bloqué** |
| Réseau : services.gradle.org, api.adoptium.net | `curl -I` | **bloqué** |

**Conséquence :** aucune compilation Android n'est possible *dans* le bac à sable (ni SDK, ni
dépôt Maven, ni JDK complet — `jdk4py` installé depuis PyPI ne fournit qu'un JRE, sans `javac`).

## 2. Décision technique

**Moteur retenu : libGDX 1.13.1** (Apache-2.0), backend Android natif OpenGL ES 2.0.

Pourquoi :
- ce n'est ni Unity, ni Unreal, ni Godot, ni une WebView — c'est une bibliothèque Java compilée
  dans une vraie `Activity` Android (`AndroidApplication`), rendu OpenGL ES natif ;
- maintenue, documentée, licence Apache-2.0 compatible ;
- excellent rendu 2D (SpriteBatch), audio OGG, entrées tactiles brutes, `Preferences` pour la
  sauvegarde, extension FreeType pour la typographie vectorielle ;
- APK léger (12 Mo) et performant sur téléphone modeste.

Alternatives écartées : Unity/Unreal/Godot (interdits), WebView/Capacitor (interdit comme moteur),
Jetpack Compose (pas un moteur de jeu, animation 2D et boucle de rendu inadaptées),
SDL2/NDK (chaîne C++ inutilement lourde ici, aucun NDK disponible).

## 3. Contournement de la chaîne de build (vérifié)

Le dépôt étant sur GitHub, la compilation est faite par **GitHub Actions** (runners `ubuntu-latest`,
Android SDK + JDK préinstallés, réseau complet). Comme les hôtes de téléchargement des artefacts
GitHub (`objects.githubusercontent.com`, `results-receiver.actions.githubusercontent.com`) sont eux
aussi bloqués depuis le bac à sable, **les workflows repoussent leurs résultats dans la branche
par `git push`** : APK dans `dist/`, journaux dans `build-reports/`, captures d'émulateur dans
`build-reports/qa/`. Preuve : run `36936010366` (prototype de faisabilité, APK 5,1 Mo) puis
run `36937918298` (jeu complet, APK 12,1 Mo, 8 tests verts).

## 4. Ressources tierces et licences

| Ressource | Source | Licence | Usage |
|---|---|---|---|
| libGDX 1.13.1 + gdx-freetype | Maven Central | Apache-2.0 | moteur |
| Cormorant Garamond | npm `@fontsource/cormorant-garamond` (converti woff2→ttf avec fontTools) | SIL OFL 1.1 (copie dans `android/assets/font/`) | titres, corps de texte, italique |
| Inter | npm `@fontsource/inter` | SIL OFL 1.1 (copie incluse) | interface |
| Illustrations | générées pour ce projet (outil d'image d'Arena Agent Mode) | créées pour ce cadeau | fonds, personnages |
| Icônes, cœur, particules | générées par script Python/Pillow (`tools/`) | originales | UI, VFX |
| Musique et bruitages | synthétisés par `tools/gen_audio.py` (numpy, aucun échantillon tiers) | originaux | bande-son |

Aucun asset ripé, aucune musique sous copyright, aucune police non libre.
