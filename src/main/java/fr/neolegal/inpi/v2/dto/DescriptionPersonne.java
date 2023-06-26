package fr.neolegal.inpi.v2.dto;

import static org.apache.commons.lang3.StringUtils.*;

import java.time.LocalDate;
import java.util.Collection;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DescriptionPersonne {

    /** Date d'effet rôle déclarant */
    String dateEffetRoleDeclarant;

    /** Rôle déclarant. Champ libre */
    String role;

    /** Nom */
    String nom;

    /** Prénom */
    Collection<String> prenoms;

    /** Genre */
    Genre genre;

    /** Titre */
    String titre;

    /** Nom d'usage */
    String nomUsage;

    /** Pseudonyme */
    String pseudonyme;

    /** Date de naissance */
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

    public LocalDate dateDeNaissanceApproximative() {
        if (isBlank(dateDeNaissance)) return null;

        // Seuls l'année et le mois de naissance sont donnés
        String completedValue = dateDeNaissance.length() == 7 ? dateDeNaissance + "-01" : dateDeNaissance;

        return LocalDate.parse(completedValue);
    }
}
