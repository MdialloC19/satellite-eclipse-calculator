package com.satellite.eclipse.service;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.hipparchus.ode.events.Action;
import org.hipparchus.util.FastMath;
import org.orekit.bodies.CelestialBody;
import org.orekit.bodies.CelestialBodyFactory;
import org.orekit.bodies.GeodeticPoint;
import org.orekit.bodies.OneAxisEllipsoid;
import org.orekit.frames.Frame;
import org.orekit.frames.FramesFactory;
import org.orekit.orbits.CartesianOrbit;
import org.orekit.propagation.Propagator;
import org.orekit.propagation.SpacecraftState;
import org.orekit.propagation.analytical.KeplerianPropagator;
import org.orekit.propagation.analytical.tle.TLE;
import org.orekit.propagation.analytical.tle.TLEPropagator;
import org.orekit.propagation.events.EclipseDetector;
import org.orekit.propagation.events.EventDetector;
import org.orekit.propagation.events.handlers.EventHandler;
import org.orekit.propagation.sampling.OrekitFixedStepHandler;
import org.orekit.propagation.sampling.OrekitStepHandler;
import org.orekit.propagation.sampling.OrekitStepInterpolator;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.TimeScalesFactory;
import org.orekit.utils.Constants;
import org.orekit.utils.IERSConventions;
import org.orekit.utils.PVCoordinates;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.satellite.eclipse.model.EclipsePeriod;
import com.satellite.eclipse.model.EclipseRequest;
import com.satellite.eclipse.model.TleData;

/**
 * Service pour calculer les périodes d'éclipse d'un satellite en utilisant Orekit.
 */
@Service
public class EclipseCalculatorService {
    private static final Logger logger = LoggerFactory.getLogger(EclipseCalculatorService.class);

    private final OrekitDataLoader dataLoader;

    public EclipseCalculatorService(OrekitDataLoader dataLoader) {
        this.dataLoader = dataLoader;
    }

    /**
     * Calcule les périodes d'éclipse pour un satellite en utilisant ses données TLE.
     * 
     * @param request La requête contenant les données TLE et la période de calcul
     * @return Liste des périodes d'éclipse détectées
     */
    public List<EclipsePeriod> calculateEclipsePeriods(EclipseRequest request) {
        logger.info("Calcul des périodes d'éclipse pour le satellite: {}", 
                request.getTleData().getSatelliteName());
        
        try {
            // Création de l'objet TLE à partir des données
            TLE tle = new TLE(request.getTleData().getLine1(), request.getTleData().getLine2());
            
            // Convertir les instants Java en dates Orekit
            AbsoluteDate startDate = new AbsoluteDate(Date.from(request.getStartDate()), 
                    TimeScalesFactory.getUTC());
            AbsoluteDate endDate = new AbsoluteDate(Date.from(request.getEndDate()), 
                    TimeScalesFactory.getUTC());
            
            // Créer le propagateur TLE
            Propagator propagator = TLEPropagator.selectExtrapolator(tle);
            
            // Obtenir les corps célestes pour la détection d'éclipse
            Frame inertialFrame = FramesFactory.getEME2000();
            CelestialBody sun = CelestialBodyFactory.getSun();
            OneAxisEllipsoid earth = new OneAxisEllipsoid(
                    Constants.WGS84_EARTH_EQUATORIAL_RADIUS,
                    Constants.WGS84_EARTH_FLATTENING,
                    FramesFactory.getITRF(IERSConventions.IERS_2010, true));
            
            // Créer le détecteur d'éclipse pour le Soleil
            final List<EclipsePeriod> eclipsePeriods = new ArrayList<>();
            final boolean[] inEclipse = {false};
            final Instant[] entryTime = {null};
            
            // Handler d'événement pour l'éclipse
            EventHandler<EclipseDetector> eclipseHandler = new EventHandler<EclipseDetector>() {
                @Override
                public Action eventOccurred(SpacecraftState s, EclipseDetector detector, boolean increasing) {
                    AbsoluteDate currentDate = s.getDate();
                    Instant currentInstant = Instant.ofEpochMilli(
                            currentDate.toDate(TimeScalesFactory.getUTC()).getTime());
                    
                    if (!increasing) { // Entrée dans l'éclipse
                        inEclipse[0] = true;
                        entryTime[0] = currentInstant;
                        logger.debug("Entrée en éclipse à {}", currentInstant);
                    } else { // Sortie de l'éclipse
                        inEclipse[0] = false;
                        
                        if (entryTime[0] != null) {
                            // Calculer la durée de l'éclipse
                            double durationMinutes = Duration.between(entryTime[0], currentInstant)
                                    .getSeconds() / 60.0;
                            
                            // Ajouter la période d'éclipse à la liste
                            eclipsePeriods.add(new EclipsePeriod(
                                    entryTime[0],
                                    currentInstant,
                                    durationMinutes,
                                    "umbra"
                            ));
                            
                            logger.debug("Sortie d'éclipse à {}, durée: {} minutes", 
                                    currentInstant, durationMinutes);
                        }
                        
                        entryTime[0] = null;
                    }
                    // Continuer la propagation
                    return Action.CONTINUE;
                }

                @Override
                public SpacecraftState resetState(EclipseDetector detector, SpacecraftState oldState) {
                    return oldState;
                }
            };
            
            // Créer et ajouter le détecteur d'éclipse
            EclipseDetector eclipseDetector = new EclipseDetector(sun, Constants.SUN_RADIUS, earth)
                    .withHandler(eclipseHandler);
            
            propagator.addEventDetector(eclipseDetector);
            
            // Configurer le propagateur pour utiliser des pas fixes
            double stepInSeconds = request.getStepInSeconds();
            propagator.getMultiplexer().add(stepInSeconds, currentState -> {
                // Vous pouvez ajouter du traitement supplémentaire à chaque pas si nécessaire
            });
            
            // Propager l'orbite pour calculer les éclipses
            propagator.propagate(startDate, endDate);
            
            logger.info("Calcul terminé. Nombre de périodes d'éclipse trouvées: {}", eclipsePeriods.size());
            return eclipsePeriods;
            
        } catch (Exception e) {
            logger.error("Erreur lors du calcul des périodes d'éclipse", e);
            throw new RuntimeException("Erreur lors du calcul des périodes d'éclipse", e);
        }
    }
}
