# Esteban & Lohen — Projet de jeu narratif

> **Projet cadeau d'Esteban pour Lohen** — une expérience mobile narrative, interactive, mystérieuse et romantique, conçue pour être développée avec **LM Arena Agent Mode**.

## 🎮 Vision

Ce projet consiste à créer un **vrai jeu mobile fonctionnel**, et non une maquette, une fausse démo ou une simulation.

Le joueur suit une aventure construite autour de l'exploration, d'énigmes, de devinettes, d'interactions avec l'environnement et d'une progression narrative qui conduit à une conclusion personnelle : **Lohen découvre une lettre d'amour écrite par Esteban.**

Le ton doit progressivement faire comprendre qu'un message important se cache derrière l'aventure, sans révéler trop tôt la conclusion.

À la fin de la lettre doivent apparaître exactement :

> **Je t'aime ❤️**
>
> **J'espère que tu as apprécié mon cadeau.**

## ❤️ Histoire centrale

Le jeu est imaginé comme un cadeau personnel.

- **Créateur / auteur du cadeau :** Esteban
- **Destinataire :** Lohen
- **Révélation finale :** une lettre d'amour qui révèle les sentiments d'Esteban pour Lohen
- **Dernière émotion recherchée :** surprise, tendresse et impression d'avoir réellement reçu un cadeau personnalisé

La narration peut utiliser des indices, des objets, des inscriptions, des énigmes et des fragments de texte afin de préparer progressivement la révélation finale.

## 🧩 Mécaniques de jeu

Le système de jeu doit privilégier l'interaction et la réflexion plutôt qu'un contrôle classique de jeu d'action.

Les mécaniques peuvent inclure notamment :

- devinettes ;
- énigmes logiques ;
- codes et combinaisons ;
- observation de l'environnement ;
- recherche d'objets ;
- associations d'indices ;
- puzzles visuels ;
- séquences narratives interactives ;
- interactions avec des objets ;
- choix narratifs lorsque cela sert réellement l'histoire ;
- mini-défis variés ;
- secrets et chemins optionnels ;
- récompenses narratives ;
- progression par découverte.

Les mécaniques définitives doivent être sélectionnées et équilibrées pendant la conception. Aucune fonctionnalité ne doit être ajoutée uniquement pour augmenter artificiellement la taille du projet.

## 🚫 Contraintes fondamentales

### Moteurs interdits

Le projet **ne doit pas utiliser** :

- Unity ;
- Unreal Engine ;
- Godot.

L'agent doit rechercher et choisir une technologie réellement compatible avec son environnement d'exécution et avec la génération d'un build Android.

### Contrôles

Le jeu final **ne doit pas utiliser de joystick virtuel** ni de système de déplacement par joystick.

Les interactions doivent être pensées autour de contrôles tactiles adaptés au design retenu : touches contextuelles, tap, glissement, zones interactives, gestes, boutons d'action, interactions directes avec les objets, etc.

### Direction visuelle

Le projet doit éviter absolument les personnages ou créatures générés sous forme de :

- cubes ;
- sphères primitives non retravaillées ;
- mannequins géométriques ;
- personnages low-poly génériques quand ils donnent un aspect de prototype ;
- formes primitives utilisées comme personnages finaux ;
- placeholders présentés comme assets définitifs.

La direction visuelle doit viser un résultat **illustré, cohérent, émotionnel et soigné**, avec une forte préférence pour la **2D**, les illustrations, les sprites, les décors dessinés, les effets de parallaxe et les animations adaptées au style choisi.

## 🎨 Images, animations et vidéo

Lorsque l'environnement d'Agent Mode permet réellement de générer, modifier ou intégrer des images, l'agent doit exploiter ces capacités **lorsqu'elles améliorent réellement le jeu**.

Exemples :

- illustrations des scènes ;
- portraits de personnages ;
- fonds et décors ;
- éléments interactifs ;
- transitions ;
- effets visuels ;
- animations 2D ;
- séquences animées ;
- cinématiques.

La vidéo ne doit être intégrée que si l'outil réellement disponible permet une génération/import fiable et si son utilisation apporte une vraie valeur. L'agent ne doit jamais prétendre avoir créé une vidéo s'il ne possède pas l'outil nécessaire.

## 🧠 Méthode de développement

Le projet est organisé en un très grand nombre de blocs de travail afin de permettre une spécification extrêmement détaillée.

