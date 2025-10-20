package com.spacevault.nodos;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface NodeRemote extends Remote {

    // 📁 Crear directorio
    void crearDirectorio(String ruta) throws RemoteException;

    // 💾 Guardar archivo en bytes
    void guardarArchivo(String ruta, String nombre, byte[] datos) throws RemoteException;

    // 📖 Leer archivo
    byte[] leerArchivo(String ruta, String nombre) throws RemoteException;

    // 🗑️ Eliminar archivo → devuelve true si se eliminó
    boolean eliminarArchivo(String ruta, String nombre) throws RemoteException;

    // 🚚 Mover/Renombrar archivo → devuelve true si se movió correctamente
    boolean moverArchivo(String origen, String destino) throws RemoteException;
}
