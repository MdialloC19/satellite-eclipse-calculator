# Guide de Test de l'API Eclipse Calculator

Ce document contient les informations nécessaires pour tester l'API REST du calculateur d'éclipses satellite, incluant des exemples de requêtes et de données TLE pour divers satellites.

## Informations de Base sur l'API

- **URL de base**: `http://localhost:8081/satellite-eclipse/api`
- **Endpoint principal**: `/eclipse/calculate`
- **Méthode HTTP**: POST
- **Format de données**: JSON
- **Dépendances**: Bibliothèque Orekit pour les calculs orbitaux et les données astronomiques

## Modèles de Données pour les Requêtes

### Format de Requête

```json
{
  "tle": {
    "satelliteName": "NOM_DU_SATELLITE",
    "line1": "PREMIÈRE_LIGNE_TLE",
    "line2": "DEUXIÈME_LIGNE_TLE"
  },
  "startDate": "DATE_DÉBUT_ISO8601",
  "endDate": "DATE_FIN_ISO8601"
}
```

### Exemples de Données TLE

Voici des exemples de données TLE pour différents satellites que vous pouvez utiliser pour tester l'API:

#### 1. Station Spatiale Internationale (ISS)

```json
{
  "tle": {
    "satelliteName": "ISS (ZARYA)",
    "line1": "1 25544U 98067A   22085.41476591  .00007277  00000-0  13908-3 0  9990",
    "line2": "2 25544  51.6449 336.4797 0005408  61.7847  47.9568 15.49454906334473"
  },
  "startDate": "2025-03-25T00:00:00Z",
  "endDate": "2025-03-26T00:00:00Z"
}
```

#### 2. Satellite Météorologique NOAA-19

```json
{
  "tle": {
    "satelliteName": "NOAA 19",
    "line1": "1 33591U 09005A   25084.51023600  .00000082  00000-0  67956-4 0  9990",
    "line2": "2 33591  99.1709 149.6914 0013503 297.9226  62.0429 14.12432383828952"
  },
  "startDate": "2025-03-25T00:00:00Z",
  "endDate": "2025-03-26T00:00:00Z"
}
```

#### 3. Satellite d'Observation de la Terre Sentinel-2A

```json
{
  "tle": {
    "satelliteName": "SENTINEL-2A",
    "line1": "1 40697U 15028A   25084.47617259 -.00000016  00000-0  21213-4 0  9997",
    "line2": "2 40697  98.5686 131.0303 0001113  82.1501 278.9845 14.30820665510128"
  },
  "startDate": "2025-03-25T00:00:00Z",
  "endDate": "2025-03-26T00:00:00Z"
}
```

#### 4. Satellite GPS IIR-M (PRN 07)

```json
{
  "tle": {
    "satelliteName": "GPS BIIR-07",
    "line1": "1 28874U 05038A   25084.53313657  .00000072  00000-0  00000-0 0  9994",
    "line2": "2 28874  55.7782 114.9025 0090068 248.1830 110.9596  2.00559640142150"
  },
  "startDate": "2025-03-25T00:00:00Z",
  "endDate": "2025-03-26T00:00:00Z"
}
```

#### 5. Satellite de Communication Iridium-133

```json
{
  "tle": {
    "satelliteName": "IRIDIUM 133",
    "line1": "1 43571U 18061F   25084.51862997  .00000069  00000-0  17697-4 0  9991",
    "line2": "2 43571  86.3994 189.6022 0002036  89.9756 270.1679 14.34217841345968"
  },
  "startDate": "2025-03-25T00:00:00Z",
  "endDate": "2025-03-25T12:00:00Z"
}
```

## Méthodes pour Tester l'API

### 1. Utilisation de cURL

Voici comment tester l'API en utilisant cURL:

```bash
curl -X POST http://localhost:8081/satellite-eclipse/api/eclipse/calculate \
  -H "Content-Type: application/json" \
  -d '{
    "tle": {
      "satelliteName": "ISS (ZARYA)",
      "line1": "1 25544U 98067A   22085.41476591  .00007277  00000-0  13908-3 0  9990",
      "line2": "2 25544  51.6449 336.4797 0005408  61.7847  47.9568 15.49454906334473"
    },
    "startDate": "2025-03-25T00:00:00Z",
    "endDate": "2025-03-26T00:00:00Z"
  }'
```

### 2. Utilisation de Python avec requests

```python
import requests
import json

url = "http://localhost:8081/satellite-eclipse/api/eclipse/calculate"

payload = {
    "tle": {
      "satelliteName": "ISS (ZARYA)",
      "line1": "1 25544U 98067A   22085.41476591  .00007277  00000-0  13908-3 0  9990",
      "line2": "2 25544  51.6449 336.4797 0005408  61.7847  47.9568 15.49454906334473"
    },
    "startDate": "2025-03-25T00:00:00Z",
    "endDate": "2025-03-26T00:00:00Z"
}

headers = {
    "Content-Type": "application/json"
}

response = requests.post(url, headers=headers, data=json.dumps(payload))

print(response.status_code)
print(response.json())
```