### Échelle du projet

- **Minimum visé : 100 000 micro-blocs documentés**
- Possibilité d'aller au-delà lorsque la décomposition apporte une vraie valeur
- Aucun remplissage artificiel
- Chaque micro-bloc doit avoir un objectif concret

Un micro-bloc utile doit, lorsque pertinent, contenir :

1. objectif ;
2. contexte ;
3. dépendances ;
4. décision de conception ;
5. implémentation ;
6. fichiers concernés ;
7. code ou pseudo-code si nécessaire ;
8. tests ;
9. critères d'acceptation ;
10. preuve de réalisation.

## 💻 Exigences techniques

Le projet doit être un **véritable logiciel exécutable**.

L'agent doit :

- analyser l'environnement disponible ;
- déterminer les technologies réellement installables ;
- faire des recherches techniques avant les choix structurants ;
- consulter la documentation officielle ;
- rechercher des bibliothèques et outils pertinents sur GitHub ;
- vérifier la compatibilité des dépendances ;
- vérifier les licences avant toute réutilisation ;
- créer une architecture cohérente ;
- écrire du code réel ;
- tester le code ;
- corriger les erreurs ;
- maintenir le projet dans Git ;
- construire l'application Android ;
- vérifier l'installation et le lancement du build.

## 🔬 Recherche obligatoire

Aucune dépendance importante ne doit être ajoutée « au hasard ».

Pour chaque technologie critique, l'agent doit rechercher autant que nécessaire :

- documentation officielle ;
- version actuelle compatible ;
- exigences système ;
- compatibilité Android ;
- compatibilité avec l'architecture choisie ;
- problèmes connus ;
- maintenance du projet ;
- licence ;
- alternatives ;
- stratégie de remplacement en cas d'échec.

Les résultats de recherche importants doivent être consignés dans la documentation du projet.

## 🧪 Tests et validation

Une fonctionnalité n'est **pas considérée comme terminée** simplement parce que le code existe.

Elle doit être vérifiée selon le niveau de risque :

- compilation ;
- tests automatisés ;
- tests d'intégration ;
- tests UI ;
- tests de régression ;
- test sur appareil Android ou environnement équivalent lorsque possible ;
- vérification des performances ;
- vérification de la sauvegarde ;
- vérification des états d'erreur.

### Règle de preuve

Pour toute fonctionnalité importante, l'agent doit pouvoir indiquer :

- ce qui a été créé ;
- où cela se trouve ;
- comment cela a été testé ;
- le résultat du test ;
- ce qui reste éventuellement à faire.

## 📦 Taille du jeu

Le projet vise une **taille installée minimale de 1 Go**, mais cette valeur ne doit jamais être atteinte par du remplissage artificiel.

Sont acceptés comme contenu réel :

- nombreuses illustrations originales ;
- animations ;
- musiques ;
- bruitages ;
- voix ;
- cinématiques ;
- environnements ;
- niveaux ;
- contenu narratif ;
- assets réellement utilisés ;
- ressources nécessaires au jeu.

**Interdiction stricte :** ajouter des fichiers inutiles, des données aléatoires, des copies, des archives cachées ou tout autre contenu sans valeur uniquement pour augmenter la taille du fichier.

Si 1 Go n'est pas techniquement justifiable avec le contenu réellement prévu, l'agent doit le signaler au lieu de fabriquer artificiellement du poids.

## 📱 Android

Le produit final doit être pensé pour Android dès le départ.

La configuration Android devra notamment traiter :

- application ID/package ;
- versioning ;
- permissions ;
- architecture CPU ;
- ressources ;
- résolution et densité d'écran ;
- rotation ;
- cycle de vie ;
- stockage ;
- performances ;
- mémoire ;
- batterie ;
- gestion réseau si nécessaire ;
- signature ;
- APK ;
- AAB si nécessaire.

## 🗃️ Git et GitHub

Le dépôt doit rester propre et récupérable.

L'agent doit :

- travailler dans des branches adaptées ;
- faire des commits compréhensibles ;
- éviter les changements destructifs non justifiés ;
- documenter les décisions importantes ;
- maintenir un état de reprise ;
- vérifier le diff avant les étapes importantes ;
- ne jamais déclarer un travail terminé sans vérifier les fichiers réellement modifiés.

### Reprise entre sessions

Le projet doit posséder des fichiers d'état permettant à un agent suivant de reprendre le travail sans dépendre uniquement de la mémoire de la session.

