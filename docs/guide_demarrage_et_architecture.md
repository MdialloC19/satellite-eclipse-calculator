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

```text
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

   ```text
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

   **Format critique** : Ce fichier doit **exactement** respecter le format officiel de l'IERS :

   ```text
   ---------------
   UTC-TAI.history
   ---------------
   RELATIONSHIP BETWEEN TAI AND UTC
   ------------------------------------------------------------------------------- 
   Limits of validity(at 0h UTC)       TAI - UTC  
   
   1961  Jan.  1 - 1961  Aug.  1     1.422 818 0s + (MJD - 37 300) x 0.001 296s
         Aug.  1 - 1962  Jan.  1     1.372 818 0s +        ""
   1962  Jan.  1 - 1963  Nov.  1     1.845 858 0s + (MJD - 37 665) x 0.001 123 2s
   1963  Nov.  1 - 1964  Jan.  1     1.945 858 0s +        ""
   ...
   ```
   
   **Éléments importants** :
   - Télécharger le fichier directement depuis la source officielle : [UTC-TAI.history](https://hpiers.obspm.fr/iers/bul/bulc/UTC-TAI.history)
   - **Ne pas modifier** le format, même si certaines sections semblent être des commentaires
   - Conserver les en-têtes, les tirets, les espaces et le formatage exact
   - **À savoir** : Un format incorrect de ce fichier est la cause la plus fréquente d'erreurs 500 lors des appels API

2. **eopc04_IAU2000.62-now** : Contient les paramètres d'orientation de la Terre (EOP).

   Format attendu :

   ```text
   # Date MJD      TAI-UTC  x_pole   y_pole   UT1-UTC   LOD     dPsi     dEpsilon   dX       dY
   # (0h UTC)      (s)      (")      (")      (s)       (s)     (mas)    (mas)      (mas)    (mas)
   59669 37.0 0.043170 0.377392 0.0340052 0.0017792 -0.002710 -0.005254 -0.000239 -0.000268
   59670 37.0 0.042724 0.377892 0.0339954 0.0024000 -0.002708 -0.005220 -0.000240 -0.000270
   ...
   ```
   
   **Source officielle** : [Fichiers IERS EOP](https://hpiers.obspm.fr/iers/eop/eopc04)

### Flux de chargement des données

Lors du démarrage de l'application, la séquence suivante s'exécute :

1. La classe `OrekitDataLoader` est initialisée (via `@PostConstruct`)
2. Vérification de l'existence du répertoire `orekit-data`
   - Si absent, création du répertoire
3. Vérification de la présence des fichiers essentiels
   - Si manquants, un avertissement est émis dans les logs
4. Configuration du `DataProvidersManager` d'Orekit avec le répertoire
5. Les données sont chargées et disponibles pour les calculs

**Note concernant l'initialisation** : Lors de cette phase, Orekit parcourt les fichiers dans le répertoire `orekit-data` et les analyse en fonction de leur nom et de leur extension. Si un fichier est présent mais mal formaté, l'application peut démarrer mais échouera plus tard lors des calculs. C'est pourquoi il est essentiel de s'assurer que les fichiers ont le bon format.

### Solutions aux problèmes courants

Si les données Orekit ne sont pas correctement chargées ou si vous rencontrez des erreurs 500 lors de l'utilisation de l'API :

1. **Erreur 'no IERS UTC-TAI history data loaded'** :
   - Téléchargez à nouveau le fichier `UTC-TAI.history` depuis la source officielle
   - Ne modifiez pas son format, même si certaines parties semblent être des commentaires ou des en-têtes
   - Consultez notre [document sur les erreurs connues](./erreurs_connues.md) pour plus de détails

2. **Vérifier l'existence des fichiers** :

   ```bash
   ls -la satellite-eclipse-calculator/orekit-data/
   ```

3. **Vérifier que les fichiers n'ont pas d'extensions supplémentaires** : 
   - Par exemple, si le fichier a été téléchargé comme `UTC-TAI.history.txt`, renommez-le en `UTC-TAI.history`

4. **Vérifier les permissions** : Les fichiers doivent être lisibles

   ```bash
   chmod 644 satellite-eclipse-calculator/orekit-data/*
   ```

5. **Redémarrer l'application après avoir corrigé les fichiers** :

   ```bash
   mvn spring-boot:run
   ```

**Important** : Nous avons constaté que les erreurs les plus fréquentes sont liées au format du fichier `UTC-TAI.history`. Dans notre cas, la résolution a consisté à utiliser le fichier original sans modification.

## Points d'API

L'application expose les points d'API suivants :

### Calcul d'éclipses

```http
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

**Exemple testé et fonctionnel** avec le satellite NOAA 19 :

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

**Points importants** :
- Assurez-vous que le champ parent pour les données TLE est `"tle"` (pas `"tleData"` comme dans certains exemples)
- Les dates doivent être au format ISO 8601 avec le 'Z' indiquant UTC

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

```text
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
