# Guide de lancement

## Prévisualisation locale

Node.js 18+ suffit :

```bash
npx --yes serve . -l 4173
```

Puis ouvrir `http://localhost:4173`.

Le jeu est statique : `index.html`, `styles.css`, `game.js`, `manifest.webmanifest`. Il n'utilise aucun moteur interdit et aucune dépendance de production.

## Android

Le projet cible actuellement une PWA responsive. La production d'un APK/AAB n'a pas été lancée car l'environnement de cette branche ne possède ni `java` ni Android SDK. Pour la suite : installer JDK 17 et Android SDK, choisir Capacitor ou un wrapper WebView maintenu, puis valider installation, cycle de vie et build signé.
