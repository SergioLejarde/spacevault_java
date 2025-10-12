package com.spacevault.nodos;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ConcurrentHashMap;

public class NodeRemoteImpl extends UnicastRemoteObject implements NodeRemote {

    private ConcurrentHashMap<String, String> archivos = new ConcurrentHashMap<>();

    public NodeRemoteImpl() throws RemoteException {
        super();
    }

    @Override
    public void guardar(String nombre, String contenido) throws RemoteException {
        archivos.put(nombre, contenido);
        System.out.println("💾 Archivo guardado en nodo: " + nombre);
    }

    @Override
    public String leer(String nombre) throws RemoteException {
        return archivos.getOrDefault(nombre, "Archivo no encontrado ❌");
    }
}
