package uy.edu.um.dominio;

import uy.edu.um.exceptions.InstruccionesInvalidasException;
import uy.edu.um.exceptions.TipoEventoInvalidoException;
import uy.edu.um.tad.list.MyList;

public class Evento {

    private TipoEvento tipo;
    private MyList<String> instrucciones;

    //Constructor
    public Evento(String tipo, MyList<String> instrucciones) throws TipoEventoInvalidoException, InstruccionesInvalidasException {
        setTipo(tipo);
        setInstrucciones(instrucciones);
    }

    public TipoEvento getTipo() {
        return tipo;
    }

    public MyList<String> getInstrucciones() {
        return instrucciones;
    }

    // Válida y convierte el texto recibido al enum TipoEvento.
    private void setTipo(String tipo) throws TipoEventoInvalidoException {
        if (tipo == null || tipo.trim().isEmpty()) {
            throw new TipoEventoInvalidoException();
        }

        try {
            this.tipo = TipoEvento.valueOf(tipo.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new TipoEventoInvalidoException();
        }
    }

    //Válida que exista al menos una instrucción y que ninguna sea vacía. Funciona como setter del atributo instrucciones.
    private void setInstrucciones(MyList<String> instrucciones) throws InstruccionesInvalidasException {
        if (instrucciones == null || instrucciones.size() == 0) {
            throw new InstruccionesInvalidasException();
        }

        for (int i = 0; i < instrucciones.size(); i++) {
            String instruccion = instrucciones.get(i);

            if (instruccion == null || instruccion.trim().isEmpty()) {
                throw new InstruccionesInvalidasException();
            }
        }
        this.instrucciones = instrucciones;
    }

    // Devuelve el evento con el formato pedido para el log
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