# Architecture et Guide d'Intégration du Calculateur d'Éclipses Satellite

## Table des matières
1. [Vue d'ensemble du projet](#vue-densemble-du-projet)
2. [Architecture technique](#architecture-technique)
3. [Comprendre les calculs d'éclipses](#comprendre-les-calculs-déclipses)
4. [Configuration et Prérequis](#configuration-et-prérequis)
5. [Utilisation du service](#utilisation-du-service)
6. [Intégration dans un simulateur](#intégration-dans-un-simulateur)
7. [Dépannage et Questions Fréquentes](#dépannage-et-questions-fréquentes)

## Vue d'ensemble du projet

Le **Satellite Eclipse Calculator** est un service Spring Boot qui permet de calculer les périodes d'éclipses pour un satellite en orbite autour de la Terre. Une éclipse se produit lorsque le satellite entre dans l'ombre de la Terre, ce qui peut affecter son fonctionnement, notamment en ce qui concerne l'alimentation solaire.

Ce service utilise la bibliothèque **Orekit** (Open-source REusable KIT for space flight dynamics) pour effectuer les calculs orbitaux et de détection d'éclipses. Orekit est une bibliothèque complète et précise pour les calculs spatiaux, incluant la propagation d'orbites, les calculs de visibilité, les éclipses, etc.

### Objectifs du projet

- Fournir une API REST permettant de calculer les périodes d'éclipses pour un satellite
- Simplifier l'intégration de calculs orbitaux complexes dans des applications tierces
- Offrir une solution facile à déployer et à maintenir

## Architecture technique

Le projet est structuré selon le modèle classique d'une application Spring Boot, avec une architecture en couches:

```
satellite-eclipse-calculator/
├── src/main/java/com/satellite/eclipse/
│   ├── controllers/        # Contrôleurs REST
│   ├── dto/                # Objets de transfert de données
│   ├── exceptions/         # Gestionnaires d'exceptions
│   ├── model/              # Modèle de données
│   ├── service/            # Services métier
│   │   └── EclipseCalculatorService.java  # Service principal de calcul
│   ├── utils/              # Utilitaires
│   │   └── OrekitDataDownloader.java  # Téléchargement/création des données Orekit
│   └── SatelliteEclipseCalculatorApplication.java  # Point d'entrée
├── src/main/resources/
│   └── application.properties  # Configuration
├── orekit-data/           # Répertoire des données Orekit (création automatique)
│   ├── UTC-TAI.history    # Historique des écarts entre UTC et TAI
│   └── eopc04_IAU2000.62-now  # Paramètres d'orientation de la Terre
└── pom.xml                # Fichier de configuration Maven
```

### Composants principaux

1. **Controller**: Point d'entrée des requêtes REST
2. **Service**: Couche métier contenant la logique de calcul
3. **DTO**: Objets de transfert de données pour les requêtes/réponses
4. **Utils**: Utilitaires comme OrekitDataDownloader

### Flux de données

1. Le client envoie une requête HTTP avec les données TLE (Two-Line Element) du satellite
2. Le contrôleur reçoit la requête et transmet les données au service
3. Le service utilise Orekit pour:
   - Initialiser les modèles physiques (gravité, atmosphère, etc.)
   - Créer l'orbite du satellite à partir des TLE
   - Configurer le détecteur d'éclipses
   - Propager l'orbite sur la période demandée
   - Collecter les événements d'éclipses
4. Les résultats sont formatés et renvoyés au client

## Comprendre les calculs d'éclipses

### Concepts de base

#### Two-Line Element (TLE)

Un TLE est un format de données standardisé qui décrit l'orbite d'un satellite. Il contient des informations telles que:
- L'époque (date/heure de référence)
- L'inclinaison
- Le demi-grand axe
- L'excentricité
- L'argument du périgée
- La longitude du nœud ascendant
- L'anomalie moyenne

Exemple de TLE:
```
ISS (ZARYA)
1 25544U 98067A   21086.42859439  .00000318  00000-0  16195-4 0  9991
2 25544  51.6445 354.6522 0003156  48.6176  25.4760 15.48939243277469
```

#### Éclipses

Une éclipse de satellite se produit lorsque celui-ci entre dans l'ombre de la Terre. Il existe deux types d'ombres:
- **Ombre**: La région où la lumière du Soleil est complètement bloquée
- **Pénombre**: La région où la lumière du Soleil est partiellement bloquée

L'algorithme de détection d'éclipses utilise des calculs géométriques pour déterminer si le satellite se trouve dans l'ombre ou la pénombre de la Terre à un moment donné.

### Comment fonctionne le calcul d'éclipses

1. **Initialisation des modèles physiques**: Configuration des modèles gravitationnels, atmosphériques et autres modèles perturbateurs
2. **Création de l'orbite**: Conversion des TLE en éléments orbitaux Orekit
3. **Configuration du propagateur**: Mise en place du propagateur qui fera évoluer l'orbite dans le temps
4. **Configuration du détecteur d'éclipses**: Mise en place de l'`EclipseDetector` d'Orekit qui détectera les entrées et sorties d'éclipses
5. **Propagation**: Simulation de l'évolution de l'orbite sur la période demandée
6. **Collecte des événements**: Enregistrement des moments où le satellite entre et sort de l'ombre

La classe `EclipseCalculatorService` implémente cette logique en utilisant Orekit:

```java
// Configuration du détecteur d'éclipses
PVCoordinatesProvider sun = CelestialBodyFactory.getSun();
PVCoordinatesProvider earth = CelestialBodyFactory.getEarth();
EclipseDetector eclipseDetector = new EclipseDetector(sun, EARTH_RADIUS, earth);

// Définition du gestionnaire d'événements
EventHandler<EclipseDetector> eclipseHandler = new EventHandler<EclipseDetector>() {
    @Override
    public Action eventOccurred(SpacecraftState s, EclipseDetector detector, boolean increasing) {
        // L'événement est enregistré ici
        // increasing=true signifie sortie d'éclipse
        // increasing=false signifie entrée en éclipse
        return Action.CONTINUE;
    }
};

// Configuration du propagateur
propagator.addEventDetector(eclipseDetector.withHandler(eclipseHandler));
```

## Configuration et Prérequis

### Dépendances

Les principales dépendances du projet sont:

- **Spring Boot**: Framework Java pour créer des applications autonomes basées sur Spring
- **Orekit 11.3.3**: Bibliothèque pour les calculs de mécanique spatiale
- **Hipparchus 2.0**: Bibliothèque de mathématiques utilisée par Orekit
- **Spring Data JPA**: Pour la persistance des données
- **H2 Database**: Base de données en mémoire pour le développement

### Installation

1. Cloner le dépôt
```bash
git clone https://github.com/votre-organisation/satellite-eclipse-calculator.git
cd satellite-eclipse-calculator
```

2. Compiler le projet
```bash
mvn clean install
```

### Démarrage de l'application

#### Méthode 1: Utilisation de Maven

```bash
# Démarrage standard
mvn spring-boot:run

# Démarrage avec logs détaillés (recommandé pour le débogage)
mvn spring-boot:run -Dspring-boot.run.arguments="--logging.level.com.satellite.eclipse=DEBUG --logging.level.org.orekit=DEBUG"
```

#### Méthode 2: Utilisation du JAR

```bash
# Générer le JAR exécutable
mvn clean package

# Exécuter le JAR
java -jar target/satellite-eclipse-calculator-0.0.1-SNAPSHOT.jar

# Exécuter avec des logs détaillés
java -jar target/satellite-eclipse-calculator-0.0.1-SNAPSHOT.jar --logging.level.com.satellite.eclipse=DEBUG --logging.level.org.orekit=DEBUG
```

Le serveur démarre sur le port 8081 avec le contexte `/satellite-eclipse`.

### Séquence de démarrage et chargement des données

Lors du démarrage de l'application, les étapes suivantes se produisent:

1. Initialisation du contexte Spring Boot
2. Exécution de la méthode `initialize()` du service `OrekitDataLoader` (annotation `@PostConstruct`)
3. Vérification de l'existence du répertoire `orekit-data` et création si nécessaire
4. Vérification de la présence des fichiers essentiels (`UTC-TAI.history` et `eopc04_IAU2000.62-now`)
5. Configuration du `DataProvidersManager` d'Orekit pour utiliser les données du répertoire
6. Démarrage du serveur web et mise à disposition de l'API

```
[DÉMARRAGE] → [VÉRIFICATION DONNÉES] → [CHARGEMENT OREKIT] → [DÉMARRAGE API]
```

Les logs afficheront les messages du processus de chargement des données Orekit, y compris les éventuels avertissements ou erreurs.

### Configuration des données Orekit

#### Structure du répertoire orekit-data

```
orekit-data/
├── UTC-TAI.history         # Histoire des écarts entre UTC et TAI
└── eopc04_IAU2000.62-now   # Paramètres d'orientation de la Terre
```

#### Méthodes d'obtention des données

1. **Création automatique**: Le composant `OrekitDataDownloader` crée automatiquement un ensemble minimal de données lors du premier démarrage.

2. **Téléchargement manuel**: Pour des données plus précises, vous pouvez:
   - Télécharger manuellement les données Orekit depuis [le dépôt officiel](https://gitlab.orekit.org/orekit/orekit-data)
   - Placer ces données dans le répertoire `orekit-data` à la racine du projet

3. **Format personnalisé**: Si vous avez besoin de créer manuellement les fichiers:
   - Le fichier `UTC-TAI.history` doit contenir les écarts entre UTC et TAI au format de date julienne
   - Le fichier `eopc04_IAU2000.62-now` doit contenir les paramètres d'orientation de la Terre

> **Important**: La précision des calculs d'éclipses dépend directement de la qualité des données Orekit utilisées.

## Utilisation du service

### Endpoints API

Le service expose les endpoints REST suivants:

#### Calcul d'éclipses

```
POST /satellite-eclipse/api/eclipse/calculate
```

Corps de la requête:
```json
{
  "tle": [
    "ISS (ZARYA)",
    "1 25544U 98067A   21086.42859439  .00000318  00000-0  16195-4 0  9991",
    "2 25544  51.6445 354.6522 0003156  48.6176  25.4760 15.48939243277469"
  ],
  "startDate": "2023-01-01T00:00:00Z",
  "endDate": "2023-01-02T00:00:00Z"
}
```

Réponse:
```json
{
  "satellite": "ISS (ZARYA)",
  "eclipses": [
    {
      "entryTime": "2023-01-01T01:23:45Z",
      "exitTime": "2023-01-01T02:01:15Z",
      "durationMinutes": 37.5
    },
    {
      "entryTime": "2023-01-01T14:56:30Z",
      "exitTime": "2023-01-01T15:34:20Z",
      "durationMinutes": 37.83
    }
  ],
  "totalEclipseDurationMinutes": 75.33
}
```

### Exemple d'utilisation en Java

Pour les clients Java, vous pouvez utiliser RestTemplate ou WebClient:

```java
import org.springframework.web.client.RestTemplate;
// ...

RestTemplate restTemplate = new RestTemplate();
String url = "http://localhost:8081/satellite-eclipse/api/eclipse/calculate";

EclipseRequest request = new EclipseRequest();
// Configuration de la requête avec les TLE et dates...

EclipseResponse response = restTemplate.postForObject(url, request, EclipseResponse.class);
// Traitement de la réponse...
```

## Intégration dans un simulateur

Pour intégrer le service de calcul d'éclipses dans un simulateur existant, vous avez plusieurs options:

### 1. Intégration via API REST

C'est l'approche la plus simple qui maintient une séparation claire entre les services:

1. Déployez le service Eclipse Calculator en tant que service indépendant
2. Dans votre simulateur, effectuez des appels REST au service lorsque vous avez besoin de calculer des éclipses
3. Intégrez les résultats dans votre simulation

**Avantages**:
- Faible couplage
- Évolutivité indépendante
- Facilité de mise à jour

**Inconvénients**:
- Latence réseau
- Dépendance à un service externe

### 2. Intégration sous forme de bibliothèque

Vous pouvez également intégrer directement le code dans votre simulateur:

1. Extrayez la classe `EclipseCalculatorService` et ses dépendances
2. Créez un module Maven/Gradle dans votre projet
3. Utilisez directement les classes pour effectuer les calculs

**Avantages**:
- Pas de latence réseau
- Pas de dépendance à un service externe

**Inconvénients**:
- Couplage plus fort
- Gestion des dépendances plus complexe

### 3. Intégration par événements

Pour les architectures basées sur les événements:

1. Configurez le service Eclipse Calculator pour publier des événements d'éclipse dans un broker (Kafka, RabbitMQ)
2. Abonnez votre simulateur à ces événements

**Avantages**:
- Découplage
- Communication asynchrone

**Exemple d'intégration dans le code du simulateur**:

```java
// Création d'une instance du service de calcul
EclipseCalculatorService eclipseService = new EclipseCalculatorService();

// Configuration des données Orekit
System.setProperty("orekit.data.path", "/chemin/vers/orekit-data");

// Calcul des éclipses pour un satellite
List<String> tle = Arrays.asList(
    "ISS (ZARYA)",
    "1 25544U 98067A   21086.42859439  .00000318  00000-0  16195-4 0  9991",
    "2 25544  51.6445 354.6522 0003156  48.6176  25.4760 15.48939243277469"
);

ZonedDateTime startDate = ZonedDateTime.now();
ZonedDateTime endDate = startDate.plusDays(1);

List<EclipsePeriod> eclipses = eclipseService.calculateEclipsePeriods(tle, startDate, endDate);

// Utilisation des résultats dans le simulateur
for (EclipsePeriod eclipse : eclipses) {
    simulateur.programmerEvenement(eclipse.getEntryTime(), "DEBUT_ECLIPSE", satellite);
    simulateur.programmerEvenement(eclipse.getExitTime(), "FIN_ECLIPSE", satellite);
    
    // Ajuster la production d'énergie solaire du satellite pendant l'éclipse
    simulateur.ajusterProduction(satellite, eclipse.getEntryTime(), eclipse.getExitTime(), 0.0);
}
```

## Dépannage et Questions Fréquentes

### Problèmes courants

#### Q: J'obtiens une erreur "Orekit data not found"
**R**: Assurez-vous que:
1. Le répertoire `orekit-data` existe
2. Les fichiers de données Orekit sont présents dans ce répertoire
3. La propriété système `orekit.data.path` pointe vers ce répertoire

#### Q: Les calculs d'éclipses ne correspondent pas à mes attentes
**R**: Vérifiez:
1. La validité et la fraîcheur des TLE utilisés
2. La configuration des modèles physiques (dans `EclipseCalculatorService`)
3. Les dates demandées (vérifiez les fuseaux horaires)

#### Q: Les performances sont insuffisantes pour mon cas d'utilisation
**R**: Considérez:
1. Mettre en cache les résultats pour des TLE fréquemment utilisés
2. Ajuster la précision des calculs (pas de temps, modèles utilisés)
3. Paralléliser les calculs pour plusieurs satellites

### Limites de précision

Il est important de comprendre que:
1. La précision des calculs dépend de la fraîcheur des TLE
2. Les TLE sont généralement précis pendant quelques jours à quelques semaines
3. Pour une précision maximale, utilisez des TLE récents
4. Les modèles physiques utilisés ont leurs propres limitations

### Ressources supplémentaires

- [Documentation Orekit](https://www.orekit.org/site-orekit-latest/index.html)
- [Tutoriels Orekit](https://www.orekit.org/site-orekit-latest/tutorials/index.html)
- [Understanding TLEs](https://celestrak.org/NORAD/documentation/tle-fmt.php)
- [Space-Track](https://www.space-track.org/) (source de TLE actualisés)
- [Principles of Orbital Mechanics](https://en.wikipedia.org/wiki/Orbital_mechanics)

---

Ce document a été créé pour faciliter la compréhension et l'intégration du service de calcul d'éclipses satellites. Pour toute question supplémentaire, veuillez contacter l'équipe de développement.
