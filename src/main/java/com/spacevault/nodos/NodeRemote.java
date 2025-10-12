package com.spacevault.nodos;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface NodeRemote extends Remote {
    void guardar(String nombre, String contenido) throws RemoteException;
    String leer(String nombre) throws RemoteException;
}
