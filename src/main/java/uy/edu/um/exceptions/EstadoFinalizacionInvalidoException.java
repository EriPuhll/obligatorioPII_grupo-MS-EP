package uy.edu.um.exceptions;

public class EstadoFinalizacionInvalidoException extends Exception {

    public EstadoFinalizacionInvalidoException() {
        super("Estados de finalización aceptados: OK, ERROR y TERMINATED");
    }
}