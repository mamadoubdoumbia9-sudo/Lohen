# État du projet — Esteban & Lohen

Date : 2026-10-01
Branche : `arena/01a0f98e-lohen`

## Réalisé dans cette session
- Jeu narratif web responsive jouable dans `index.html`.
- Parcours complet en trois chapitres : jardin, observatoire, serre.
- Énigme d'observation (ordre des lanternes), puzzle de séquence, révélation de la lettre finale.
- Sauvegarde automatique via `localStorage`, reprise, menu pause, reset, option sonore tactile.
- Direction artistique 2D illustrée réalisée en CSS : ciel, lune, paysage, dôme, jardin et lettre.
- Contrôles tactiles : tap et boutons contextuels, aucun joystick.

## Limites honnêtes
- Le build Android natif n'est pas vérifiable dans cet environnement : aucun JDK/SDK Android détecté.
- L'expérience est livrée comme PWA/web playable et peut être encapsulée par Capacitor après installation d'un JDK + Android SDK.
- Les polices Google sont un enrichissement externe ; le jeu reste lisible avec les fallbacks système.

## Prochaine étape
Valider sur appareil Android et produire l'APK/AAB via un wrapper Android/Capacitor lorsque le toolchain sera disponible.
