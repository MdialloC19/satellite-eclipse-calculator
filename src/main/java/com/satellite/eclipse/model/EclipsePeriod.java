package com.satellite.eclipse.model;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Classe représentant une période d'éclipse d'un satellite.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EclipsePeriod {
    private Instant entryTime;    // Moment d'entrée dans l'éclipse
    private Instant exitTime;     // Moment de sortie de l'éclipse
    private double durationMinutes;  // Durée de l'éclipse en minutes
    private String eclipseType;   // Type d'éclipse (umbra, penumbra)
}
