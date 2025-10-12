package com.spacevault.servidor.nodos;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import com.spacevault.nodos.NodeRemote;
import java.io.File;


public class GestorNodos {

    // Crea el directorio físico en disco en el nodo remoto
    public String crearDirectorio(String usuario, String ruta) {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            NodeRemote nodo = (NodeRemote) registry.lookup("Nodo1");
            String fullPath = usuario + ruta;
            nodo.crearDirectorio(fullPath);
            return "📁 Directorio '" + ruta + "' creado correctamente para " + usuario;
        } catch (Exception e) {
            e.printStackTrace();
            return "❌ Error al crear directorio: " + e.getMessage();
        }
    }

    // Guarda el archivo (en bytes) en el nodo remoto
    public String almacenarArchivo(String usuario, String ruta, String nombre, byte[] datos) {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            NodeRemote nodo = (NodeRemote) registry.lookup("Nodo1");
            String fullPath = usuario + File.separator + ruta;
            nodo.guardarArchivo(fullPath, nombre, datos);
            return "📦 Archivo '" + nombre + "' almacenado correctamente en " + fullPath;
        } catch (Exception e) {
            e.printStackTrace();
            return "❌ Error al enviar al nodo: " + e.getMessage();
        }
    }

    // Lee el archivo del nodo remoto
    public byte[] leerArchivo(String usuario, String ruta, String nombre) {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            NodeRemote nodo = (NodeRemote) registry.lookup("Nodo1");
            String fullPath = usuario + File.separator + ruta;
            return nodo.leerArchivo(fullPath, nombre);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Listado simbólico de nodos activos
    public String listarNodos() {
        return "Nodos activos: Nodo1 (localhost:1099)";
    }
}
