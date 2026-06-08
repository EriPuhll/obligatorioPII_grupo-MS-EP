package uy.edu.um.doors;

import uy.edu.um.dominio.EstadoFinalizacion;
import uy.edu.um.dominio.EstadoProceso;
import uy.edu.um.dominio.Evento;
import uy.edu.um.dominio.Proceso;
import uy.edu.um.dominio.Usuario;

import uy.edu.um.exceptions.EstadoFinalizacionInvalidoException;
import uy.edu.um.exceptions.EstadoProcesoInvalidoException;

import uy.edu.um.tad.queue.MyQueue;
import uy.edu.um.tad.queue.MyQueueImpl;
import uy.edu.um.tad.queue.EmptyQueueException;

import uy.edu.um.tad.heap.MyHeap;
import uy.edu.um.tad.heap.MyHeapImpl;
import uy.edu.um.tad.heap.EmptyHeapException;

import uy.edu.um.tad.stack.MyStack;
import uy.edu.um.tad.stack.MyStackImpl;
import uy.edu.um.tad.stack.EmptyStackException;

import uy.edu.um.tad.hash.MyHash;
import uy.edu.um.tad.hash.MyHashImpl;

import uy.edu.um.tad.list.MyList;

public class ProcessManagerImpl implements ProcessManager {

    private MyQueue<Proceso> procesosNuevos;
    private MyHeap<Proceso> procesosPendientes;
    private MyStack<Proceso> procesosFinalizados;

    private MyHash<Integer, Usuario> usuarios;
    private MyHash<Integer, Proceso> procesosEnMemoria;

    private Proceso procesoEnEjecucion;

    public ProcessManagerImpl() {
        this.procesosNuevos = new MyQueueImpl<>();
        this.procesosPendientes = new MyHeapImpl<>(false);
        this.procesosFinalizados = new MyStackImpl<>();

        this.usuarios = new MyHashImpl<>();
        this.procesosEnMemoria = new MyHashImpl<>();

        this.procesoEnEjecucion = null;
    }

    @Override
    public void loadProcessAndUserData(String processCsvPath, String usersCsvPath) {
        System.out.println("IMPLEMENTAR CARGA CSV");
    }

    @Override
    public void prepareProcesses() {
        while (!procesosNuevos.isEmpty()) {
            try {
                Proceso proceso = procesosNuevos.dequeue();

                proceso.calcularPrioridad();
                proceso.setEstado(EstadoProceso.PENDING);

                procesosPendientes.insert(proceso);

                System.out.println("NEW PENDING PROCESS: PID=" + proceso.getPid()
                        + " | " + proceso.getNombre()
                        + " | USER:" + proceso.getUsuario().getAlias()
                        + " UID:" + proceso.getUsuario().getUid()
                        + " | P=" + proceso.getPrioridad());

            } catch (EmptyQueueException e) {
                System.out.println("No hay procesos nuevos para preparar.");
            } catch (EstadoProcesoInvalidoException e) {
                System.out.println("No se pudo cambiar el proceso a estado PENDING.");
            }
        }
    }

    @Override
    public void executeNextProcess() {
        if (procesoEnEjecucion != null) {
            System.out.println("Ya existe un proceso en ejecución.");
            return;
        }

        if (procesosPendientes.isEmpty()) {
            System.out.println("No hay procesos pendientes para ejecutar.");
            return;
        }

        try {
            Proceso proceso = procesosPendientes.remove();

            proceso.setEstado(EstadoProceso.RUNNING);
            procesoEnEjecucion = proceso;

            System.out.println("EXECUTING PROCESS: PID=" + proceso.getPid()
                    + " | USER:" + proceso.getUsuario().getAlias()
                    + " UID:" + proceso.getUsuario().getUid());

            MyList<Evento> eventos = proceso.getEventos();

            for (int i = 0; i < eventos.size(); i++) {
                Evento evento = eventos.get(i);

                System.out.println("EVENT: " + evento.getTipoEvento()
                        + " | Instructions " + instruccionesToString(evento.getInstrucciones()));
            }

        } catch (EmptyHeapException e) {
            System.out.println("No hay procesos pendientes para ejecutar.");
        } catch (EstadoProcesoInvalidoException e) {
            System.out.println("No se pudo cambiar el proceso a estado RUNNING.");
        }
    }

