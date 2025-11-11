package fr.neolegal.inpi.v2.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.stream.Stream;
import lombok.Getter;

@Getter
public enum TypeActe {
    PV_AGOA("Procès-verbal d\'assemblée générale ordinaire", false),
    STATUTS_MIS_A_JOUR("Statuts mis à jour", true),
    DECISION_PRESIDENT("Décision(s) du président", false),
    STATUTS_CONSTITUTIFS("Statuts constitutifs", true),
    CERTIFICAT("Certificat", false),
    LISTE_DES_SOUSCRIPTEURS("Liste des souscripteurs", false),
    ATTESTATION_DEPOT_FONDS_ET_LISTE_DES_SOUSCRIPTEURS("Attestation de dépôt des fonds et liste des souscripteurs", false),
    ACTE_SOUS_SEING_PRIVE("Acte sous seing privé", false),
    DECISION_ASSOCIE_UNIQUE("Décision(s) de l'associé unique", false);

    final String label;

    boolean publicationStatuts;

    TypeActe(String label, boolean publicationStatuts) {
        this.label = label;
        this.publicationStatuts = publicationStatuts;
    }

    public boolean isPublicationStatuts() {
        return publicationStatuts;
    }

    @JsonCreator
    public static TypeActe forValue(String label) {
        return Stream.of(TypeActe.values()).filter(typeActe -> typeActe.getLabel().equalsIgnoreCase(label)).findFirst().orElse(null);
    }
}
