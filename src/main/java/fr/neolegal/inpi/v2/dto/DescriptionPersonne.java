package fr.neolegal.inpi.v2.dto;

import static org.apache.commons.lang3.StringUtils.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Collection;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import fr.neolegal.inpi.v2.dto.deserializer.FlexibleInstantDeserializer;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DescriptionPersonne {

    /** Date d'effet rôle déclarant */
    Boolean dateEffetRoleDeclarantPresent;
    @JsonDeserialize(using = FlexibleInstantDeserializer.class)
    Instant dateEffetRoleDeclarant;

    /** Rôle déclarant. Champ libre */
    String role;

    /** Nom */
    String nom;

    /** Prénom */
    Collection<String> prenoms;

    /** Genre */
    Boolean genrePresent;
    Genre genre;

    /** Titre */
    Boolean titrePresent;    
    String titre;

    /** Nom d'usage */
    String nomUsage;

    /** Pseudonyme */
    String pseudonyme;

    /** Date de naissance */
    Boolean dateDeNaissancePresent;
    String dateDeNaissance;

    /** Code nationalité */
    String codeNationalite;

    /** Nationalité */
    String nationalite;

    /** Situation matrimoniale */
    SituationMatrimoniale situationMatrimoniale;

    /** Indicateur de non sédentarité */
    Boolean indicateurDeNonSedentarite;

    /** Qualité de non- sédentarité */
    QualiteNonSedentarite qualiteDeNonSedentarite;

    Boolean sirenPresent;        
    Boolean paysNaissancePresent;
    Boolean codePaysNaissancePresent;
    Boolean lieuDeNaissancePresent;
    Boolean codePostalNaissancePresent;
    Boolean codeInseeGeographiquePresent;
    Boolean situationMatrimonialePresent;
    Boolean qualiteDeNonSedentaritePresent;
    Boolean indicateurDeNonSedentaritePresent;


    public LocalDate dateDeNaissanceApproximative() {
        if (isBlank(dateDeNaissance)) return null;

        // Seuls l'année et le mois de naissance sont donnés
        String completedValue = dateDeNaissance.length() == 7 ? dateDeNaissance + "-01" : dateDeNaissance;

        return LocalDate.parse(completedValue);
    }
}
