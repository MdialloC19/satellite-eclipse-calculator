package com.satellite.eclipse.examples;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.satellite.eclipse.model.EclipsePeriod;
import com.satellite.eclipse.model.EclipseRequest;
import com.satellite.eclipse.model.TleData;
import com.satellite.eclipse.service.EclipseCalculatorService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Exemple d'utilisation du calculateur de périodes d'éclipse.
 * Cette classe montre comment utiliser le service pour calculer les éclipses
 * d'un satellite à partir de ses données TLE.
 * 
 * Cet exemple s'exécutera automatiquement au démarrage de l'application
 * si le profil "example" est activé.
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
@Profile("example")
public class EclipseCalculationExample {

    private final EclipseCalculatorService eclipseCalculatorService;

    @Bean
    public CommandLineRunner runEclipseExample() {
        return args -> {
            log.info("Démarrage de l'exemple de calcul d'éclipse...");

            // Exemple de données TLE pour l'ISS (Station Spatiale Internationale)
            TleData issTleData = new TleData(
                    "ISS (ZARYA)",
                    "1 25544U 98067A   22085.41476591  .00007277  00000-0  13908-3 0  9990",
                    "2 25544  51.6449 336.4797 0005408  61.7847  47.9568 15.49454906334473");

            // Définir la période pour laquelle calculer les éclipses (24 heures)
            Instant now = Instant.now();
            Instant tomorrow = now.plus(24, ChronoUnit.HOURS);

            // Créer la requête de calcul d'éclipse
            EclipseRequest request = new EclipseRequest(
                    issTleData,
                    now,
                    tomorrow,
                    60 // Pas de calcul: 60 secondes
            );

            log.info("Calcul des périodes d'éclipse pour l'ISS sur 24 heures à partir de maintenant");

            // Appeler le service pour calculer les périodes d'éclipse
            List<EclipsePeriod> eclipsePeriods = eclipseCalculatorService.calculateEclipsePeriods(request);

            // Afficher les résultats
            log.info("Nombre de périodes d'éclipse trouvées: {}", eclipsePeriods.size());
            eclipsePeriods.forEach(period -> {
                log.info("Éclipse: entrée à {}, sortie à {}, durée: {} minutes",
                        period.getEntryTime(),
                        period.getExitTime(),
                        period.getDurationMinutes());
            });

            log.info("Fin de l'exemple de calcul d'éclipse");
        };
    }

    /**
     * Cette méthode montre comment utiliser le calculateur d'éclipse dans
     * le contexte du simulateur de batterie de satellite.
     * 
     * @param tleData   Les données TLE du satellite
     * @param startTime Heure de début de la simulation
     * @param endTime   Heure de fin de la simulation
     * @return Liste des périodes d'éclipse durant la simulation
     */
    public List<EclipsePeriod> calculateEclipseForBatterySimulation(
            TleData tleData, Instant startTime, Instant endTime) {

        log.info("Calcul des périodes d'éclipse pour le simulateur de batterie");

        // Créer la requête de calcul avec un pas de 10 secondes pour plus de précision
        EclipseRequest request = new EclipseRequest(
                tleData,
                startTime,
                endTime,
                10 // Pas de calcul de 10 secondes pour une simulation précise
        );

        // Calculer les périodes d'éclipse
        List<EclipsePeriod> eclipsePeriods = eclipseCalculatorService.calculateEclipsePeriods(request);

        log.info("Périodes d'éclipse calculées pour le simulateur: {} périodes trouvées",
                eclipsePeriods.size());

        return eclipsePeriods;
    }
}
