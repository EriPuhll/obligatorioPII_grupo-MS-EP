package uy.edu.um.dominio;

import uy.edu.um.exceptions.InstruccionesInvalidasException;
import uy.edu.um.exceptions.TipoEventoInvalidoException;
import uy.edu.um.tad.list.MyList;

public class Evento {

    private String tipo;
    private MyList<String> instrucciones;

    public Evento(String tipo, MyList<String> instrucciones) throws TipoEventoInvalidoException, InstruccionesInvalidasException {
        setTipo(tipo);
        setInstrucciones(instrucciones);
    }

    public String getTipo() {
        return tipo;
    }

    public MyList<String> getInstrucciones() {
        return instrucciones;
    }

    public void setTipo(String tipo) throws TipoEventoInvalidoException {
        if (tipo == null) {
            throw new TipoEventoInvalidoException();
        }

        tipo = tipo.trim().toUpperCase();

        if (!tipo.equals("CPU") && !tipo.equals("RAM") && !tipo.equals("DISK")) {
            throw new TipoEventoInvalidoException();
        }

        this.tipo = tipo;
    }

    public void setInstrucciones(MyList<String> instrucciones) throws InstruccionesInvalidasException {
        if (instrucciones == null || instrucciones.size() == 0) {
            throw new InstruccionesInvalidasException();
        }

        this.instrucciones = instrucciones;
    }

    @Override
    public String toString() {
        String resultado = "EVENT: " + tipo + " | Instructions [";

        for (int i = 0; i < instrucciones.size(); i++) {
            resultado += instrucciones.get(i);

            if (i < instrucciones.size() - 1) {
                resultado += ", ";
            }
        }

        resultado += "]";

        return resultado;
    }
}