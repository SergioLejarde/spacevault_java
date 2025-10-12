package com.spacevault.servidor.workers;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WorkerPool {
    private static final int NUM_WORKERS = 4;
    private static final ExecutorService pool = Executors.newFixedThreadPool(NUM_WORKERS);

    public static void ejecutarTarea(Runnable tarea) {
        pool.execute(tarea);
    }
}
