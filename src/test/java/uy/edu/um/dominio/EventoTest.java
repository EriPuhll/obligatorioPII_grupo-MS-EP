package uy.edu.um.dominio;

import org.junit.jupiter.api.Test;
import uy.edu.um.exceptions.InstruccionesInvalidasException;
import uy.edu.um.exceptions.TipoEventoInvalidoException;
import uy.edu.um.tad.list.MyLinkedListImpl;
import uy.edu.um.tad.list.MyList;

import static org.junit.jupiter.api.Assertions.*;

public class EventoTest {

    @Test
    public void crearEventoCorrectamente() throws Exception {
        MyList<String> instrucciones = new MyLinkedListImpl<>();
        instrucciones.add("mov");
        instrucciones.add("add");

        Evento evento = new Evento(TipoEvento.CPU, instrucciones);

        assertEquals(TipoEvento.CPU, evento.getTipoEvento());
        assertEquals(2, evento.getInstrucciones().size());
        assertEquals("mov", evento.getInstrucciones().get(0));
        assertEquals("add", evento.getInstrucciones().get(1));
    }

    @Test
    public void crearEventoDesdeStringCorrectamente() throws Exception {
        MyList<String> instrucciones = new MyLinkedListImpl<>();
        instrucciones.add("load");

        Evento evento = new Evento("RAM", instrucciones);

        assertEquals(TipoEvento.RAM, evento.getTipoEvento());
    }

    @Test
    public void noPermiteTipoEventoNulo() {
        MyList<String> instrucciones = new MyLinkedListImpl<>();
        instrucciones.add("mov");

        assertThrows(TipoEventoInvalidoException.class, () -> {
            new Evento((TipoEvento) null, instrucciones);
        });
    }

    @Test
    public void noPermiteTipoEventoInvalido() {
        MyList<String> instrucciones = new MyLinkedListImpl<>();
        instrucciones.add("mov");

        assertThrows(TipoEventoInvalidoException.class, () -> {
            new Evento("GPU", instrucciones);
        });
    }

    @Test
    public void noPermiteListaDeInstruccionesVacia() {
        MyList<String> instrucciones = new MyLinkedListImpl<>();

        assertThrows(InstruccionesInvalidasException.class, () -> {
            new Evento(TipoEvento.CPU, instrucciones);
        });
    }

    @Test
    public void noPermiteInstruccionVacia() {
        MyList<String> instrucciones = new MyLinkedListImpl<>();
        instrucciones.add("");

        assertThrows(InstruccionesInvalidasException.class, () -> {
            new Evento(TipoEvento.CPU, instrucciones);
        });
    }
}
