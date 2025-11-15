package sn.suntelecoms.assurway.clearingapi.exception;

public class ResourceAlreadyExistsException extends RuntimeException {

    public ResourceAlreadyExistsException(String message) {
        super(message);
    }

    public ResourceAlreadyExistsException(String resource, String field, Object value) {
        super(String.format("%s existe déjà avec %s : '%s'", resource, field, value));
    }
}