package uy.edu.um.dominio;

import uy.edu.um.exceptions.DatoInvalidoException;
import uy.edu.um.exceptions.EstadoFinalizacionInvalidoException;
import uy.edu.um.exceptions.EstadoProcesoInvalidoException;
import uy.edu.um.exceptions.EventosInvalidosException;
import uy.edu.um.tad.list.MyList;

public class Proceso implements Comparable<Proceso> {

    private int pid;
    private String nombre;
    private Usuario usuario;
    private int prioridad;
    private EstadoProceso estado;
    private EstadoFinalizacion estadoFinalizacion;
    private MyList<Evento> eventos;

    // Constructor
    public Proceso(int pid, String nombre, Usuario usuario, MyList<Evento> eventos)
            throws EstadoProcesoInvalidoException, EventosInvalidosException, DatoInvalidoException {

        if (pid <= 0) {
            throw new DatoInvalidoException("El PID del proceso debe ser mayor a cero");
        }

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
        this.estado = EstadoProceso.NEW;
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

    // el calculo de la prioridad se realiza según los eventos y el tipo de usuario
    public void calcularPrioridad() {
        int cantidadCpu = 0;
        int cantidadRam = 0;
        int cantidadDisk = 0;

        int totalEventos = eventos.size();

        if (totalEventos == 0) {
            this.prioridad = 0;
            return;
        }

        for (int i = 0; i < eventos.size(); i++) {
            Evento evento = eventos.get(i);

            if (evento.getTipoEvento() == TipoEvento.CPU) {
                cantidadCpu++;
            } else if (evento.getTipoEvento() == TipoEvento.RAM) {
                cantidadRam++;
            } else if (evento.getTipoEvento() == TipoEvento.DISK) {
                cantidadDisk++;
            }
        }

        int pesoUsuario;

        if (usuario.getTipo() == TipoUsuario.ADMIN) {
            pesoUsuario = 32;
        } else {
            pesoUsuario = 16;
        }

        this.prioridad = ((8 * cantidadCpu + 2 * cantidadRam + 2 * cantidadDisk) / totalEventos)
                + pesoUsuario * totalEventos;
    }

    public int getPrioridad() {
        return prioridad;
    }

    public void setPrioridad(int prioridad) {
        this.prioridad = prioridad;
    }

    public EstadoProceso getEstado() {
        return estado;
    }

    // Valida y convierte el texto recibido al enum EstadoProceso
    public void setEstado(String estado) throws EstadoProcesoInvalidoException {
        if (estado == null || estado.trim().isEmpty()) {
            throw new EstadoProcesoInvalidoException();
        }

        try {
            this.estado = EstadoProceso.valueOf(estado.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new EstadoProcesoInvalidoException();
        }
    }

    // Valida y asigna directamente el enum EstadoProceso
    public void setEstado(EstadoProceso estado) throws EstadoProcesoInvalidoException {
        if (estado == null) {
            throw new EstadoProcesoInvalidoException();
        }

        this.estado = estado;
    }

    public EstadoFinalizacion getEstadoFinalizacion() {
        return estadoFinalizacion;
    }

    // Valida y convierte el texto recibido al enum EstadoFinalizacion
    public void setEstadoFinalizacion(String estadoFinalizacion) throws EstadoFinalizacionInvalidoException {
        if (estadoFinalizacion == null) {
            this.estadoFinalizacion = null;
            return;
        }

        estadoFinalizacion = estadoFinalizacion.trim().toUpperCase();

        if (estadoFinalizacion.equals("TERM")) {
            estadoFinalizacion = "TERMINATED";
        }

        if (!estadoFinalizacion.equals("OK") &&
                !estadoFinalizacion.equals("ERROR") &&
                !estadoFinalizacion.equals("TERMINATED")) {
            throw new EstadoFinalizacionInvalidoException();
        }

        this.estadoFinalizacion = EstadoFinalizacion.valueOf(estadoFinalizacion);
    }

    // Valida y asigna directamente el enum EstadoFinalizacion
    public void setEstadoFinalizacion(EstadoFinalizacion estadoFinalizacion)
            throws EstadoFinalizacionInvalidoException {

        if (estadoFinalizacion == null) {
            this.estadoFinalizacion = null;
            return;
        }

        this.estadoFinalizacion = estadoFinalizacion;
    }

    public MyList<Evento> getEventos() {
        return eventos;
    }

    // Valida que el proceso tenga al menos un evento y que no haya eventos nulos
    public void setEventos(MyList<Evento> eventos) throws EventosInvalidosException {
        if (eventos == null || eventos.size() == 0) {
            throw new EventosInvalidosException();
        }

        for (int i = 0; i < eventos.size(); i++) {
            if (eventos.get(i) == null) {
                throw new EventosInvalidosException();
            }
        }

        this.eventos = eventos;
    }

    // Cambia el proceso a estado PENDING.
    public void pasarAPending() throws EstadoProcesoInvalidoException {
        setEstado(EstadoProceso.PENDING);
    }

    // Cambia el proceso a estado RUNNING.
    public void pasarARunning() throws EstadoProcesoInvalidoException {
        setEstado(EstadoProceso.RUNNING);
    }

    // Cambia el proceso a estado FINISHED y asigna cómo finalizó.
    public void finalizar(String estadoFinalizacion)
            throws EstadoProcesoInvalidoException, EstadoFinalizacionInvalidoException {
        setEstado(EstadoProceso.FINISHED);
        setEstadoFinalizacion(estadoFinalizacion);
    }

    // Cambia el proceso a estado FINISHED y asigna cómo finalizó usando enum.
    public void finalizar(EstadoFinalizacion estadoFinalizacion)
            throws EstadoProcesoInvalidoException, EstadoFinalizacionInvalidoException {
        setEstado(EstadoProceso.FINISHED);
        setEstadoFinalizacion(estadoFinalizacion);
    }

    // metodo para imprimir proceso con sus eventos
    private String eventosToString() {
        String resultado = "";

        for (int i = 0; i < eventos.size(); i++) {
            resultado += eventos.get(i).toString();

            if (i < eventos.size() - 1) {
                resultado += "\n";
            }
        }

        return resultado;
    }

    // agrego metodo comparable
    @Override
    public int compareTo(Proceso otro) {
        return Integer.compare(this.prioridad, otro.prioridad);
    }

    @Override
    public String toString() {
        return "PID=" + pid + " | " + nombre + " | " + usuario + " | P=" + prioridad;
    }
}