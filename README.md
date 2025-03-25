# Calculateur de Périodes d'Eclipse Satellite

Ce projet est un microservice Spring Boot qui utilise la bibliothèque Orekit pour calculer les périodes d'éclipse d'un satellite à partir de ses données TLE (Two-Line Element set). Il est conçu pour s'intégrer avec le simulateur de batterie satellite existant.

## Fonctionnalités

- Calcul précis des périodes d'éclipse (entrée et sortie d'ombre)
- Utilisation des TLE pour la propagation d'orbite
- Intégration avec les API REST du simulateur de batterie
- Téléchargement automatique des données Orekit nécessaires

## Prérequis

- Java 17
- Maven 3.6+
- Accès Internet (pour le téléchargement initial des données Orekit)

## Installation et Démarrage

1. Cloner le dépôt
```bash
git clone <url-du-repo>
cd satellite-eclipse-calculator
```

2. Compiler le projet
```bash
mvn clean install
```

3. Exécuter l'application
```bash
mvn spring-boot:run
```

4. Pour exécuter avec l'exemple
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=example
```

## Utilisation

### API REST

L'API expose un endpoint principal pour le calcul des périodes d'éclipse:

**POST** `/satellite-eclipse/api/eclipse/calculate`

Exemple de requête:
```json
{
  "tleData": {
    "satelliteName": "ISS (ZARYA)",
    "line1": "1 25544U 98067A   22085.41476591  .00007277  00000-0  13908-3 0  9990",
    "line2": "2 25544  51.6449 336.4797 0005408  61.7847  47.9568 15.49454906334473"
  },
  "startDate": "2025-03-25T00:00:00Z",
  "endDate": "2025-03-26T00:00:00Z",
  "stepInSeconds": 60
}
```

Exemple de réponse:
```json
[
  {
    "entryTime": "2025-03-25T01:23:45Z",
    "exitTime": "2025-03-25T02:06:30Z",
    "durationMinutes": 42.75,
    "eclipseType": "umbra"
  },
  {
    "entryTime": "2025-03-25T02:59:15Z",
    "exitTime": "2025-03-25T03:41:20Z",
    "durationMinutes": 42.08,
    "eclipseType": "umbra"
  }
]
```

### Intégration avec le Simulateur de Batterie

Pour intégrer ce service avec le simulateur de batterie satellite existant:

1. Utilisez le client HTTP de Spring pour appeler l'API depuis le simulateur principal
2. Utilisez les périodes d'éclipse pour moduler la charge solaire dans la simulation
3. Synchronisez les périodes d'éclipse avec les cycles charge/décharge de la batterie

Exemple d'intégration:
```java
@Service
public class BatterySimulationService {
    
    private final RestTemplate restTemplate;
    private final String eclipseServiceUrl = "http://localhost:8081/satellite-eclipse/api/eclipse/calculate";
    
    // ...
    
    public SimulationResult simulateBatteryWithEclipses(SimulationParameters params) {
        // Préparer la requête TLE
        EclipseRequest eclipseRequest = new EclipseRequest(
            params.getTleData(),
            params.getStartTime(),
            params.getEndTime(),
            params.getSimulationStepSeconds()
        );
        
        // Appeler le service d'éclipse
        EclipsePeriod[] eclipsePeriods = restTemplate.postForObject(
            eclipseServiceUrl, 
            eclipseRequest, 
            EclipsePeriod[].class);
        
        // Utiliser les périodes d'éclipse pour la simulation de batterie
        // ...
        
        return results;
    }
}
```

## Structure du Projet

- `model` - Classes de modèle (TLE, périodes d'éclipse)
- `service` - Services de calcul et utilitaires Orekit
- `controller` - API REST
- `examples` - Exemples d'utilisation

## Personnalisation

Le service peut être configuré via le fichier `application.properties`:

```properties
# Port du serveur (modifiez au besoin pour éviter les conflits)
server.port=8081

# Chemin des données Orekit
orekit.data.path=src/main/resources/orekit-data
```

## Dépannage

Si vous rencontrez des problèmes avec les données Orekit:

1. Vérifiez que le téléchargement automatique a fonctionné
2. Vous pouvez manuellement télécharger les données depuis [le dépôt Gitlab d'Orekit](https://gitlab.orekit.org/orekit/orekit-data/-/archive/master/orekit-data-master.zip)
3. Extrayez le fichier ZIP dans le dossier `src/main/resources/orekit-data`
