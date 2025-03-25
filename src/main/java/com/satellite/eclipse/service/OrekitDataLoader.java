package com.satellite.eclipse.service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicBoolean;

import org.orekit.data.DataContext;
import org.orekit.data.DataProvidersManager;
import org.orekit.data.DirectoryCrawler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

/**
 * Composant pour charger les données Orekit nécessaires aux calculs orbitaux.
 */
@Component
public class OrekitDataLoader {
    private static final Logger logger = LoggerFactory.getLogger(OrekitDataLoader.class);
    private static final AtomicBoolean isDataLoaded = new AtomicBoolean(false);
    private static final String OREKIT_DATA_DIR = "orekit-data";

    /**
     * Initialise les données Orekit au démarrage de l'application.
     */
    @PostConstruct
    public void initialize() {
        if (!isDataLoaded.get()) {
            synchronized (OrekitDataLoader.class) {
                if (!isDataLoaded.get()) {
                    try {
                        // Récupérer le chemin du data directory depuis la propriété système
                        // ou utiliser le chemin par défaut si la propriété n'est pas définie
                        String orekitDataPath = System.getProperty("orekit.data.path");
                        if (orekitDataPath == null || orekitDataPath.isEmpty()) {
                            // Utiliser le même chemin que OrekitDataDownloader
                            orekitDataPath = OREKIT_DATA_DIR;
                            Path absolutePath = Paths.get(orekitDataPath).toAbsolutePath();
                            System.setProperty("orekit.data.path", absolutePath.toString());
                            orekitDataPath = absolutePath.toString();
                        }
                        
                        logger.info("Chargement des données Orekit depuis {}", orekitDataPath);
                        File orekitDataFolder = new File(orekitDataPath);
                        
                        if (!orekitDataFolder.exists()) {
                            logger.warn("Dossier de données Orekit introuvable: {}", orekitDataPath);
                            logger.info("Tentative de création du dossier");
                            orekitDataFolder.mkdirs();
                        }
                        
                        // Vérifier que les fichiers essentiels existent
                        Path utcTaiFile = Paths.get(orekitDataPath, "UTC-TAI.history");
                        Path eopC04File = Paths.get(orekitDataPath, "eopc04_IAU2000.62-now");
                        
                        if (!Files.exists(utcTaiFile) || !Files.exists(eopC04File)) {
                            logger.warn("Fichiers essentiels manquants dans {}: UTC-TAI.history={}, eopc04_IAU2000.62-now={}", 
                                    orekitDataPath, Files.exists(utcTaiFile), Files.exists(eopC04File));
                        } else {
                            logger.info("Fichiers essentiels trouvés: UTC-TAI.history et eopc04_IAU2000.62-now");
                        }
                        
                        // Configurer le DataProvidersManager avec le répertoire de données
                        DataProvidersManager manager = DataContext.getDefault().getDataProvidersManager();
                        manager.addProvider(new DirectoryCrawler(orekitDataFolder));
                        
                        isDataLoaded.set(true);
                        logger.info("Données Orekit chargées avec succès");
                    } catch (Exception e) {
                        logger.error("Erreur lors du chargement des données Orekit", e);
                        throw new RuntimeException("Impossible de charger les données Orekit", e);
                    }
                }
            }
        }
    }
}
