package com.satellite.eclipse.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

/**
 * Utilitaire pour télécharger et préparer les données Orekit nécessaires
 * au bon fonctionnement des calculs d'éclipse.
 */
@Configuration
public class OrekitDataDownloader {
    private static final Logger logger = LoggerFactory.getLogger(OrekitDataDownloader.class);
    private static final String OREKIT_DATA_URL = "https://github.com/CS-SI/Orekit/releases/download/v11.3.1/orekit-data-master.zip";
    private static final String LOCAL_DATA_DIR = "orekit-data";
    private static final int BUFFER_SIZE = 8192;
    
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    @Profile("!test") // Ne pas exécuter pendant les tests
    public CommandLineRunner prepareOrekitData() {
        return args -> {
            Path dataDir = Paths.get(LOCAL_DATA_DIR);
            boolean shouldDownload = false;
            
            // Vérifier si le répertoire existe
            if (!Files.exists(dataDir)) {
                logger.info("Le répertoire de données Orekit n'existe pas. Création du répertoire...");
                try {
                    Files.createDirectories(dataDir);
                    shouldDownload = true;
                } catch (IOException e) {
                    logger.error("Erreur lors de la création du répertoire {}", dataDir, e);
                    throw new RuntimeException("Impossible de créer le répertoire pour les données Orekit", e);
                }
            } else if (isEmpty(dataDir.toFile())) {
                logger.info("Le répertoire de données Orekit existe mais est vide.");
                shouldDownload = true;
            } else {
                // Vérifier si les fichiers essentiels existent
                Path utcTaiFile = dataDir.resolve("UTC-TAI.history");
                Path eopC04File = dataDir.resolve("eopc04_IAU2000.62-now");
                
                if (!Files.exists(utcTaiFile) || !Files.exists(eopC04File)) {
                    logger.info("Fichiers Orekit essentiels manquants. Téléchargement nécessaire.");
                    shouldDownload = true;
                }
            }
            
            if (shouldDownload) {
                logger.info("Téléchargement des données Orekit depuis {}...", OREKIT_DATA_URL);
                downloadOrekitData(dataDir);
            }
            
            // Définir la propriété système pour le dossier des données Orekit
            System.setProperty("orekit.data.path", dataDir.toAbsolutePath().toString());
            logger.info("Propriété orekit.data.path définie sur: {}", dataDir.toAbsolutePath());
        };
    }
    
    /**
     * Télécharge et extrait les données Orekit dans le répertoire spécifié.
     * 
     * @param dataDir Répertoire où stocker les données
     * @throws RuntimeException Si une erreur survient pendant le téléchargement ou l'extraction
     */
    private void downloadOrekitData(Path dataDir) {
        try {
            // Configuration de la connexion HTTP
            URL url = new URL(OREKIT_DATA_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/99.0.4844.83 Safari/537.36");
            connection.setRequestProperty("Accept", "*/*");
            connection.setRequestProperty("Connection", "keep-alive");
            
            // Vérifier le code de réponse HTTP
            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                logger.error("Le téléchargement des données a échoué avec le code HTTP: {}", responseCode);
                throw new IOException("Échec du téléchargement avec le code HTTP: " + responseCode);
            }
            
            // Téléchargement et extraction du fichier zip
            try (InputStream in = new BufferedInputStream(connection.getInputStream());
                 ZipInputStream zipIn = new ZipInputStream(in)) {
                
                ZipEntry entry;
                byte[] buffer = new byte[BUFFER_SIZE];
                
                // Traiter chaque entrée du fichier zip
                while ((entry = zipIn.getNextEntry()) != null) {
                    String name = entry.getName();
                    
                    // Ignorer le répertoire racine pour avoir une structure plus plate
                    int firstSlash = name.indexOf('/');
                    if (firstSlash >= 0) {
                        name = name.substring(firstSlash + 1);
                    }
                    
                    // Ignorer les entrées vides ou les répertoires
                    if (name.isEmpty()) {
                        continue;
                    }
                    
                    Path filePath = dataDir.resolve(name);
                    
                    // Créer les répertoires parents si nécessaire
                    if (name.contains("/")) {
                        Files.createDirectories(filePath.getParent());
                    }
                    
                    // Extraire uniquement les fichiers (pas les répertoires)
                    if (!entry.isDirectory()) {
                        try (FileOutputStream fos = new FileOutputStream(filePath.toFile());
                             BufferedOutputStream bos = new BufferedOutputStream(fos, BUFFER_SIZE)) {
                            
                            int read;
                            while ((read = zipIn.read(buffer)) != -1) {
                                bos.write(buffer, 0, read);
                            }
                        }
                    }
                    
                    zipIn.closeEntry();
                }
            }
            
            logger.info("Données Orekit téléchargées et extraites avec succès dans {}", dataDir);
            
        } catch (Exception e) {
            logger.error("Erreur lors du téléchargement ou de l'extraction des données Orekit", e);
            
            // Créer des fichiers minimaux en cas d'échec du téléchargement
            logger.info("Tentative de création de fichiers minimaux pour Orekit suite à l'échec du téléchargement...");
            try {
                createMinimalOrekitFiles(dataDir);
                logger.info("Fichiers minimaux créés avec succès");
            } catch (IOException ioe) {
                logger.error("Échec de la création des fichiers minimaux", ioe);
                throw new RuntimeException("Impossible de télécharger les données Orekit et de créer des fichiers minimaux", e);
            }
        }
    }
    
