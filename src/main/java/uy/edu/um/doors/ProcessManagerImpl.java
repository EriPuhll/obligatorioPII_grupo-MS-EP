package uy.edu.um.doors;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import uy.edu.um.dominio.EstadoFinalizacion;
import uy.edu.um.dominio.EstadoProceso;
import uy.edu.um.dominio.Evento;
import uy.edu.um.dominio.Proceso;
import uy.edu.um.dominio.Usuario;
import uy.edu.um.dominio.TipoEvento;

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
import uy.edu.um.tad.list.MyLinkedListImpl;

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
        cargarUsuarios(usersCsvPath);
        cargarProcesos(processCsvPath);

        System.out.println("Carga finalizada.");
        System.out.println("Usuarios cargados: " + usuarios.size());
        System.out.println("Procesos cargados en NEW: " + procesosNuevos.size());
    }

    private void cargarUsuarios(String usersCsvPath) {
        try (BufferedReader br = new BufferedReader(new FileReader(usersCsvPath))) {
            String linea = br.readLine(); // saltea encabezado: uid;alias;type

            while ((linea = br.readLine()) != null) {
                if (linea.trim().isEmpty()) {
                    continue;
                }

                String[] partes = linea.split(";");

                if (partes.length != 3) {
                    System.out.println("Línea de usuario inválida: " + linea);
                    continue;
                }

                try {
                    int uid = Integer.parseInt(partes[0].trim());
                    String alias = partes[1].trim();
                    String tipo = partes[2].trim();

                    if (usuarios.contains(uid)) {
                        System.out.println("Usuario duplicado, se ignora UID=" + uid);
                        continue;
                    }

                    Usuario usuario = new Usuario(uid, alias, tipo);
                    usuarios.put(uid, usuario);

                } catch (Exception e) {
                    System.out.println("Error al cargar usuario: " + linea);
                }
            }

        } catch (IOException e) {
            System.out.println("No se pudo leer el archivo de usuarios.");
        }
    }

    private void cargarProcesos(String processCsvPath) {
        try (BufferedReader br = new BufferedReader(new FileReader(processCsvPath))) {
            String linea = br.readLine(); // saltea encabezado: pid;uid;name;events

            while ((linea = br.readLine()) != null) {
                if (linea.trim().isEmpty()) {
                    continue;
                }

                String[] partes = linea.split(";", 4);

                if (partes.length != 4) {
                    System.out.println("Línea de proceso inválida: " + linea);
                    continue;
                }

                try {
                    int pid = Integer.parseInt(partes[0].trim());
                    int uid = Integer.parseInt(partes[1].trim());
                    String nombre = partes[2].trim();
                    String eventosTexto = partes[3].trim();

                    if (procesosEnMemoria.contains(pid)) {
                        System.out.println("Proceso duplicado, se ignora PID=" + pid);
                        continue;
                    }

                    Usuario usuario = usuarios.get(uid);

                    if (usuario == null) {
                        System.out.println("No existe usuario para el proceso PID=" + pid + " UID=" + uid);
                        continue;
                    }

                    MyList<Evento> eventos = parsearEventos(eventosTexto);

                    Proceso proceso = new Proceso(pid, nombre, usuario, eventos);

                    procesosNuevos.enqueue(proceso);
                    procesosEnMemoria.put(pid, proceso);

                } catch (Exception e) {
                    System.out.println("Error al cargar proceso: " + linea);
                }
            }

        } catch (IOException e) {
            System.out.println("No se pudo leer el archivo de procesos.");
        }
    }

    private MyList<Evento> parsearEventos(String eventosTexto) throws Exception {
        MyList<Evento> eventos = new MyLinkedListImpl<>();

        if (eventosTexto == null || eventosTexto.trim().isEmpty()) {
            return eventos;
        }

        eventosTexto = eventosTexto.trim();

        if (eventosTexto.startsWith("{")) {
            eventosTexto = eventosTexto.substring(1);
        }

        if (eventosTexto.endsWith("}")) {
            eventosTexto = eventosTexto.substring(0, eventosTexto.length() - 1);
        }

        String[] eventosSeparados = eventosTexto.split("#");

        for (int i = 0; i < eventosSeparados.length; i++) {
            String eventoTexto = eventosSeparados[i].trim();

            if (eventoTexto.isEmpty()) {
                continue;
            }

            int posicionDosPuntos = eventoTexto.indexOf(":");

            if (posicionDosPuntos == -1) {
                continue;
            }

            String tipoTexto = eventoTexto.substring(0, posicionDosPuntos).trim();
            String instruccionesTexto = eventoTexto.substring(posicionDosPuntos + 1).trim();

            if (instruccionesTexto.startsWith("[")) {
                instruccionesTexto = instruccionesTexto.substring(1);
            }

            if (instruccionesTexto.endsWith("]")) {
                instruccionesTexto = instruccionesTexto.substring(0, instruccionesTexto.length() - 1);
            }

            MyList<String> instrucciones = parsearInstrucciones(instruccionesTexto);

            Evento evento = new Evento(tipoTexto, instrucciones);
            eventos.add(evento);
        }

        return eventos;
    }

    private MyList<String> parsearInstrucciones(String instruccionesTexto) {
        MyList<String> instrucciones = new MyLinkedListImpl<>();

        if (instruccionesTexto == null || instruccionesTexto.trim().isEmpty()) {
            return instrucciones;
        }

        String[] instruccionesSeparadas = instruccionesTexto.split(",");

        for (int i = 0; i < instruccionesSeparadas.length; i++) {
            String instruccion = instruccionesSeparadas[i].trim();

            if (!instruccion.isEmpty()) {
                instrucciones.add(instruccion);
            }
        }

        return instrucciones;
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