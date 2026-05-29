package uy.edu.um.exceptions;

public class EventosInvalidosException extends Exception {

    public EventosInvalidosException() {
        super("El proceso debe tener uno o más eventos");
    }
}