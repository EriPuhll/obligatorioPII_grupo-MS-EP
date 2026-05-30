package uy.edu.um.dominio;

import uy.edu.um.exceptions.DatoInvalidoException;
import uy.edu.um.exceptions.TipoUsuarioInvalidoException;

public class Usuario {

    // uid y alias son final
    private final int uid;
    private final String alias;
    private TipoUsuario tipo;

    //Constructor
    public Usuario(int uid, String alias, String tipo)
            throws TipoUsuarioInvalidoException, DatoInvalidoException {

        if (uid < 0) {
            throw new DatoInvalidoException("El UID no puede ser negativo");
        }

        if (alias == null || alias.trim().isEmpty()) {
            throw new DatoInvalidoException("El alias del usuario no puede estar vacío");
        }

        this.uid = uid;
        this.alias = alias.trim();
        setTipo(tipo);
    }

    public int getUid() {
        return uid;
    }

    public String getAlias() {
        return alias;
    }

    public TipoUsuario getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) throws TipoUsuarioInvalidoException {
        if (tipo == null) {
            throw new TipoUsuarioInvalidoException();
        }

        tipo = tipo.trim().toUpperCase();

        if (!tipo.equals("ADMIN") && !tipo.equals("GENERIC")) {
            throw new TipoUsuarioInvalidoException();
        }

        this.tipo = TipoUsuario.valueOf(tipo);
    }

    // Devuelve el peso del usuario según su tipo
    public int getPeso() {
        if (tipo == TipoUsuario.ADMIN) {
            return 32;
        }
        return 16;
    }

    @Override
    public String toString() {
        return "USER:" + alias + " UID:" + uid;
    }
}