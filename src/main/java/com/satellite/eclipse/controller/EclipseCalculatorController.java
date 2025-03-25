package com.satellite.eclipse.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.satellite.eclipse.model.EclipsePeriod;
import com.satellite.eclipse.model.EclipseRequest;
import com.satellite.eclipse.service.EclipseCalculatorService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Contrôleur REST pour calculer les périodes d'éclipse d'un satellite.
 */
@RestController
@RequestMapping("/api/eclipse")
@RequiredArgsConstructor
@Slf4j
public class EclipseCalculatorController {

    private final EclipseCalculatorService eclipseCalculatorService;

    /**
     * Endpoint pour calculer les périodes d'éclipse à partir des données TLE d'un satellite.
     * 
     * @param request La requête contenant les données TLE et la période de calcul
     * @return Liste des périodes d'éclipse détectées
     */
    @PostMapping("/calculate")
    public ResponseEntity<List<EclipsePeriod>> calculateEclipsePeriods(@RequestBody EclipseRequest request) {
        log.info("Réception d'une requête de calcul d'éclipse pour le satellite: {}", 
                request.getTleData().getSatelliteName());
        
        List<EclipsePeriod> eclipsePeriods = eclipseCalculatorService.calculateEclipsePeriods(request);
        
        return ResponseEntity.ok(eclipsePeriods);
    }
}
