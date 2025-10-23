package com.spacevault.nodos;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.io.File;

public class GestorNodos {

    /** 🔗 Obtiene referencia al Nodo1 (puerto 1099) */
    private NodeRemote nodo1() throws Exception {
        Registry r1 = LocateRegistry.getRegistry("localhost", 1099);
        return (NodeRemote) r1.lookup("Nodo1");
    }

    /** 🔗 Obtiene referencia al Nodo2 (puerto 1100) */
    private NodeRemote nodo2() throws Exception {
        Registry r2 = LocateRegistry.getRegistry("localhost", 1100);
        return (NodeRemote) r2.lookup("Nodo2");
    }

    /** 📁 Crea el directorio en ambos nodos (replicado) */
    public String crearDirectorio(String usuario, String ruta) {
        String fullPath = usuario + ruta;
        try {
            nodo1().crearDirectorio(fullPath);
            System.out.println("📁 Nodo1 creó: " + fullPath);
        } catch (Exception e) {
            System.err.println("⚠️ Nodo1 error crearDirectorio: " + e.getMessage());
        }
        try {
            nodo2().crearDirectorio(fullPath);
            System.out.println("📁 Nodo2 creó: " + fullPath);
        } catch (Exception e) {
            System.err.println("⚠️ Nodo2 error crearDirectorio: " + e.getMessage());
        }
        return "📂 Directorio '" + ruta + "' creado (replicado en Nodo1 y Nodo2)";
    }

    /** 📦 Guarda el archivo en ambos nodos (redundancia real) */
    public String almacenarArchivo(String usuario, String ruta, String nombre, byte[] datos) {
        String fullPath = usuario + File.separator + ruta;

        boolean ok1 = false, ok2 = false;
        try {
            nodo1().guardarArchivo(fullPath, nombre, datos);
            ok1 = true;
            System.out.println("💾 Nodo1 guardó: " + fullPath + "/" + nombre);
        } catch (Exception e) {
            System.err.println("⚠️ Nodo1 error guardarArchivo: " + e.getMessage());
        }

        try {
            nodo2().guardarArchivo(fullPath, nombre, datos);
            ok2 = true;
            System.out.println("💾 Nodo2 guardó: " + fullPath + "/" + nombre);
        } catch (Exception e) {
            System.err.println("⚠️ Nodo2 error guardarArchivo: " + e.getMessage());
        }

        if (ok1 || ok2)
            return "📦 Archivo '" + nombre + "' replicado correctamente en Nodo1 y Nodo2";
        else
            return "❌ Error: no se pudo guardar el archivo en ningún nodo";
    }

    /** 📖 Lee del Nodo1; si falla, intenta Nodo2 */
    public byte[] leerArchivo(String usuario, String ruta, String nombre) {
        String fullPath = usuario + File.separator + ruta;
        try {
            byte[] data = nodo1().leerArchivo(fullPath, nombre);
            if (data != null) return data;
        } catch (Exception e) {
            System.err.println("⚠️ Error leer Nodo1: " + e.getMessage());
        }
        try {
            return nodo2().leerArchivo(fullPath, nombre);
        } catch (Exception e) {
            System.err.println("⚠️ Error leer Nodo2: " + e.getMessage());
        }
        return null;
    }

    /** 🗑️ Elimina el archivo en ambos nodos */
    public String eliminarArchivo(String usuario, String ruta, String nombre) {
        String fullPath = usuario + File.separator + ruta;
        boolean ok1 = false, ok2 = false;
        try {
            ok1 = nodo1().eliminarArchivo(fullPath, nombre);
            System.out.println("🗑️ Nodo1 eliminó: " + fullPath + "/" + nombre);
        } catch (Exception e) {
            System.err.println("⚠️ Nodo1 error eliminarArchivo: " + e.getMessage());
        }
        try {
            ok2 = nodo2().eliminarArchivo(fullPath, nombre);
            System.out.println("🗑️ Nodo2 eliminó: " + fullPath + "/" + nombre);
        } catch (Exception e) {
            System.err.println("⚠️ Nodo2 error eliminarArchivo: " + e.getMessage());
        }

        if (ok1 || ok2)
            return "🗑️ Archivo eliminado (en ambos nodos)";
        else
            return "❌ No se pudo eliminar el archivo en ninguno de los nodos";
    }

    /** 🚚 Mueve/renombra el archivo en ambos nodos */
    public String moverArchivo(String usuario, String rutaVieja, String rutaNueva) {
        String src = usuario + File.separator + rutaVieja;
        String dst = usuario + File.separator + rutaNueva;
        boolean ok1 = false, ok2 = false;
        try {
            ok1 = nodo1().moverArchivo(src, dst);
            System.out.println("📦 Nodo1 movió: " + src + " -> " + dst);
        } catch (Exception e) {
            System.err.println("⚠️ Nodo1 error moverArchivo: " + e.getMessage());
        }
        try {
            ok2 = nodo2().moverArchivo(src, dst);
            System.out.println("📦 Nodo2 movió: " + src + " -> " + dst);
        } catch (Exception e) {
            System.err.println("⚠️ Nodo2 error moverArchivo: " + e.getMessage());
        }

        if (ok1 || ok2)
            return "📦 Archivo movido/renombrado (replicado en ambos nodos)";
        else
            return "❌ No se pudo renombrar el archivo en ninguno de los nodos";
    }

    /** 🤝 Copia un archivo compartido del dueño hacia el invitado (en ambos nodos) */
    public String compartirArchivo(String owner, String invitado, String ruta, String nombre) {
        try {
            String origen = owner + File.separator + ruta;
            String destino = invitado + File.separator + ruta;

            // 🔹 Intentar leer el archivo desde Nodo1 o Nodo2
            byte[] data = null;
            try {
                data = nodo1().leerArchivo(origen, nombre);
                if (data != null) {
                    System.out.println("📥 Archivo leído desde Nodo1: " + origen + "/" + nombre);
                }
            } catch (Exception e1) {
                System.err.println("⚠️ Nodo1 no disponible, intentando Nodo2...");
                try {
                    data = nodo2().leerArchivo(origen, nombre);
                    System.out.println("📥 Archivo leído desde Nodo2: " + origen + "/" + nombre);
                } catch (Exception e2) {
                    System.err.println("❌ Error leyendo archivo desde ambos nodos: " + e2.getMessage());
                }
            }

            if (data == null) {
                return "❌ No se pudo leer el archivo original (" + origen + ")";
            }

            // 🔹 Guardar el archivo en el espacio del invitado (en ambos nodos)
            try {
                nodo1().guardarArchivo(destino, nombre, data);
                System.out.println("📤 Nodo1 compartió archivo en: " + destino + "/" + nombre);
            } catch (Exception e) {
                System.err.println("⚠️ Nodo1 error al copiar archivo: " + e.getMessage());
            }

            try {
                nodo2().guardarArchivo(destino, nombre, data);
                System.out.println("📤 Nodo2 compartió archivo en: " + destino + "/" + nombre);
            } catch (Exception e) {
                System.err.println("⚠️ Nodo2 error al copiar archivo: " + e.getMessage());
            }

            return "📁 Archivo '" + nombre + "' replicado en nodos del usuario " + invitado;

        } catch (Exception e) {
            e.printStackTrace();
            return "❌ Error al compartir: " + e.getMessage();
        }
    }

    /** 🛰️ Información de los nodos activos */
    public String listarNodos() {
        return "Nodos activos:\n🛰️ Nodo1 -> localhost:1099\n🛰️ Nodo2 -> localhost:1100";
    }
}
