# Guide de Dépannage : Erreurs Connues et Solutions

Ce document détaille les erreurs couramment rencontrées lors de l'utilisation du Satellite Eclipse Calculator, leurs causes et les solutions recommandées.

## Erreurs relatives aux données Orekit

Les erreurs les plus fréquentes concernent les données Orekit nécessaires au calcul des éclipses.

### 1. Erreur 500 - "no IERS UTC-TAI history data loaded"

**Problème** : L'application ne peut pas trouver ou charger les données d'historique des écarts entre les échelles de temps UTC et TAI.

**Symptômes** :

- Erreur 500 lors de l'appel à l'API `/eclipse/calculate`
- Message d'erreur dans les logs mentionnant "no IERS UTC-TAI history data loaded"

**Causes possibles** :

- Le fichier `UTC-TAI.history` est absent du répertoire `orekit-data`
- Le fichier `UTC-TAI.history` existe mais son format est incorrect
- Le chemin vers le répertoire des données Orekit est mal configuré

**Solutions** :
1. Vérifier l'existence du fichier dans le répertoire `orekit-data`
2. S'assurer que le fichier respecte le format attendu par Orekit :
   - Le fichier doit utiliser le format officiel de l'IERS (International Earth Rotation and Reference Systems Service)
   - Utiliser de préférence le format directement depuis la source officielle : [UTC-TAI.history](https://hpiers.obspm.fr/iers/bul/bulc/UTC-TAI.history)
   - Exemple de format correct (extrait) :
     ```
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
     ```
   - **Important** : Orekit attend exactement ce format spécifique et non un format générique de dates juliennes
3. Redémarrer l'application après avoir corrigé les fichiers

**Résolution confirmée** :
Nous avons résolu ce problème en remplaçant le contenu du fichier UTC-TAI.history par les données officielles de l'IERS, en conservant exactement le format d'origine. Ne pas modifier ou simplifier ce format, même si certaines sections semblent être des commentaires ou de la documentation.

### 2. Erreur 500 - "unable to read IERS Earth Orientation Parameters"

**Problème** : L'application ne peut pas lire ou interpréter les paramètres d'orientation de la Terre.

**Symptômes** :

- Erreur 500 lors de l'appel à l'API
- Message d'erreur contenant "unable to read IERS Earth Orientation Parameters"

**Causes possibles** :

- Le fichier `eopc04_IAU2000.62-now` est absent
- Le fichier existe mais son format est incorrect
- Les données sont incomplètes ou corrompues

**Solutions** :
1. Vérifier l'existence du fichier `eopc04_IAU2000.62-now` dans le répertoire `orekit-data`
2. S'assurer que le fichier respecte le format attendu :
   - Chaque ligne doit contenir une date MJD et plusieurs paramètres d'orientation
   - Format attendu : `MJD TAI-UTC xp yp UT1-UTC LOD dPsi dEpsilon dX dY`
   - Exemple :
     ```
     59669 37.0 0.043170 0.377392 0.0340052 0.0017792 -0.002710 -0.005254 -0.000239 -0.000268
     59670 37.0 0.042724 0.377892 0.0339954 0.0024000 -0.002708 -0.005220 -0.000240 -0.000270
     ```
3. Redémarrer l'application après correction

## Erreurs liées aux calculs d'éclipses

### 3. Erreur 500 - "TLE orbit date is outside of covered range"

**Problème** : La date d'époque du TLE est trop éloignée de la période demandée pour le calcul.

**Symptômes** :
- Erreur 500 avec message "TLE orbit date is outside of covered range"

**Causes possibles** :
- Le TLE fourni est trop ancien par rapport à la période de calcul demandée
- Les dates de début/fin sont trop éloignées de la date d'époque du TLE

**Solutions** :
1. Utiliser un TLE plus récent ou plus proche de la période demandée
2. Réduire l'écart entre les dates de début/fin et la date d'époque du TLE
3. Pour les tests, utiliser une période de calcul proche de la date du TLE (quelques jours maximum)

### 4. Erreur 400 - "Invalid TLE format"

**Problème** : Le format des données TLE fournies est incorrect.

**Symptômes** :
- Erreur 400 (Bad Request)
- Message d'erreur mentionnant un problème de format TLE

**Causes possibles** :
- Caractères manquants ou supplémentaires dans les lignes TLE
- Checksums incorrects
- Format général non conforme au standard TLE

**Solutions** :
1. Vérifier les lignes TLE avec un validateur en ligne
2. S'assurer que chaque ligne respecte exactement le format TLE standard :
   - Première ligne commence par "1" suivi de l'identifiant satellite
   - Deuxième ligne commence par "2" suivi de l'identifiant satellite
   - Les deux lignes doivent avoir exactement 69 caractères chacune

## Erreurs de configuration de l'application

### 5. Connexion refusée sur le port 8081

**Problème** : Impossible de se connecter à l'API.

**Symptômes** :
- Erreur "Connection refused" lors des appels à l'API
- Messages "Unable to connect to localhost:8081"

**Causes possibles** :
- L'application n'est pas démarrée
- Un autre processus utilise déjà le port 8081
- Problèmes de permissions réseau

**Solutions** :
1. Vérifier que l'application est bien en cours d'exécution
2. Examiner les logs de démarrage pour voir si l'application a démarré correctement
3. Vérifier si le port 8081 est déjà utilisé avec la commande :
   ```bash
   lsof -i :8081
   ```
4. Si nécessaire, changer le port dans `application.properties` :
   ```properties
   server.port=8082
   ```

### 6. Erreur dans le format de la requête API

**Problème** : Discordance entre le format attendu par l'API et le format fourni dans la requête.

**Symptômes** :

- Erreur 400 Bad Request
- Message d'erreur indiquant un problème de désérialisation JSON

**Causes possibles** :

- Structure incorrecte de l'objet JSON envoyé à l'API
- Champs manquants ou noms de champs incorrects
- Format de date invalide

**Solutions** :

1. Vérifier que l'objet JSON respecte strictement le format attendu par l'API

2. Le format correct pour une requête de calcul d'éclipse est :

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

3. S'assurer que les dates sont au format ISO 8601 (YYYY-MM-DDThh:mm:ssZ)

## Meilleures pratiques pour éviter les erreurs

1. **Toujours vérifier les données Orekit** avant de démarrer l'application

2. **Télécharger les fichiers Orekit depuis les sources officielles** plutôt que de les créer manuellement

3. **Utiliser des TLE récents** pour les calculs d'éclipses

4. **Limiter la période de calcul** à quelques jours pour éviter les erreurs de propagation

5. **Tester l'API avec des données connues** avant de l'intégrer dans d'autres systèmes

6. **Augmenter le niveau de log** pour faciliter le diagnostic des problèmes :

   ```bash
   mvn spring-boot:run -Dspring-boot.run.arguments="--logging.level.com.satellite.eclipse=DEBUG --logging.level.org.orekit=DEBUG"
   ```

7. **Consulter régulièrement les mises à jour** des fichiers Orekit pour s'assurer d'utiliser les données les plus récentes

8. **Valider le format des requêtes** avant de les envoyer à l'API

## Historique des problèmes résolus

### Problème 1: Fichiers de données Orekit mal formatés

**Contexte** : L'application rencontrait systématiquement une erreur 500 lors des calculs d'éclipses en raison de fichiers de données Orekit incorrectement formatés.

**Solution appliquée** : 
- Reformatage du fichier UTC-TAI.history avec des dates en format julien
- Mise à jour du fichier eopc04_IAU2000.62-now avec des données réalistes
- Correction de la gestion des chemins de données dans OrekitDataLoader

**Date de résolution** : Mars 2025
