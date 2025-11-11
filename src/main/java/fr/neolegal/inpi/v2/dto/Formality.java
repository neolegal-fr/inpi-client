package fr.neolegal.inpi.v2.dto;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import fr.neolegal.inpi.v2.dto.deserializer.FlexibleInstantDeserializer;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Formality {

    String siren;
    Content content;
    String diffusionINSEE;
    String typePersonne;
    boolean diffusionCommerciale;
    List<Evenement> historique;
    String formeJuridique;
    
    @JsonDeserialize(using = FlexibleInstantDeserializer.class)
    Instant created;
    
    @JsonDeserialize(using = FlexibleInstantDeserializer.class)
    Instant updated;
}
