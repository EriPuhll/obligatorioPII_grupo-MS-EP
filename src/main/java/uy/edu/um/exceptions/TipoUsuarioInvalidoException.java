package uy.edu.um.exceptions;

public class TipoUsuarioInvalidoException extends Exception {

    public TipoUsuarioInvalidoException() {
        super("Tipos de usuario aceptados: ADMIN y GENERIC");
    }
}