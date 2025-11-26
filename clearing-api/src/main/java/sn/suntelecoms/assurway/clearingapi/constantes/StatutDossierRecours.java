package sn.suntelecoms.assurway.clearingapi.constantes;

public enum StatutDossierRecours {
    ENVOYE("ENVOYE", "ENVOYE"),
    ACCEPTE("ACCEPTE", "ACCEPTE"),
    EN_ATTENTE("EN_ATTENTE", "EN_ATTENTE"),
    RETOURNE("RETOURNE", "RETOURNE"),
    REJETE("REJETE", "REJETE"),
    OUVERT("OUVERT", "OUVERT");
    String code;
    String value;

    private StatutDossierRecours (String code, String value) {
        this.code = code;
        this.value = value;
    }

    public String getCode() {
        return code;
    }

    public String getValue() {
        return value;
    }

    public static boolean from (String value) {
        for (StatutDossierRecours statut : StatutDossierRecours.values()) {
            if (statut.getValue().equalsIgnoreCase(value)) {
                return true;
            }
        }
        return false;
    }
}
