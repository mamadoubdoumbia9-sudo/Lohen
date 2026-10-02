# Pour Lohen

Un jeu d'aventure narratif Android, écrit et fabriqué pour une seule personne.
Six chapitres, six énigmes, une lettre à la fin.

> Le cahier des charges d'origine est conservé tel quel dans [`BRIEF.md`](BRIEF.md).

## Ce que c'est vraiment

Une **application Android native** (libGDX 1.13.1 + Java 17, backend Android
officiel). Pas de WebView, pas de page HTML, pas de Unity / Unreal / Godot.
Le code de jeu est dans `core/`, la couche Android dans `android/`, et le
résultat est un vrai `.apk` installable.

## Installer

1. Télécharger l'APK :
   - **build complet** (cinématiques, bande originale lossless, art HD) :
     onglet **Releases** du dépôt, fichier `PourLohen-full-debug.apk` ;
   - **build léger** (~12 Mo, même jeu, assets compressés) :
     [`dist/PourLohen-debug.apk`](dist/PourLohen-debug.apk).
2. Sur le téléphone : ouvrir le fichier, autoriser « installer des
   applications inconnues » pour le navigateur ou le gestionnaire de fichiers.
3. Ouvrir **Pour Lohen**.

Android 5.0 (API 21) minimum, testé sur émulateur API 30 (x86_64).
L'APK est signé avec la clé de debug Android : c'est normal pour une
installation directe, cela n'empêche ni l'installation ni le fonctionnement.

## Le jeu

| Chapitre | Lieu | Énigme |
|---|---|---|
| 1 | Le Seuil | remettre quatre souvenirs dans l'ordre |
| 2 | Le Jardin des Lanternes | allumer cinq lanternes dans le bon ordre |
| 3 | La Bibliothèque des Souvenirs | reconstituer un mot à partir d'un indice |
| 4 | Le Ciel qu'on Regardait | relier huit étoiles en un seul geste |
| 5 | La Boîte à Musique | rejouer une mélodie de six notes |
| 6 | Le Phare | ouvrir un coffre à quatre symboles |

Puis la lettre : elle s'écrit seule, paragraphe après paragraphe, lue par la
voix d'Esteban, et se termine par ses deux dernières phrases.

Tout se joue au doigt : tap, appui long, glissé, glisser-déposer, boutons
contextuels. **Aucun joystick virtuel.** Les indices reformulent la piste,
ils ne résolvent jamais l'énigme à la place du joueur. La progression, les
énigmes résolues et les objets découverts sont sauvegardés automatiquement.

## Contenu

- 7 cinématiques pré-rendues 1920×1080 (parallaxe réelle, lumière volumétrique,
  particules, fondus enchaînés), diffusées à 20 images/seconde ;
- bande originale originale : 10 pièces longues en FLAC 48 kHz / 24 bits pour
  le build complet, versions OGG pour le build léger ;
- voix off française de la lettre ;
- illustrations originales, masters sans perte 2048 px pour le build complet.

Provenance et licences de chaque fichier : [`ASSET_MANIFEST.md`](ASSET_MANIFEST.md).

## Développer / rebuilder

Voir [`BUILD_GUIDE.md`](BUILD_GUIDE.md). En résumé :

```bash
./gradlew :android:assembleDebug          # build léger
python3 tools/gen_cutscenes.py            # contenu lourd (optionnel)
python3 tools/gen_audio_hd.py
python3 tools/gen_hd_art.py
./gradlew :android:assembleDebug          # build complet
```

Les builds et les tests tournent aussi sur GitHub Actions
(`.github/workflows/`) : build + tests unitaires, test d'installation et de
lancement sur émulateur avec captures d'écran, et build complet publié en
Release.

## État

- Ce qui marche, ce qui ne marche pas : [`KNOWN_ISSUES.md`](KNOWN_ISSUES.md)
- Journal de recherche technique : [`RESEARCH_LOG.md`](RESEARCH_LOG.md)
- Rapports de build et preuves de test : `build-reports/`