    private void finalizarProceso(EstadoFinalizacion estadoFinalizacion, Integer uidResponsable) {
        if (procesoEnEjecucion == null) {
            System.out.println("No hay proceso en ejecución.");
            return;
        }

        Usuario usuarioResponsable = null;

        if (estadoFinalizacion == EstadoFinalizacion.TERMINATED) {
            usuarioResponsable = usuarios.get(uidResponsable);

            if (usuarioResponsable == null) {
                System.out.println("No existe el usuario responsable de la terminación.");
                return;
            }
        }

        Proceso proceso = procesoEnEjecucion;

        try {
            proceso.setEstado(EstadoProceso.FINISHED);
            proceso.setEstadoFinalizacion(estadoFinalizacion);
        } catch (EstadoProcesoInvalidoException e) {
            System.out.println("No se pudo cambiar el proceso a estado FINISHED.");
            return;
        } catch (EstadoFinalizacionInvalidoException e) {
            System.out.println("El estado de finalización no es válido.");
            return;
        }

        if (estadoFinalizacion == EstadoFinalizacion.TERMINATED) {
            System.out.println("ENDING PROCESS: PID=" + proceso.getPid()
                    + " | STATE: TERMINATED by USER:" + usuarioResponsable.getAlias()
                    + " UID:" + usuarioResponsable.getUid());
        } else {
            System.out.println("ENDING PROCESS: PID=" + proceso.getPid()
                    + " | STATE: " + estadoFinalizacion);
        }

        agregarProcesoFinalizado(proceso);
        procesoEnEjecucion = null;
    }

    @Override
    public void finishProcessOk() {
        finalizarProceso(EstadoFinalizacion.OK, null);
    }

    @Override
    public void finishProcessError() {
        finalizarProceso(EstadoFinalizacion.ERROR, null);
    }

    @Override
    public void terminateProcess(int uid) {
        finalizarProceso(EstadoFinalizacion.TERMINATED, uid);
    }

    private void agregarProcesoFinalizado(Proceso proceso) {
        if (procesosFinalizados.size() == MAX_FINISHED_PROCESS_ON_RAM) {
            System.out.println("Finished process stack overflow");

            while (!procesosFinalizados.isEmpty()) {
                try {
                    Proceso procesoDescartado = procesosFinalizados.pop();

                    System.out.println("PID=" + procesoDescartado.getPid()
                            + " " + procesoDescartado.getNombre()
                            + " | STATE: " + procesoDescartado.getEstadoFinalizacion()
                            + " | USER:" + procesoDescartado.getUsuario().getAlias()
                            + " UID:" + procesoDescartado.getUsuario().getUid());

                } catch (EmptyStackException e) {
                    System.out.println("Error al vaciar pila de finalizados.");
                }
            }
        }

        procesosFinalizados.push(proceso);
    }

    private String instruccionesToString(MyList<String> instrucciones) {
        String resultado = "[";

        for (int i = 0; i < instrucciones.size(); i++) {
            resultado += instrucciones.get(i);

            if (i < instrucciones.size() - 1) {
                resultado += ", ";
            }
        }

        resultado += "]";

        return resultado;
    }

    @Override
    public void printStatus() {
        System.out.println("IMPLEMENTAR STATUS");
    }

    @Override
    public void printStatusVerbose() {
        System.out.println("IMPLEMENTAR STATUS VERBOSE");
    }

    @Override
    public void printStatusByUser(int uid) {
        System.out.println("IMPLEMENTAR STATUS POR USUARIO");
    }

    @Override
    public void printStatusByProcess(int pid) {
        System.out.println("IMPLEMENTAR STATUS POR PROCESO");
    }
}