package uy.edu.um.exceptions;

public class InstruccionesInvalidasException extends Exception {

    public InstruccionesInvalidasException() {
        super("El evento debe tener una o más instrucciones");
    }
}