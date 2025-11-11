package fr.neolegal.inpi.v2.dto;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import fr.neolegal.inpi.v2.dto.deserializer.FlexibleInstantDeserializer;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Evenement {
    @JsonDeserialize(using = FlexibleInstantDeserializer.class)
    Instant dateIntegration;
    
    String codeEvenement;
    String libelleEvenement;
    String numeroLiasse;
    String patchId;
    
    @JsonDeserialize(using = FlexibleInstantDeserializer.class)
    Instant dateEffet;
    
    String cheminDateEffet;
}
