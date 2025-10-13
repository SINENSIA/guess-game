package com.sinensia.games;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class FileIO implements GameIO {

    private final BufferedReader reader;

    public FileIO(String rutaArchivo) throws IOException {
        this.reader = new BufferedReader(new FileReader(rutaArchivo));
    }

    @Override
    public String read() {
        try {
            String linea = reader.readLine();
            if (linea == null) {
                throw new RuntimeException("Fin de archivo alcanzado");
            }
            return linea.trim();
        } catch (IOException _) {
            throw new RuntimeException("Error leyendo del archivo");
        }
    }

    @Override
    public void print(String mensaje) {
        System.out.println("[SALIDA]" + mensaje);
    }

}
