# Guide de Démarrage et Architecture

Ce document explique l'architecture complète du Satellite Eclipse Calculator, comment démarrer l'application et comment gérer correctement les données Orekit.

## Table des matières

1. [Architecture du système](#architecture-du-système)
2. [Comment démarrer l'application](#comment-démarrer-lapplication)
3. [Gestion des données Orekit](#gestion-des-données-orekit)
4. [Points d'API](#points-dapi)
5. [Flux d'exécution](#flux-dexécution)

## Architecture du système

### Vue d'ensemble

Le **Satellite Eclipse Calculator** est une application Spring Boot qui fournit une API REST pour calculer les périodes d'éclipses des satellites en orbite terrestre. L'application utilise la bibliothèque Orekit pour effectuer les calculs orbitaux et de détection d'éclipses.

### Structure du projet

```
satellite-eclipse-calculator/
├── src/                           # Code source
│   ├── main/java/com/satellite/eclipse/
│   │   ├── controller/            # Contrôleurs REST
│   │   │   └── EclipseCalculatorController.java
│   │   ├── dto/                   # Objets de transfert de données
│   │   │   ├── EclipseRequest.java
│   │   │   ├── EclipseResponse.java
│   │   │   └── TleData.java
│   │   ├── exception/             # Gestion des exceptions
│   │   │   └── GlobalExceptionHandler.java
│   │   ├── model/                 # Modèles de données
│   │   │   └── EclipsePeriod.java
│   │   ├── service/               # Services métier
│   │   │   ├── EclipseCalculatorService.java
│   │   │   └── OrekitDataLoader.java
│   │   ├── utils/                 # Utilitaires
│   │   │   └── OrekitDataDownloader.java
│   │   └── SatelliteEclipseCalculatorApplication.java
│   └── main/resources/
│       └── application.properties # Configuration
├── orekit-data/                   # Données Orekit
│   ├── UTC-TAI.history            # Historique UTC-TAI
│   └── eopc04_IAU2000.62-now      # Paramètres d'orientation Terre
├── docs/                          # Documentation
└── pom.xml                        # Configuration Maven
```

### Composants clés et leurs rôles

#### 1. Controllers (Contrôleurs)

Le contrôleur `EclipseCalculatorController` expose l'API REST et gère les requêtes entrantes :
- Validation des données d'entrée
- Transmission des demandes au service
- Formatage des réponses

#### 2. Services (Services)

- `EclipseCalculatorService` : Implémente la logique de calcul des éclipses
  - Conversion des TLE en orbites Orekit
  - Configuration des détecteurs d'éclipses
  - Propagation des orbites et détection des éclipses
  
- `OrekitDataLoader` : Gère le chargement des données Orekit au démarrage
  - Vérification de l'existence des fichiers de données
  - Configuration du `DataProvidersManager` d'Orekit

#### 3. Utils (Utilitaires)

- `OrekitDataDownloader` : Gère le téléchargement ou la création des données Orekit
  - Téléchargement depuis les serveurs officiels
  - Création de fichiers minimaux en cas d'échec

#### 4. DTOs (Objets de transfert de données)

- `EclipseRequest` : Encapsule les données de requête (TLE, dates)
- `EclipseResponse` : Structure la réponse (périodes d'éclipses)
- `TleData` : Représente les données TLE du satellite

#### 5. Modèle (Model)

- `EclipsePeriod` : Représente une période d'éclipse avec ses dates d'entrée/sortie et sa durée

## Comment démarrer l'application

### Prérequis

- JDK 17 ou supérieur
- Maven 3.6 ou supérieur
- Au moins 512 Mo de RAM disponible
- Port 8081 disponible (ou configurer un autre port)

### Méthodes de démarrage

#### Option 1 : Démarrage avec Maven

```bash
# Démarrage standard
cd satellite-eclipse-calculator
mvn spring-boot:run

# Démarrage avec logs détaillés (recommandé pour le débogage)
mvn spring-boot:run -Dspring-boot.run.arguments="--logging.level.com.satellite.eclipse=DEBUG --logging.level.org.orekit=DEBUG"
```

#### Option 2 : Démarrage avec le JAR

```bash
# Compiler et créer le JAR
cd satellite-eclipse-calculator
mvn clean package

# Lancer l'application
java -jar target/satellite-eclipse-calculator-0.0.1-SNAPSHOT.jar

# Avec logs détaillés
java -jar target/satellite-eclipse-calculator-0.0.1-SNAPSHOT.jar --logging.level.com.satellite.eclipse=DEBUG --logging.level.org.orekit=DEBUG
```

### Vérification du démarrage

Pour confirmer que l'application a démarré correctement :

1. Vérifiez les logs de démarrage pour les messages :
   ```
   Données Orekit chargées avec succès
   Tomcat started on port 8081
   Started SatelliteEclipseCalculatorApplication in X.XXX seconds
   ```

2. Testez l'API avec une requête simple :
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

## Gestion des données Orekit

### Fichiers essentiels

L'application nécessite deux fichiers de données Orekit critiques :

1. **UTC-TAI.history** : Contient l'historique des écarts entre les échelles de temps UTC et TAI.
   
   Format attendu :
   ```
   # UTC-TAI.history
   # Dernière mise à jour : 2025-03-25
   # Format : date_julienne TAI-UTC_en_secondes
   2441317.5  10.0
   2441499.5  11.0
   2441683.5  12.0
   ...
   ```

2. **eopc04_IAU2000.62-now** : Contient les paramètres d'orientation de la Terre (EOP).
   
   Format attendu :
   ```
   # Date MJD      TAI-UTC  x_pole   y_pole   UT1-UTC   LOD     dPsi     dEpsilon   dX       dY
   # (0h UTC)      (s)      (")      (")      (s)       (s)     (mas)    (mas)      (mas)    (mas)
   59669 37.0 0.043170 0.377392 0.0340052 0.0017792 -0.002710 -0.005254 -0.000239 -0.000268
   59670 37.0 0.042724 0.377892 0.0339954 0.0024000 -0.002708 -0.005220 -0.000240 -0.000270
   ...
   ```

### Flux de chargement des données

Lors du démarrage de l'application, la séquence suivante s'exécute :

1. La classe `OrekitDataLoader` est initialisée (via `@PostConstruct`)
2. Vérification de l'existence du répertoire `orekit-data`
   - Si absent, création du répertoire
3. Vérification de la présence des fichiers essentiels
   - Si manquants, un avertissement est émis dans les logs
4. Configuration du `DataProvidersManager` d'Orekit avec le répertoire
5. Les données sont chargées et disponibles pour les calculs

### Solutions aux problèmes courants

Si les données Orekit ne sont pas correctement chargées :

1. **Vérifier l'existence des fichiers** :
   ```bash
   ls -la satellite-eclipse-calculator/orekit-data/
   ```

2. **Vérifier le format des fichiers** : Assurez-vous que les fichiers respectent les formats décrits ci-dessus

3. **Vérifier les permissions** : Les fichiers doivent être lisibles
   ```bash
   chmod 644 satellite-eclipse-calculator/orekit-data/*
   ```

4. **Recréer manuellement les fichiers** : Si nécessaire, utilisez les exemples ci-dessus comme modèles

## Points d'API

L'application expose les points d'API suivants :

### Calcul d'éclipses

```
POST /satellite-eclipse/api/eclipse/calculate
```

#### Paramètres de requête

Format JSON :
```json
{
  "tle": {
    "satelliteName": "NOM_DU_SATELLITE",
    "line1": "LIGNE_1_TLE",
    "line2": "LIGNE_2_TLE"
  },
  "startDate": "DATE_DEBUT_ISO8601",
  "endDate": "DATE_FIN_ISO8601"
}
```

#### Réponse

Format JSON :
```json
{
  "satellite": "NOM_DU_SATELLITE",
  "eclipses": [
    {
      "entryTime": "DATE_ENTREE_ISO8601",
      "exitTime": "DATE_SORTIE_ISO8601",
      "durationMinutes": DUREE_EN_MINUTES
    },
    ...
  ],
  "totalEclipseDurationMinutes": DUREE_TOTALE_EN_MINUTES
}
```

## Flux d'exécution

### Séquence de calcul des éclipses

1. **Réception de la requête** : Le contrôleur reçoit les données TLE et la période de calcul
2. **Validation des données** : Vérification du format TLE et des dates
3. **Conversion TLE** : Les données TLE sont converties en éléments orbitaux Orekit
4. **Configuration du propagateur** : Initialisation du propagateur numérique avec les forces perturatrices
5. **Configuration du détecteur d'éclipses** : Mise en place du détecteur d'éclipses d'Orekit
6. **Propagation** : Simulation de l'orbite sur la période demandée
7. **Collecte des événements** : Détection et enregistrement des entrées/sorties d'éclipses
8. **Formatage de la réponse** : Calcul des durées et formatage des résultats
9. **Retour au client** : Envoi de la réponse JSON

### Diagramme de séquence simplifié

```
Client           Controller                  Service                   Orekit
  |                 |                           |                         |
  |--- Requête ---->|                           |                         |
  |                 |---- Transmet données ---->|                         |
  |                 |                           |--- Initialise modèle -->|
  |                 |                           |                         |
  |                 |                           |--- Config détecteur --->|
  |                 |                           |                         |
  |                 |                           |--- Propage orbite ----->|
  |                 |                           |<---- Événements --------|
  |                 |                           |                         |
  |                 |<--- Retourne résultats ---|                         |
  |<-- Réponse -----|                           |                         |
  |                 |                           |                         |
```

## Conclusion

Ce guide vous a présenté l'architecture du Satellite Eclipse Calculator, comment démarrer l'application et comment gérer les données Orekit essentielles pour les calculs. Pour plus d'informations sur les erreurs courantes et leur résolution, consultez le document `erreurs_connues.md`.
