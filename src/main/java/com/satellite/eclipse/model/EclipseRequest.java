package com.satellite.eclipse.model;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Classe représentant une requête de calcul d'éclipse.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EclipseRequest {
    @JsonProperty("tle")
    private TleData tleData;
    private Instant startDate;
    private Instant endDate;
    private int stepInSeconds = 60; // Pas de calcul par défaut (60 secondes)
}
