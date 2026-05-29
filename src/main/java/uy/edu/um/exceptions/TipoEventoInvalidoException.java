package uy.edu.um.exceptions;

public class TipoEventoInvalidoException extends Exception {

    public TipoEventoInvalidoException() {
        super("Tipos de evento aceptados: CPU, RAM y DISK");
    }
}