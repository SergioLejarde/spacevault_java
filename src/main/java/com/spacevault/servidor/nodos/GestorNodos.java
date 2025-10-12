package com.spacevault.servidor.nodos;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import com.spacevault.nodos.NodeRemote;

public class GestorNodos {

    public String almacenarArchivo(String nombre, String contenido) {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            NodeRemote nodo = (NodeRemote) registry.lookup("Nodo1");
            nodo.guardar(nombre, contenido);
            return "Archivo enviado al nodo correctamente ✅";
        } catch (Exception e) {
            e.printStackTrace();
            return "❌ Error al enviar al nodo: " + e.getMessage();
        }
    }

    public String leerArchivo(String nombre) {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            NodeRemote nodo = (NodeRemote) registry.lookup("Nodo1");
            return nodo.leer(nombre);
        } catch (Exception e) {
            e.printStackTrace();
            return "❌ Error al leer del nodo: " + e.getMessage();
        }
    }

    public String listarNodos() {
        return "Nodos activos: Nodo1 (localhost:1099)";
    }
}
