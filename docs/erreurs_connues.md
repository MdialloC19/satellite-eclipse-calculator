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
   - Les dates doivent être au format julien (et non au format ISO)
   - Chaque ligne doit contenir une date et un offset TAI-UTC
   - Exemple de format correct :
     ```
     2441317.5  10.0
     2441499.5  11.0
     2441683.5  12.0
     ```
3. Redémarrer l'application après avoir corrigé les fichiers

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

## Meilleures pratiques pour éviter les erreurs

1. **Toujours vérifier les données Orekit** avant de démarrer l'application
2. **Utiliser des TLE récents** pour les calculs d'éclipses
3. **Limiter la période de calcul** à quelques jours pour éviter les erreurs de propagation
4. **Augmenter le niveau de log** pour faciliter le diagnostic des problèmes :
   ```bash
   mvn spring-boot:run -Dspring-boot.run.arguments="--logging.level.com.satellite.eclipse=DEBUG --logging.level.org.orekit=DEBUG"
   ```
5. **Valider le format des requêtes** avant de les envoyer à l'API

## Historique des problèmes résolus

### Problème 1: Fichiers de données Orekit mal formatés

**Contexte** : L'application rencontrait systématiquement une erreur 500 lors des calculs d'éclipses en raison de fichiers de données Orekit incorrectement formatés.

**Solution appliquée** : 
- Reformatage du fichier UTC-TAI.history avec des dates en format julien
- Mise à jour du fichier eopc04_IAU2000.62-now avec des données réalistes
- Correction de la gestion des chemins de données dans OrekitDataLoader

**Date de résolution** : Mars 2025
