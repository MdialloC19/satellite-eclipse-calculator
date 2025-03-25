package com.satellite.eclipse.service;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;

import org.orekit.data.DataContext;
import org.orekit.data.DataProvidersManager;
import org.orekit.data.DirectoryCrawler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

/**
 * Composant pour charger les données Orekit nécessaires aux calculs orbitaux.
 */
@Component
public class OrekitDataLoader {
    private static final Logger logger = LoggerFactory.getLogger(OrekitDataLoader.class);
    private static final AtomicBoolean isDataLoaded = new AtomicBoolean(false);

    @Value("${orekit.data.path:src/main/resources/orekit-data}")
    private String orekitDataPath;

    /**
     * Initialise les données Orekit au démarrage de l'application.
     */
    @PostConstruct
    public void initialize() {
        if (!isDataLoaded.get()) {
            synchronized (OrekitDataLoader.class) {
                if (!isDataLoaded.get()) {
                    try {
                        logger.info("Chargement des données Orekit depuis {}", orekitDataPath);
                        File orekitDataFolder = new File(orekitDataPath);
                        if (!orekitDataFolder.exists()) {
                            logger.warn("Dossier de données Orekit introuvable: {}", orekitDataPath);
                            logger.info("Tentative de création du dossier");
                            orekitDataFolder.mkdirs();
                        }
                        
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
