package uy.edu.um.exceptions;

public class EstadoProcesoInvalidoException extends Exception {

    public EstadoProcesoInvalidoException() {
        super("Estados de proceso aceptados: NEW, PENDING, RUNNING y FINISHED");
    }
}