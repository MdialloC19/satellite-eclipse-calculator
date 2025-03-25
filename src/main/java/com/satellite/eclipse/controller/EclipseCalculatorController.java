package com.satellite.eclipse.controller;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
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
    public ResponseEntity<?> calculateEclipsePeriods(@RequestBody EclipseRequest request) {
        log.info("Réception d'une requête de calcul d'éclipse pour le satellite: {}", 
                request.getTleData().getSatelliteName());
        
        try {
            List<EclipsePeriod> eclipsePeriods = eclipseCalculatorService.calculateEclipsePeriods(request);
            return ResponseEntity.ok(eclipsePeriods);
        } catch (Exception e) {
            log.error("Erreur lors du calcul des périodes d'éclipse: {}", e.getMessage(), e);
            
            // Créer une réponse avec des informations détaillées sur l'erreur pour le débogage
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Erreur lors du calcul des périodes d'éclipse");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("stackTrace", Arrays.stream(e.getStackTrace())
                    .map(StackTraceElement::toString)
                    .limit(20)
                    .collect(Collectors.toList()));
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
