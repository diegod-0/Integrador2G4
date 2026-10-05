package org.rescuelink.exception;

public class ResourceNotFoundException extends BusinessException {
    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String recurso, Object id) {
        super(String.format("El recurso %s con identificador '%s' no fue encontrado o ha sido dado de baja", recurso, id));
    }
}
