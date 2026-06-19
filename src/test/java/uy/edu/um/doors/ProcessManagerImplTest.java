package uy.edu.um.doors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class ProcessManagerImplTest {

    @TempDir
    Path tempDir;

    @Test
    public void cargarPrepararEjecutarYFinalizarProcesoCorrectamente() throws Exception {
        Path users = tempDir.resolve("users.csv");
        Path processes = tempDir.resolve("process.csv");

        Files.writeString(users,
                "uid;alias;type\n" +
                        "1;Hera;ADMIN\n");

        Files.writeString(processes,
                "pid;uid;name;events\n" +
                        "10;1;java.exe;CPU:mov,add|RAM:load\n");

        ProcessManagerImpl manager = new ProcessManagerImpl();

        assertDoesNotThrow(() -> {
            manager.loadProcessAndUserData(processes.toString(), users.toString());
            manager.prepareProcesses();
            manager.executeNextProcess();
            manager.finishProcessOk();
        });
    }
}
