package com.spacevault.nodos;

import java.io.*;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * Implementación RMI de un nodo de almacenamiento distribuido.
 * Cada instancia representa un nodo físico (data-Node1, data-Node2, etc.)
 */
public class NodeRemoteImpl extends UnicastRemoteObject implements NodeRemote {

    private final String BASE_DIR;

    // 🔹 Constructor por defecto (para Nodo1)
    public NodeRemoteImpl() throws RemoteException {
        this("data-Node1");
    }

    // 🔹 Constructor personalizado (para Nodo2, o futuros nodos)
    public NodeRemoteImpl(String baseDir) throws RemoteException {
        super();
        this.BASE_DIR = baseDir;
        File base = new File(BASE_DIR);
        if (!base.exists()) base.mkdirs();
        System.out.println("💾 Nodo inicializado con base: " + base.getAbsolutePath());
    }

    /** 📁 Crear directorio */
    @Override
    public void crearDirectorio(String ruta) throws RemoteException {
        File dir = new File(BASE_DIR + File.separator + ruta);
        if (!dir.exists()) {
            dir.mkdirs();
            System.out.println("📁(" + BASE_DIR + ") Directorio creado: " + dir.getAbsolutePath());
        } else {
            System.out.println("ℹ️(" + BASE_DIR + ") Directorio ya existente: " + dir.getAbsolutePath());
        }
    }

    /** 💾 Guardar archivo */
    @Override
    public void guardarArchivo(String ruta, String nombre, byte[] datos) throws RemoteException {
        try {
            File dir = new File(BASE_DIR + File.separator + ruta);
            if (!dir.exists()) dir.mkdirs();
            File f = new File(dir, nombre);
            try (FileOutputStream fos = new FileOutputStream(f)) {
                fos.write(datos);
            }
            System.out.println("💾(" + BASE_DIR + ") Archivo guardado: " + f.getAbsolutePath() + " (" + datos.length + " bytes)");
        } catch (IOException e) {
            throw new RemoteException("Error al guardar archivo (" + BASE_DIR + ")", e);
        }
    }

    /** 📖 Leer archivo */
    @Override
    public byte[] leerArchivo(String ruta, String nombre) throws RemoteException {
        try {
            File f = new File(BASE_DIR + File.separator + ruta + File.separator + nombre);
            if (!f.exists()) {
                System.out.println("⚠️(" + BASE_DIR + ") Archivo no encontrado: " + f.getAbsolutePath());
                return null;
            }
            try (FileInputStream fis = new FileInputStream(f)) {
                byte[] data = fis.readAllBytes();
                System.out.println("📖(" + BASE_DIR + ") Archivo leído: " + f.getAbsolutePath() + " (" + data.length + " bytes)");
                return data;
            }
        } catch (IOException e) {
            throw new RemoteException("Error al leer archivo (" + BASE_DIR + ")", e);
        }
    }

    /** 🗑️ Eliminar archivo */
    @Override
    public boolean eliminarArchivo(String ruta, String nombre) throws RemoteException {
        File f = new File(BASE_DIR + File.separator + ruta + File.separator + nombre);
        boolean ok = f.exists() && f.delete();
        System.out.println("🗑️(" + BASE_DIR + ") Eliminar " + f.getAbsolutePath() + " -> " + ok);
        return ok;
    }

    /** 🚚 Mover o renombrar archivo */
    @Override
    public boolean moverArchivo(String origen, String destino) throws RemoteException {
        File src = new File(BASE_DIR + File.separator + origen);
        File dst = new File(BASE_DIR + File.separator + destino);

        if (!src.exists()) {
            System.out.println("⚠️(" + BASE_DIR + ") Archivo origen no existe: " + src.getAbsolutePath());
            return false;
        }

        dst.getParentFile().mkdirs();
        boolean ok = src.renameTo(dst);
        System.out.println("📦(" + BASE_DIR + ") Mover " + src.getAbsolutePath() + " -> " + dst.getAbsolutePath() + " = " + ok);
        return ok;
    }
}
