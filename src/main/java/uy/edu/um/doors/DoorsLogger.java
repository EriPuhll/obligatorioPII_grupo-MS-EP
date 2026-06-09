package uy.edu.um.doors;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DoorsLogger {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final DateTimeFormatter TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final String logFileName;

    public DoorsLogger() {
        String fecha = LocalDate.now().format(DATE_FORMAT);
        this.logFileName = "DOORS_PROCESS_LOG_" + fecha;
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
        try (FileWriter writer = new FileWriter(logFileName, true)) {
            writer.write(texto);
            writer.write(System.lineSeparator());
        } catch (IOException e) {
            System.out.println("No se pudo escribir en el archivo de log.");
        }
    }
}