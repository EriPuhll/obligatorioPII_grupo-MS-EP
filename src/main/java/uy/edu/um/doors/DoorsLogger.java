package uy.edu.um.doors;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DoorsLogger {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final DateTimeFormatter TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");


    private final Path logFilePath;

    public DoorsLogger() {
        String fecha = LocalDate.now().format(DATE_FORMAT);
        String logFileName = "DOORS_PROCESS_LOG_" + fecha;
        this.logFilePath = Path.of(logFileName);
    }

    public void log(String mensaje) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        String linea = "[" + timestamp + "]: " + mensaje;

        escribir(linea);
    }

    public void logBlock(String bloque) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        String texto = "[" + timestamp + "]: " + bloque;

        escribir(texto);
    }

    private void escribir(String texto) {
        try (BufferedWriter writer = Files.newBufferedWriter(logFilePath, StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {

            writer.write(texto);
            writer.newLine();

        } catch (IOException e) {
            System.out.println("No se pudo escribir en el archivo de log." + e.getMessage());
        }
    }
}