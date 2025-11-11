package fr.neolegal.inpi.v2.dto;

import java.time.Instant;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import fr.neolegal.inpi.v2.dto.deserializer.FlexibleInstantDeserializer;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Company {

    String id;
    
    @JsonDeserialize(using = FlexibleInstantDeserializer.class)
    Instant updatedAt;
    
    Integer nombreRepresentantsActifs;
    Integer nombreEtablissementsOuverts;
    Integer nombreBeneficiairesEffectifsActifs;
    Formality formality;
}
