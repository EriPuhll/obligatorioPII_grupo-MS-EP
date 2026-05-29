package uy.edu.um.dominio;

import uy.edu.um.exceptions.DatoInvalidoException;
import uy.edu.um.exceptions.EstadoFinalizacionInvalidoException;
import uy.edu.um.exceptions.EstadoProcesoInvalidoException;
import uy.edu.um.exceptions.EventosInvalidosException;
import uy.edu.um.tad.list.MyList;

public class Proceso {

    private int pid;
    private String nombre;
    private Usuario usuario;
    private int prioridad;
    private String estado;
    private String estadoFinalizacion;
    private MyList<Evento> eventos;

    public Proceso(int pid, String nombre, Usuario usuario, MyList<Evento> eventos)
            throws EstadoProcesoInvalidoException, EventosInvalidosException, DatoInvalidoException {

        if (nombre == null || nombre.trim().isEmpty()) {
            throw new DatoInvalidoException("El nombre del proceso no puede estar vacío");
        }

        if (usuario == null) {
            throw new DatoInvalidoException("El proceso debe tener un usuario asociado");
        }

        this.pid = pid;
        this.nombre = nombre.trim();
        this.usuario = usuario;
        this.prioridad = 0;
        this.estadoFinalizacion = null;
        setEstado("NEW");
        setEventos(eventos);
    }

    public int getPid() {
        return pid;
    }

    public String getNombre() {
        return nombre;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    // el calculo de la prioridad lo haremos en el ProcessManager
    public int getPrioridad() {
        return prioridad;
    }

    public void setPrioridad(int prioridad) {
        this.prioridad = prioridad;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) throws EstadoProcesoInvalidoException {
        if (estado == null) {
            throw new EstadoProcesoInvalidoException();
        }

        estado = estado.trim().toUpperCase();

        if (!estado.equals("NEW") &&
                !estado.equals("PENDING") &&
                !estado.equals("RUNNING") &&
                !estado.equals("FINISHED")) {
            throw new EstadoProcesoInvalidoException();
        }

        this.estado = estado;
    }

    public String getEstadoFinalizacion() {
        return estadoFinalizacion;
    }

    public void setEstadoFinalizacion(String estadoFinalizacion) throws EstadoFinalizacionInvalidoException {
        if (estadoFinalizacion == null) {
            this.estadoFinalizacion = null;
            return;
        }

        estadoFinalizacion = estadoFinalizacion.trim().toUpperCase();

        if (!estadoFinalizacion.equals("OK") &&
                !estadoFinalizacion.equals("ERROR") &&
                !estadoFinalizacion.equals("TERMINATED")) {
            throw new EstadoFinalizacionInvalidoException();
        }

        this.estadoFinalizacion = estadoFinalizacion;
    }

    public MyList<Evento> getEventos() {
        return eventos;
    }

    public void setEventos(MyList<Evento> eventos) throws EventosInvalidosException {
        if (eventos == null || eventos.size() == 0) {
            throw new EventosInvalidosException();
        }

        this.eventos = eventos;
    }

    // metodo para imprimir proceso con sus eventos
    public String eventosToString() {
        String resultado = "";

        for (int i = 0; i < eventos.size(); i++) {
            resultado += eventos.get(i).toString();

            if (i < eventos.size() - 1) {
                resultado += "\n";
            }
        }

        return resultado;
    }

    @Override
    public String toString() {
        return "PID=" + pid + " | " + nombre + " | " + usuario + " | P=" + prioridad;
    }
}