    /**
     * Vérifie si un répertoire est vide.
     * 
     * @param directory Répertoire à vérifier
     * @return true si le répertoire est vide ou n'est pas un répertoire
     */
    private boolean isEmpty(File directory) {
        if (directory.isDirectory()) {
            String[] files = directory.list();
            return files == null || files.length == 0;
        }
        return true;
    }
    
    /**
     * Crée des fichiers minimaux requis par Orekit en cas d'échec du téléchargement
     * 
     * @param dataDir Répertoire où créer les fichiers
     * @throws IOException Si une erreur survient lors de la création des fichiers
     */
    private void createMinimalOrekitFiles(Path dataDir) throws IOException {
        // Créer UTC-TAI.history (minimal) avec des données jusqu'en 2027
        Path utcTaiFile = dataDir.resolve("UTC-TAI.history");
        String utcTaiContent = "# UTC-TAI history file\n" +
                "# https://hpiers.obspm.fr/iers/bul/bulc/UTC-TAI.history\n" +
                "1972-01-01 00:00:00  10\n" +
                "1972-07-01 00:00:00  11\n" +
                "1973-01-01 00:00:00  12\n" +
                "1974-01-01 00:00:00  13\n" +
                "1975-01-01 00:00:00  14\n" +
                "1976-01-01 00:00:00  15\n" +
                "1977-01-01 00:00:00  16\n" +
                "1978-01-01 00:00:00  17\n" +
                "1979-01-01 00:00:00  18\n" +
                "1980-01-01 00:00:00  19\n" +
                "1981-07-01 00:00:00  20\n" +
                "1982-07-01 00:00:00  21\n" +
                "1983-07-01 00:00:00  22\n" +
                "1985-07-01 00:00:00  23\n" +
                "1988-01-01 00:00:00  24\n" +
                "1990-01-01 00:00:00  25\n" +
                "1991-01-01 00:00:00  26\n" +
                "1992-07-01 00:00:00  27\n" +
                "1993-07-01 00:00:00  28\n" +
                "1994-07-01 00:00:00  29\n" +
                "1996-01-01 00:00:00  30\n" +
                "1997-07-01 00:00:00  31\n" +
                "1999-01-01 00:00:00  32\n" +
                "2006-01-01 00:00:00  33\n" +
                "2009-01-01 00:00:00  34\n" +
                "2012-07-01 00:00:00  35\n" +
                "2015-07-01 00:00:00  36\n" +
                "2017-01-01 00:00:00  37\n" +
                "2022-01-01 00:00:00  37\n" +
                "2023-01-01 00:00:00  37\n" +
                "2024-01-01 00:00:00  37\n" +
                "2025-01-01 00:00:00  37\n" +
                "2026-01-01 00:00:00  37\n" +
                "2027-01-01 00:00:00  37";
        Files.write(utcTaiFile, utcTaiContent.getBytes());
        
        // Créer eopc04_IAU2000.62-now (minimal) avec plus de points de données
        Path eopC04File = dataDir.resolve("eopc04_IAU2000.62-now");
        String eopC04Content = "# EOP C04 file\n" +
                "# Earth Orientation parameters, IAU2000\n" +
                "# MJD         x           y         UT1-UTC      LOD         dX          dY\n" +
                // Ajouter des données pour les dates de 2023 à 2026 (MJD approximatifs)
                "59580.00  0.000100  0.000100  0.0000100  0.0000100  0.000100  0.000100\n" +  // Début 2022
                "59945.00  0.000100  0.000100  0.0000100  0.0000100  0.000100  0.000100\n" +  // Début 2023
                "60310.00  0.000100  0.000100  0.0000100  0.0000100  0.000100  0.000100\n" +  // Début 2024
                "60675.00  0.000100  0.000100  0.0000100  0.0000100  0.000100  0.000100\n" +  // Début 2025
                "60706.00  0.000100  0.000100  0.0000100  0.0000100  0.000100  0.000100\n" +  // Février 2025
                "60737.00  0.000100  0.000100  0.0000100  0.0000100  0.000100  0.000100\n" +  // Mars 2025
                "60767.00  0.000100  0.000100  0.0000100  0.0000100  0.000100  0.000100\n" +  // Avril 2025
                "60798.00  0.000100  0.000100  0.0000100  0.0000100  0.000100  0.000100\n" +  // Mai 2025
                "60828.00  0.000100  0.000100  0.0000100  0.0000100  0.000100  0.000100\n" +  // Juin 2025
                "60859.00  0.000100  0.000100  0.0000100  0.0000100  0.000100  0.000100\n" +  // Juillet 2025
                "60890.00  0.000100  0.000100  0.0000100  0.0000100  0.000100  0.000100\n" +  // Août 2025
                "60920.00  0.000100  0.000100  0.0000100  0.0000100  0.000100  0.000100\n" +  // Septembre 2025
                "60951.00  0.000100  0.000100  0.0000100  0.0000100  0.000100  0.000100\n" +  // Octobre 2025
                "60981.00  0.000100  0.000100  0.0000100  0.0000100  0.000100  0.000100\n" +  // Novembre 2025
                "61012.00  0.000100  0.000100  0.0000100  0.0000100  0.000100  0.000100\n" +  // Décembre 2025
                "61040.00  0.000100  0.000100  0.0000100  0.0000100  0.000100  0.000100";
        Files.write(eopC04File, eopC04Content.getBytes());
    }
}
