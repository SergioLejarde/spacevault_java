package com.spacevault.nodos;

import java.io.*;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class NodeRemoteImpl extends UnicastRemoteObject implements NodeRemote {

    private static final String BASE_DIR = "data-Node1";

    public NodeRemoteImpl() throws RemoteException {
        super();
        File base = new File(BASE_DIR);
        if (!base.exists()) base.mkdirs();
    }

    @Override
    public void crearDirectorio(String ruta) throws RemoteException {
        File dir = new File(BASE_DIR + File.separator + ruta);
        if (!dir.exists()) {
            dir.mkdirs();
            System.out.println("📁 Directorio creado: " + dir.getAbsolutePath());
        }
    }

    @Override
    public void guardarArchivo(String ruta, String nombre, byte[] datos) throws RemoteException {
        try {
            File dir = new File(BASE_DIR + File.separator + ruta);
            if (!dir.exists()) dir.mkdirs();
            File f = new File(dir, nombre);
            try (FileOutputStream fos = new FileOutputStream(f)) {
                fos.write(datos);
            }
            System.out.println("💾 Archivo guardado: " + f.getAbsolutePath());
        } catch (IOException e) {
            throw new RemoteException("Error al guardar archivo", e);
        }
    }

    @Override
    public byte[] leerArchivo(String ruta, String nombre) throws RemoteException {
        try {
            File f = new File(BASE_DIR + File.separator + ruta + File.separator + nombre);
            if (!f.exists()) return null;
            try (FileInputStream fis = new FileInputStream(f)) {
                return fis.readAllBytes();
            }
        } catch (IOException e) {
            throw new RemoteException("Error al leer archivo", e);
        }
    }

    // 🗑️ Eliminar archivo o carpeta
    @Override
    public void eliminarArchivo(String ruta, String nombre) throws RemoteException {
        try {
            File objetivo = new File(BASE_DIR + File.separator + ruta, nombre);
            if (!objetivo.exists()) {
                System.out.println("⚠️ Archivo o directorio no encontrado: " + objetivo.getAbsolutePath());
                return;
            }

            if (objetivo.isDirectory()) {
                eliminarDirectorioRecursivo(objetivo);
                System.out.println("🗑️ Directorio eliminado: " + objetivo.getAbsolutePath());
            } else {
                objetivo.delete();
                System.out.println("🗑️ Archivo eliminado: " + objetivo.getAbsolutePath());
            }
        } catch (Exception e) {
            throw new RemoteException("Error eliminando archivo o directorio", e);
        }
    }

    private void eliminarDirectorioRecursivo(File dir) {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) eliminarDirectorioRecursivo(f);
                else f.delete();
            }
        }
        dir.delete();
    }

    // 📦 Mover o renombrar archivo
    @Override
    public void moverArchivo(String rutaVieja, String rutaNueva) throws RemoteException {
        try {
            File origen = new File(BASE_DIR + File.separator + rutaVieja);
            File destino = new File(BASE_DIR + File.separator + rutaNueva);

            if (!origen.exists()) {
                System.out.println("⚠️ Archivo origen no encontrado: " + origen.getAbsolutePath());
                return;
            }

            destino.getParentFile().mkdirs();

            if (origen.renameTo(destino)) {
                System.out.println("📦 Archivo movido de " + origen.getAbsolutePath() + " a " + destino.getAbsolutePath());
            } else {
                throw new IOException("No se pudo mover el archivo o directorio");
            }
        } catch (Exception e) {
            throw new RemoteException("Error moviendo archivo o directorio", e);
        }
    }
}
