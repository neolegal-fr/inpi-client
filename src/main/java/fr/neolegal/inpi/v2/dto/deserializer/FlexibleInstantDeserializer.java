package fr.neolegal.inpi.v2.dto.deserializer;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

/**
 * Désérialiseur Jackson personnalisé pour gérer les multiples formats de dates/timestamps
 * retournés par l'API INPI.
 * 
 * Gère les formats suivants :
 * - "yyyy-MM-dd" (ex: "2023-11-02") - converti en Instant à minuit UTC
 * - ISO 8601 avec timezone (ex: "2023-11-02T00:00:00+01:00")
 * - Format Instant standard (ex: "2023-11-02T00:00:00Z")
 */
public class FlexibleInstantDeserializer extends JsonDeserializer<Instant> {

    @Override
    public Instant deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String dateStr = p.getText();
        
        if (dateStr == null || dateStr.isEmpty()) {
            return null;
        }
        
        try {
            // Tentative 1 : Format simple yyyy-MM-dd
            // Convertir en Instant à minuit UTC
            if (dateStr.length() == 10 && dateStr.matches("\\d{4}-\\d{2}-\\d{2}")) {
                LocalDate localDate = LocalDate.parse(dateStr);
                return localDate.atStartOfDay(ZoneId.of("UTC")).toInstant();
            }
        } catch (Exception e) {
            // Passer à la tentative suivante
        }
        
        try {
            // Tentative 2 : Format ISO 8601 avec timezone (ex: 2023-11-02T00:00:00+01:00)
            ZonedDateTime zonedDateTime = ZonedDateTime.parse(dateStr);
            return zonedDateTime.toInstant();
        } catch (Exception e) {
            // Passer à la tentative suivante
        }
        
        try {
            // Tentative 3 : Format Instant standard
            return Instant.parse(dateStr);
        } catch (Exception e) {
            throw new IOException("Impossible de parser la date/timestamp: " + dateStr, e);
        }
    }
}
