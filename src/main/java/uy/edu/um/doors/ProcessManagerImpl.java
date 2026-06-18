package uy.edu.um.doors;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;

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
import uy.edu.um.tad.list.MyLinkedListImpl;

public class ProcessManagerImpl implements ProcessManager {

    private MyQueue<Proceso> procesosNuevos;
    private MyHeap<Proceso> procesosPendientes;
    private MyStack<Proceso> procesosFinalizados;

    private MyHash<Integer, Usuario> usuarios;
    private MyHash<Integer, Proceso> procesosEnMemoria;

    private Proceso procesoEnEjecucion;
    private DoorsLogger logger;

    public ProcessManagerImpl() {
        this.procesosNuevos = new MyQueueImpl<>();
        this.procesosPendientes = new MyHeapImpl<>(false);
        this.procesosFinalizados = new MyStackImpl<>();

        this.usuarios = new MyHashImpl<>();
        this.procesosEnMemoria = new MyHashImpl<>();

        this.procesoEnEjecucion = null;
        this.logger = new DoorsLogger();
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

        Path path = Path.of(usersCsvPath);

        try (BufferedReader br = Files.newBufferedReader(path)) {

            String linea = br.readLine();//saltea encabezados

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
        Path path = Path.of(processCsvPath);

        try (BufferedReader br = Files.newBufferedReader(path)) {

            String linea = br.readLine(); //saltea encabezado

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

                String mensaje = "NEW PENDING PROCESS: PID=" + proceso.getPid()
                        + " | " + proceso.getNombre()
                        + " | USER:" + proceso.getUsuario().getAlias()
                        + " UID:" + proceso.getUsuario().getUid()
                        + " | P=" + proceso.getPrioridad();

                System.out.println(mensaje);
                logger.log(mensaje);

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

            String bloque = "EXECUTING PROCESS: PID=" + proceso.getPid()
                    + " | USER:" + proceso.getUsuario().getAlias()
                    + " UID:" + proceso.getUsuario().getUid();

            System.out.println(bloque);

            MyList<Evento> eventos = proceso.getEventos();

            for (int i = 0; i < eventos.size(); i++) {
                Evento evento = eventos.get(i);

                String lineaEvento = "EVENT: " + evento.getTipoEvento()
                        + " | Instructions " + instruccionesToString(evento.getInstrucciones());

                System.out.println(lineaEvento);
                bloque += System.lineSeparator() + lineaEvento;
            }

            logger.logBlock(bloque);

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

        String mensaje;

        if (estadoFinalizacion == EstadoFinalizacion.TERMINATED) {
            mensaje = "ENDING PROCESS: PID=" + proceso.getPid()
                    + " | STATE: TERMINATED by USER:" + usuarioResponsable.getAlias()
                    + " UID:" + usuarioResponsable.getUid();
        } else {
            mensaje = "ENDING PROCESS: PID=" + proceso.getPid()
                    + " | STATE: " + estadoFinalizacion;
        }

        System.out.println(mensaje);
        logger.log(mensaje);

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
            String bloque = "Finished process stack overflow";

            System.out.println(bloque);

            while (!procesosFinalizados.isEmpty()) {
                try {
                    Proceso procesoDescartado = procesosFinalizados.pop();

                    String lineaProceso = "PID=" + procesoDescartado.getPid()
                            + " " + procesoDescartado.getNombre()
                            + " | STATE: " + procesoDescartado.getEstadoFinalizacion()
                            + " | USER:" + procesoDescartado.getUsuario().getAlias()
                            + " UID:" + procesoDescartado.getUsuario().getUid();

                    System.out.println(lineaProceso);
                    bloque += System.lineSeparator() + lineaProceso;

                    procesosEnMemoria.remove(procesoDescartado.getPid());

                } catch (EmptyStackException e) {
                    System.out.println("Error al vaciar pila de finalizados.");
                }
            }

            logger.logBlock(bloque);
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

    private void imprimirProcesoResumen(Proceso proceso) {
        if (proceso == null) {
            return;
        }

        if (proceso.getEstado() == EstadoProceso.FINISHED) {
            System.out.println("PID=" + proceso.getPid()
                    + " " + proceso.getNombre()
                    + " | STATE: " + proceso.getEstadoFinalizacion()
                    + " | USER:" + proceso.getUsuario().getAlias()
                    + " UID:" + proceso.getUsuario().getUid());
        } else {
            System.out.println("PID=" + proceso.getPid()
                    + " | " + proceso.getNombre()
                    + " | USER:" + proceso.getUsuario().getAlias()
                    + " UID:" + proceso.getUsuario().getUid()
                    + " | P=" + proceso.getPrioridad());
        }
    }

    private void imprimirProcesoDetalle(Proceso proceso) {
        if (proceso == null) {
            return;
        }

        imprimirProcesoResumen(proceso);

        MyList<Evento> eventos = proceso.getEventos();

        for (int i = 0; i < eventos.size(); i++) {
            Evento evento = eventos.get(i);

            System.out.println("  EVENT: " + evento.getTipoEvento()
                    + " | Instructions " + instruccionesToString(evento.getInstrucciones()));
        }
    }

    private void imprimirPendientesOrdenados(boolean verbose) {

        MyHeap<Proceso> pendientesOrdenados = new MyHeapImpl<>(false);
        MyList<Proceso> procesos = procesosEnMemoria.values();

        for (int i = 0; i < procesos.size(); i++) {
            Proceso proceso = procesos.get(i);

            if (proceso.getEstado() == EstadoProceso.PENDING) {
                pendientesOrdenados.insert(proceso);
            }
        }

        if (pendientesOrdenados.isEmpty()) {
            System.out.println("No hay procesos pendientes.");
            return;
        }

        while (!pendientesOrdenados.isEmpty()) {
            try {
                Proceso proceso = pendientesOrdenados.remove();

                if (verbose) {
                    imprimirProcesoDetalle(proceso);
                } else {
                    imprimirProcesoResumen(proceso);
                }

            } catch (EmptyHeapException e) {
                System.out.println("Error al mostrar procesos pendientes.");
            }
        }
    }

    private void imprimirFinalizadosDesdePila(boolean verbose) {

        if (procesosFinalizados.isEmpty()) {
            System.out.println("No hay procesos finalizados en memoria.");
            return;
        }

        MyStack<Proceso> auxiliar = new MyStackImpl<>();

        while (!procesosFinalizados.isEmpty()) {
            try {
                Proceso proceso = procesosFinalizados.pop();

                if (verbose) {
                    imprimirProcesoDetalle(proceso);
                } else {
                    imprimirProcesoResumen(proceso);
                }

                auxiliar.push(proceso);

            } catch (EmptyStackException e) {
                System.out.println("Error al mostrar procesos finalizados.");
            }
        }

        while (!auxiliar.isEmpty()) {
            try {
                procesosFinalizados.push(auxiliar.pop());
            } catch (EmptyStackException e) {
                System.out.println("Error al restaurar pila de finalizados.");
            }
        }
    }

    @Override
    public void printStatus() {
        System.out.println("PROCESS STATUS");

        System.out.println("EXECUTING:");
        if (procesoEnEjecucion == null) {
            System.out.println("No hay proceso en ejecución.");
        } else {
            imprimirProcesoResumen(procesoEnEjecucion);
        }

        System.out.println("PENDING:");
        imprimirPendientesOrdenados(false);

        System.out.println("FINISHED:");
        imprimirFinalizadosDesdePila(false);

    }

    @Override
    public void printStatusVerbose() {
        System.out.println("PROCESS STATUS - VERBOSE");

        System.out.println("EXECUTING:");
        if (procesoEnEjecucion == null) {
            System.out.println("No hay proceso en ejecución.");
        } else {
            imprimirProcesoDetalle(procesoEnEjecucion);
        }

        System.out.println("PENDING:");
        imprimirPendientesOrdenados(true);

        System.out.println("FINISHED:");
        imprimirFinalizadosDesdePila(true);

    }

    @Override
    public void printStatusByUser(int uid) {
        Usuario usuario = usuarios.get(uid);

        if (usuario == null) {
            System.out.println("No existe usuario con UID=" + uid);
            return;
        }

        System.out.println("PROCESS STATUS BY USER");
        System.out.println("USER:" + usuario.getAlias() + " UID:" + usuario.getUid());

        MyList<Proceso> procesos = procesosEnMemoria.values();

        boolean encontrado = false;

        for (int i = 0; i < procesos.size(); i++) {
            Proceso proceso = procesos.get(i);

            if (proceso.getUsuario().getUid() == uid) {
                imprimirProcesoDetalle(proceso);
                encontrado = true;
            }
        }

        if (!encontrado) {
            System.out.println("No hay procesos cargados para el usuario indicado.");
        }
    }

    @Override
    public void printStatusByProcess(int pid) {
        Proceso proceso = procesosEnMemoria.get(pid);

        if (proceso == null) {
            System.out.println("No existe proceso cargado en memoria con PID=" + pid);
            return;
        }

        System.out.println("PROCESS STATUS BY PID");
        imprimirProcesoDetalle(proceso);
    }
}