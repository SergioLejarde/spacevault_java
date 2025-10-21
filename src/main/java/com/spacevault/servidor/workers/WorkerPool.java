package com.spacevault.servidor.workers;

import java.io.File;
import java.util.concurrent.*;

/**
 * WorkerPool – procesamiento concurrente para métricas de uso.
 * Calcula el número de archivos y el tamaño total almacenado en cada nodo.
 */
public class WorkerPool {

    private final ExecutorService pool;

    public WorkerPool(int threads) {
        this.pool = Executors.newFixedThreadPool(threads);
    }

    /** Calcula métricas de almacenamiento en paralelo para Nodo1 y Nodo2 */
    public void calcularMetricas() {
        System.out.println("⚙️ Iniciando cálculo concurrente de métricas de nodos...");

        // Tareas en paralelo
        Future<String> nodo1 = pool.submit(() -> analizarNodo("data-Node1"));
        Future<String> nodo2 = pool.submit(() -> analizarNodo("data-Node2"));

        try {
            System.out.println(nodo1.get());
            System.out.println(nodo2.get());
        } catch (Exception e) {
            System.err.println("❌ Error al calcular métricas: " + e.getMessage());
        }

        pool.shutdown();
        System.out.println("✅ Procesamiento concurrente finalizado.");
    }

    /** Analiza un nodo (carpeta) y devuelve un resumen */
    private String analizarNodo(String rutaBase) {
        File base = new File(rutaBase);
        if (!base.exists()) return "⚠️ Nodo " + rutaBase + " no existe.";

        long totalArchivos = contarArchivos(base);
        long totalBytes = calcularTamanio(base);

        double mb = totalBytes / (1024.0 * 1024.0);
        return String.format("📊 %s: %d archivos, %.2f MB", rutaBase, totalArchivos, mb);
    }

    /** Cuenta todos los archivos dentro de una carpeta */
    private long contarArchivos(File dir) {
        if (!dir.isDirectory()) return 1;
        long count = 0;
        File[] files = dir.listFiles();
        if (files != null) {
            for (File f : files) {
                count += contarArchivos(f);
            }
        }
        return count;
    }

    /** Calcula el tamaño total en bytes de todos los archivos */
    private long calcularTamanio(File dir) {
        if (!dir.isDirectory()) return dir.length();
        long total = 0;
        File[] files = dir.listFiles();
        if (files != null) {
            for (File f : files) {
                total += calcularTamanio(f);
            }
        }
        return total;
    }

    /** Método de prueba manual (puedes ejecutarlo solo) */
    public static void main(String[] args) {
        WorkerPool pool = new WorkerPool(4);
        pool.calcularMetricas();
    }
}
