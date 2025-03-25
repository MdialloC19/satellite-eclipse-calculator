package com.satellite.eclipse.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

/**
 * Utilitaire pour préparer les données Orekit nécessaires
 * au bon fonctionnement des calculs d'éclipse.
 */
@Configuration
public class OrekitDataDownloader {
    private static final Logger logger = LoggerFactory.getLogger(OrekitDataDownloader.class);
    
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    @Profile("!test") // Ne pas exécuter pendant les tests
    public CommandLineRunner prepareOrekitData() {
        return args -> {
            Path dataDir = Paths.get("orekit-data");
            
            // Vérifier si le répertoire existe et s'il est vide
            if (!Files.exists(dataDir) || isEmpty(dataDir.toFile())) {
                logger.info("Le répertoire de données Orekit est vide ou n'existe pas. Création des données minimales...");
                
                try {
                    // Créer les répertoires nécessaires
                    Files.createDirectories(dataDir);
                    
                    // Créer un fichier de configuration UTC-TAI.history
                    Path utcTaiFile = dataDir.resolve("UTC-TAI.history");
                    String utcTaiContent = 
                          "# UTC-TAI history file\n"
                        + "# File created for Orekit by SatelliteEclipseCalculator\n"
                        + "# This simplified file is valid from 1972-01-01 to 2025-12-31\n"
                        + "MJD UTC-TAI\n"
                        + "41317.00 -10.0\n"
                        + "41499.00 -11.0\n"
                        + "41683.00 -12.0\n"
                        + "42048.00 -13.0\n"
                        + "42413.00 -14.0\n"
                        + "42778.00 -15.0\n"
                        + "43144.00 -16.0\n"
                        + "43509.00 -17.0\n"
                        + "43874.00 -18.0\n"
                        + "44239.00 -19.0\n"
                        + "44786.00 -20.0\n"
                        + "45151.00 -21.0\n"
                        + "45516.00 -22.0\n"
                        + "46247.00 -23.0\n"
                        + "47161.00 -24.0\n"
                        + "47892.00 -25.0\n"
                        + "48257.00 -26.0\n"
                        + "48804.00 -27.0\n"
                        + "49169.00 -28.0\n"
                        + "49534.00 -29.0\n"
                        + "50083.00 -30.0\n"
                        + "50630.00 -31.0\n"
                        + "51179.00 -32.0\n"
                        + "53736.00 -33.0\n"
                        + "54832.00 -34.0\n"
                        + "56109.00 -35.0\n"
                        + "57204.00 -36.0\n"
                        + "57754.00 -37.0\n";
                    
                    Files.write(utcTaiFile, utcTaiContent.getBytes(StandardCharsets.UTF_8));
                    
                    // Créer d'autres fichiers de base si nécessaire (Earth Orientation Parameters, etc.)
                    Path eopFile = dataDir.resolve("eopc04_IAU2000.62-now");
                    String eopFileHeader = 
                          "# Earth Orientation Parameters\n"
                        + "# File created for Orekit by SatelliteEclipseCalculator\n"
                        + "# This is a minimal placeholder file\n"
                        + "Date      MJD      x         y         UT1-UTC      LOD       dPsi    dEpsilon     dX        dY     xError  yError ut1Error lodError dPsiError  dEpsError  dXError   dYError\n";
                    Files.write(eopFile, eopFileHeader.getBytes(StandardCharsets.UTF_8));
                    
                    logger.info("Données Orekit minimales créées avec succès dans {}", dataDir);
                    
                    // Définir la propriété système pour le dossier des données Orekit
                    System.setProperty("orekit.data.path", dataDir.toAbsolutePath().toString());
                    logger.info("Propriété orekit.data.path définie sur: {}", dataDir.toAbsolutePath());
                    
                } catch (Exception e) {
                    logger.error("Erreur lors de la création des données Orekit", e);
                    throw new RuntimeException("Impossible de préparer les données Orekit", e);
                }
            } else {
                logger.info("Les données Orekit existent déjà dans {}", dataDir);
                // Définir la propriété système pour le dossier des données Orekit
                System.setProperty("orekit.data.path", dataDir.toAbsolutePath().toString());
                logger.info("Propriété orekit.data.path définie sur: {}", dataDir.toAbsolutePath());
            }
        };
    }
    
    private boolean isEmpty(File directory) {
        if (directory.isDirectory()) {
            String[] files = directory.list();
            return files == null || files.length == 0;
        }
        return true;
    }
}
