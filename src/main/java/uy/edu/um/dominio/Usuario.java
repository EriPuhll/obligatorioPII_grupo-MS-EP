package uy.edu.um.dominio;

import uy.edu.um.exceptions.TipoUsuarioInvalidoException;

public class Usuario {

    // uid y alias son final
    private final int uid;
    private final String alias;
    private String tipo;

    public Usuario(int uid, String alias, String tipo) throws TipoUsuarioInvalidoException {
        this.uid = uid;
        this.alias = alias;
        setTipo(tipo);
    }

    public int getUid() {
        return uid;
    }

    public String getAlias() {
        return alias;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) throws TipoUsuarioInvalidoException {
        if (tipo == null) {
            throw new TipoUsuarioInvalidoException();
        }
// normalización de tipo de usuario
        tipo = tipo.trim().toUpperCase();

        if (!tipo.equals("ADMIN") && !tipo.equals("GENERIC")) {
            throw new TipoUsuarioInvalidoException();
        }

        this.tipo = tipo;
    }

    // pesos según la letra
    public int getPeso() {
        if (tipo.equals("ADMIN")) {
            return 32;
        }

        return 16;
    }

    @Override
    public String toString() {
        return "USER:" + alias + " UID:" + uid;
    }
}