### 3. Utilisation de JavaScript avec fetch

```javascript
const url = 'http://localhost:8081/satellite-eclipse/api/eclipse/calculate';

const data = {
    tle: {
      satelliteName: "ISS (ZARYA)",
      line1: "1 25544U 98067A   22085.41476591  .00007277  00000-0  13908-3 0  9990",
      line2: "2 25544  51.6449 336.4797 0005408  61.7847  47.9568 15.49454906334473"
    },
    startDate: "2025-03-25T00:00:00Z",
    endDate: "2025-03-26T00:00:00Z"
};

fetch(url, {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
  },
  body: JSON.stringify(data),
})
.then(response => response.json())
.then(data => console.log(data))
.catch((error) => console.error('Error:', error));
```

### 4. Utilisation de Postman

1. Ouvrez Postman
2. Créez une nouvelle requête POST
3. Entrez l'URL: `http://localhost:8081/satellite-eclipse/api/eclipse/calculate`
4. Allez dans l'onglet "Body"
5. Sélectionnez "raw" et "JSON"
6. Copiez-collez un des exemples JSON ci-dessus
7. Cliquez sur "Send"

## Format de Réponse Attendu

```json
{
  "satellite": "NOM_DU_SATELLITE",
  "eclipses": [
    {
      "entryTime": "DATE_ENTRÉE_ISO8601",
      "exitTime": "DATE_SORTIE_ISO8601",
      "durationMinutes": DURÉE_EN_MINUTES
    },
    ...
  ],
  "totalEclipseDurationMinutes": DURÉE_TOTALE_EN_MINUTES
}
```

## Où Trouver des Données TLE à Jour

Pour obtenir des données TLE à jour pour vos tests, vous pouvez consulter ces sources:

1. **CelesTrak**: [https://celestrak.org/NORAD/elements/](https://celestrak.org/NORAD/elements/)
2. **Space-Track**: [https://www.space-track.org/](https://www.space-track.org/) (nécessite une inscription)
3. **N2YO**: [https://www.n2yo.com/](https://www.n2yo.com/)

## Conseils pour le Test

1. **Période de Calcul**: Pour voir plusieurs éclipses, choisissez une période d'au moins 24 heures
2. **Actualité des TLE**: Utilisez des TLE récents pour une meilleure précision
3. **Orbites Diverses**: Testez avec différents types d'orbites (LEO, MEO, GEO) pour voir les différences dans les périodes d'éclipses
4. **Validation**: Vous pouvez valider vos résultats en les comparant avec ceux d'autres outils comme STK (Systems Tool Kit) ou GMAT (General Mission Analysis Tool)

## Dépannage

### Problèmes Courants

1. **HTTP 400 (Bad Request)**: Vérifiez le format de votre JSON et assurez-vous que les dates sont au format ISO 8601
2. **HTTP 404 (Not Found)**: Vérifiez l'URL de l'API et le contexte de l'application
3. **HTTP 500 (Server Error)**: Vérifiez les logs du serveur pour plus de détails

### Erreurs liées aux données Orekit

Les calculs d'éclipses nécessitent que l'application ait accès à des fichiers de données Orekit spécifiques :

1. **UTC-TAI.history** : Contient l'historique des écarts entre les échelles de temps UTC et TAI
2. **eopc04_IAU2000.62-now** : Contient les paramètres d'orientation de la Terre (EOP)

Si vous rencontrez des erreurs 500 liées à ces fichiers, vérifiez :

- Que le dossier `orekit-data` existe à la racine de l'application
- Que les fichiers sont présents et correctement formatés
- Que la propriété système `orekit.data.path` pointe vers le bon répertoire

### Messages d'erreur spécifiques Orekit

| Message d'erreur | Cause probable | Solution |
|------------------|---------------|----------|
| "no IERS UTC-TAI history data loaded" | Fichier UTC-TAI.history manquant ou mal formaté | Vérifiez le format et le contenu du fichier UTC-TAI.history |
| "no entries found in IERS UTC-TAI history file" | Format incorrect du fichier UTC-TAI.history | Le fichier doit contenir des dates au format Julian date et des offsets |
| "unable to read IERS Earth Orientation Parameters" | Fichier eopc04_IAU2000.62-now manquant ou mal formaté | Vérifiez le format et le contenu du fichier EOP |

### Comment Debugger

Si vous rencontrez des problèmes, vérifiez:

1. Que le serveur est en cours d'exécution
2. Que les données TLE sont valides
3. Que la période demandée n'est pas trop longue (essayez de la réduire à 24-48 heures)
4. Les logs du serveur pour des indications sur l'erreur (augmentez le niveau de log à DEBUG pour plus de détails)
5. Que les fichiers de données Orekit sont correctement configurés et accessibles
