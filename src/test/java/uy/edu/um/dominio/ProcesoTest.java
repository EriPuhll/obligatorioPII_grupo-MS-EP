package uy.edu.um.dominio;

import org.junit.jupiter.api.Test;
import uy.edu.um.tad.list.MyLinkedListImpl;
import uy.edu.um.tad.list.MyList;

import static org.junit.jupiter.api.Assertions.*;

public class ProcesoTest {

    private Evento crearEvento(TipoEvento tipo) throws Exception {
        MyList<String> instrucciones = new MyLinkedListImpl<>();
        instrucciones.add("inst");
        return new Evento(tipo, instrucciones);
    }

    @Test
    public void crearProcesoCorrectamente() throws Exception {
        Usuario usuario = new Usuario(1, "Hera", TipoUsuario.ADMIN);

        MyList<Evento> eventos = new MyLinkedListImpl<>();
        eventos.add(crearEvento(TipoEvento.CPU));

        Proceso proceso = new Proceso(10, "java.exe", usuario, eventos);

        assertEquals(10, proceso.getPid());
        assertEquals("java.exe", proceso.getNombre());
        assertEquals(usuario, proceso.getUsuario());
        assertEquals(EstadoProceso.NEW, proceso.getEstado());
        assertEquals(0, proceso.getPrioridad());
    }

    @Test
    public void calcularPrioridadCorrectamente() throws Exception {
        Usuario usuario = new Usuario(1, "Hera", TipoUsuario.ADMIN);

        MyList<Evento> eventos = new MyLinkedListImpl<>();
        eventos.add(crearEvento(TipoEvento.CPU));
        eventos.add(crearEvento(TipoEvento.RAM));
        eventos.add(crearEvento(TipoEvento.DISK));

        Proceso proceso = new Proceso(10, "java.exe", usuario, eventos);

        proceso.calcularPrioridad();

        assertEquals(100, proceso.getPrioridad());
    }

    @Test
    public void cambiarEstadosCorrectamente() throws Exception {
        Usuario usuario = new Usuario(1, "Hera", TipoUsuario.ADMIN);

        MyList<Evento> eventos = new MyLinkedListImpl<>();
        eventos.add(crearEvento(TipoEvento.CPU));

        Proceso proceso = new Proceso(10, "java.exe", usuario, eventos);

        proceso.pasarAPending();
        assertEquals(EstadoProceso.PENDING, proceso.getEstado());

        proceso.pasarARunning();
        assertEquals(EstadoProceso.RUNNING, proceso.getEstado());

        proceso.finalizar(EstadoFinalizacion.OK);
        assertEquals(EstadoProceso.FINISHED, proceso.getEstado());
        assertEquals(EstadoFinalizacion.OK, proceso.getEstadoFinalizacion());
    }
}