Exemples :

- `PROJECT_STATE.md`
- `CURRENT_BLOCK.md`
- `DECISIONS.md`
- `TECH_STACK.md`
- `BUILD_STATUS.md`
- `KNOWN_ISSUES.md`
- `TEST_REPORT.md`
- `ASSET_MANIFEST.md`

## 🤖 Règles spécifiques pour LM Arena Agent Mode

L'agent doit se comporter comme une **équipe de production complète** :

- Game Designer ;
- Narrative Designer ;
- Level Designer ;
- UX/UI Designer ;
- Technical Designer ;
- Programmeur ;
- QA Engineer ;
- ingénieur build Android ;
- responsable documentation.

### Interdictions comportementales

L'agent ne doit pas :

- inventer une fonctionnalité comme terminée ;
- simuler une compilation réussie ;
- annoncer un test qu'il n'a pas exécuté ;
- inventer le contenu d'un fichier qu'il n'a pas vérifié ;
- inventer un résultat de recherche ;
- inventer une dépendance ;
- prétendre avoir accès à un outil qui n'est pas présent ;
- contourner les contraintes techniques du projet ;
- remplacer silencieusement une exigence par une autre ;
- utiliser des placeholders comme résultat final sans l'indiquer.

### Règle d'incertitude

En cas d'incertitude technique, l'agent doit :

1. constater l'incertitude ;
2. rechercher des preuves ;
3. tester l'hypothèse ;
4. choisir une solution compatible ;
5. documenter la décision.

## 🏗️ Architecture documentaire recommandée

```text
/
├── README.md
├── PROJECT_STATE.md
├── CURRENT_BLOCK.md
├── DECISIONS.md
├── TECH_STACK.md
├── BUILD_STATUS.md
├── KNOWN_ISSUES.md
├── TEST_REPORT.md
├── ASSET_MANIFEST.md
├── docs/
│   ├── game-design/
│   ├── narrative/
│   ├── level-design/
│   ├── technical-design/
│   ├── ui-ux/
│   ├── art/
│   ├── audio/
│   ├── qa/
│   └── research/
├── src/              # selon la technologie réellement choisie
├── assets/
├── tests/
└── scripts/
```

La structure exacte doit être adaptée à la technologie finale choisie après étude de faisabilité.

## 🧭 Principe de production

Le projet suit la logique :

**Concevoir → rechercher → décider → prototyper → implémenter → tester → corriger → valider → documenter → intégrer.**

Une décision technique importante ne doit pas être prise uniquement parce qu'elle est rapide à coder.

## 🏁 Définition de « terminé »

Le projet sera considéré comme terminé uniquement lorsque les éléments nécessaires sont réellement présents et vérifiés, notamment :

- jeu exécutable ;
- parcours de jeu complet ;
- gameplay fonctionnel ;
- histoire complète ;
- énigmes fonctionnelles ;
- fin narrative fonctionnelle ;
- lettre finale correctement intégrée ;
- UI fonctionnelle ;
- sauvegarde testée ;
- erreurs critiques corrigées ;
- performances contrôlées ;
- build Android produit ;
- installation vérifiée ;
- documentation à jour.

## 💌 Fin du jeu

La fin doit conserver une importance émotionnelle particulière.

Lohen doit parvenir jusqu'à la lettre après avoir terminé l'expérience prévue. La lettre doit révéler les sentiments d'Esteban et se conclure par les deux lignes imposées :

**Je t'aime ❤️**

**J'espère que tu as apprécié mon cadeau.**

La présentation doit être soignée : mise en page, animation, musique éventuelle, transitions et rythme doivent servir la scène sans la surcharger.

## 📚 Documents du projet

Le fichier `PROMPT_MAITRE_ARENA_ESTEBAN_LOHEN_V2_STRICT.md` contient les instructions d'exécution détaillées destinées à l'agent.

Le cahier des charges PDF associé contient la décomposition massive du projet en blocs et micro-blocs.

## ⚠️ Principe absolu

> **Le but n'est pas de produire beaucoup de texte. Le but est de produire beaucoup de travail réel, vérifiable et utile.**

Chaque bloc doit faire avancer concrètement le jeu.

---

**Projet : Esteban & Lohen**  
**Type : jeu mobile narratif / puzzle / aventure**  
**Cible principale : Android**  
**Agent de développement : LM Arena Agent Mode**
