package com.spacevault.nodos;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface NodeRemote extends Remote {
    void crearDirectorio(String ruta) throws RemoteException;
    void guardarArchivo(String ruta, String nombre, byte[] datos) throws RemoteException;
    byte[] leerArchivo(String ruta, String nombre) throws RemoteException;
}
