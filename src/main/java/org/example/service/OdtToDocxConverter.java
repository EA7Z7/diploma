package org.example.service;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.PumpStreamHandler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class OdtToDocxConverter { // Исправлено имя класса

    // Для Windows (экранированные слеши)
    private static final String LIBRE_OFFICE_PATH = "C:\\Program Files\\LibreOffice\\program\\soffice.exe";

    // Для Linux/Mac:
    // private static final String LIBRE_OFFICE_PATH = "/usr/bin/soffice";

    public static void convert(String inputPath, String outputPath) throws IOException {
        CommandLine cmd = new CommandLine(LIBRE_OFFICE_PATH);
        cmd.addArgument("--headless");
        cmd.addArgument("--convert-to");
        cmd.addArgument("docx");
        cmd.addArgument("--outdir");
        cmd.addArgument(outputPath);
        cmd.addArgument(inputPath);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ByteArrayOutputStream errorStream = new ByteArrayOutputStream();

        PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream, errorStream);
        DefaultExecutor executor = new DefaultExecutor();
        executor.setStreamHandler(streamHandler);

        try {
            int exitValue = executor.execute(cmd); // Выполняем команду
            if (exitValue != 0) {
                throw new IOException("Conversion failed!\n"
                        + "Output: " + outputStream.toString() + "\n"
                        + "Error: " + errorStream.toString());
            }
        } catch (ExecuteException e) {
            throw new IOException("Conversion error: " + e.getMessage() + "\n"
                    + "Output: " + outputStream.toString() + "\n"
                    + "Error: " + errorStream.toString());
        }
    }
}