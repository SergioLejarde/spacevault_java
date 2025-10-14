package com.spacevault.nodos;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface NodeRemote extends Remote {

    void crearDirectorio(String ruta) throws RemoteException;

    void guardarArchivo(String ruta, String nombre, byte[] datos) throws RemoteException;

    byte[] leerArchivo(String ruta, String nombre) throws RemoteException;

    // 🗑️ Nuevo: eliminar archivo o carpeta
    void eliminarArchivo(String ruta, String nombre) throws RemoteException;

    // 📦 Nuevo: mover o renombrar archivo
    void moverArchivo(String rutaVieja, String rutaNueva) throws RemoteException;
}
