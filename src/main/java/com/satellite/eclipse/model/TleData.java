package com.satellite.eclipse.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Classe représentant les données TLE (Two-Line Element set) d'un satellite.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TleData {
    private String satelliteName;
    private String line1;
    private String line2;
}
