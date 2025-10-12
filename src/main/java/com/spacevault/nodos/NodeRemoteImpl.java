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
}
