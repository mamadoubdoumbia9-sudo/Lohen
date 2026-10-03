# RAPPORT FINAL — Pour Lohen

*(Ce qui a réellement été construit et testé, avec les preuves.)*

## Ce qui a été livré

**Un jeu d'aventure narratif Android complet**, de Esteban pour Lohen :
application native libGDX 1.13.1 / Java 17 (aucun WebView, aucun HTML,
aucun moteur interdit). Projet libGDX standard : `core/` (jeu) + `android/`
(lanceur). Six chapitres — explorations, hotspots, dialogues — chacun
protégé par une énigme réelle (ordre de souvenirs, lanternes, rébus,
constellation à tracer au doigt, mélodie à rejouer, coffre à symboles à
glisser-déposer). L'ensemble se termine par la lettre d'Esteban qui se
révèle paragraphe après paragraphe, lue à voix haute, et qui finit
exactement par « je t'aime ❤️ » puis « j'espère que tu as apprécié mon
cadeau ».

Contrôles 100 % tactiles : tap, appui long, glissés, drag & drop, boutons
contextuels. Aucun joystick virtuel. Indices qui reforment, jamais de
solution automatique. Sauvegarde automatique de la progression et de
l'état des énigmes. Écran paramètres (volumes, vitesse du texte).

## Les deux APK (tous deux compilés sur GitHub Actions, jour pour jour)

| Build | Taille mesurée | Où |
|---|---|---|
| Léger (assets compressés) | ~12 Mio | `dist/PourLohen-debug.apk` |
| **Complet** (cinématiques, HD audio, HD art) | **1 273 516 082 octets (1,19 Gio)** | Release `full-v3`, `PourLohen-full-debug.apk`, sha256 `aafaee73…` |

Le build complet regroupe : 7 cinématiques pré-rendues 1920×1080 à 20 i/s
(721 Mio, parallaxe par tranches de profondeur, lumière volumétrique,
particules, fondus enchaînés, sous-titres), 10 titres originaux de bande
originale longs en FLAC 48 kHz/24 bits (175 Mio), des masters illustrés
2048 px sans perte (47 Mio), la lettre lue en français (15 clips, 3,5 Mio).
Aucun octet de remplissage : `build-reports/full-build.md` détaille la
ventilation et chaque dossier est réellement demandé par le jeu à
l'exécution.

## Tests réellement exécutés

- **Compilation + tests unitaires** sur CI à chaque push
  (`build-reports/last-build.md`), toujours verts.
- **Installation et lancement sur émulateur Android 30 x86_64** à chaque
  push du pipeline QA : vérifié — le jeu boote et répond (preuves
  `build-reports/qa/`).
- **Parcours scénarisé complet** (45 captures + logcat) : les chapitres
  1→3 résolus mécaniquement bout à bout à chaque run ; le chapitre 4 est
  passé après correction du script de QA ; le correctif des lanternes du
  chapitre 2 a permis d'atteindre l'écran de la lettre. Les 14 jalons
  (chapter-enter/complete, letter-open, letter-end) sont désormais
  journalisés et vérifiés automatiquement par la CI ; la dernière
  validation complète de ce verrou teste au moment de l'écriture du
  rapport était en cours sur le dernier pipeline.
- **Regards visuels** des captures : fond correct, personnages visibles,
  HUD, énigmes à l'écran, lettre affichée — inspectés, jamais supposés
  d'après les noms de fichiers.

## Bugs réellement trouvés et corrigés

1. Hit-test « premier trouvé » trop large (lanternes, puis étoiles à
   135 px l'une de l'autre) → sélection de l'élément le plus proche.
2. Script QA : coordonnées étoiles y·900 au lieu de (1−y)·900 → trace
   du cœur impossible ; corrigé.
3. Un `tr -d '\\r'` ma-échappé brûlait 420 s du budget CI → corrigé.
4. Collecte de preuves QA : capture stale des anciens runs pouvait
   maquiller un échec précoce → purge avant collecte + journal shell
   complet committé comme preuve (`script-log.txt`).
5. Voix off incomplète (10/13 paragraphes) → 15 clips au total maintenant.

## Provenance et licences

Tout contenu est original ou sous licence libre : art généré pour ce
projet, audio synthétisé par numpy, voix synthétiseur du projet
(`voice-00`), polices Cormorant Garamond et Inter (SIL OFL 1.1, fichiers
de licence embarqués dans l'APK). Détail exhaustif : `ASSET_MANIFEST.md`.
