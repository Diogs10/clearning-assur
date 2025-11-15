package sn.suntelecoms.assurway.clearingapi.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resource, String field, Object value) {
        super(String.format("%s non trouvé(e) avec %s : '%s'", resource, field, value));
    }
}
