package com.spacevault.servidor;

import jakarta.jws.WebService;
import jakarta.jws.WebMethod;
import com.spacevault.servidor.nodos.GestorNodos;
import com.spacevault.servidor.db.DBClient;

@WebService(
    endpointInterface = "com.spacevault.servidor.ServicioArchivosInterface",
    serviceName = "ServicioArchivosService",
    portName = "ServicioArchivosPort",
    targetNamespace = "http://servidor.spacevault.com/"
)
public class ServicioArchivos implements ServicioArchivosInterface {

    private GestorNodos gestor = new GestorNodos();
    private DBClient db = new DBClient("localhost", 9090); // conexión TCP a DBServer

    // --- USUARIOS ---
    @WebMethod
    public String registrarUsuario(String usuario, String password) {
        boolean ok = db.registrarUsuario(usuario, password);
        return ok ? "✅ Usuario registrado en base de datos"
                  : "⚠️ Usuario ya existe o error de conexión";
    }

    @WebMethod
    public String loginUsuario(String usuario, String password) {
        boolean ok = db.login(usuario, password);
        return ok ? "✅ Bienvenido a SpaceVault, " + usuario + " 🚀"
                  : "❌ Usuario o contraseña incorrectos";
    }

    // --- DIRECTORIOS ---
    @WebMethod
    public String crearDirectorio(String usuario, String ruta) {
        String result = gestor.crearDirectorio(usuario, ruta);
        // Registrar en BD
        String padre = ruta.contains("/") ? ruta.substring(0, ruta.lastIndexOf("/")) : "/";
        String nombre = ruta.substring(ruta.lastIndexOf("/") + 1);
        db.send("MKDIR|" + usuario + "|" + padre + "|" + nombre);
        return result + " (📂 registrado en BD)";
    }

    // --- ARCHIVOS ---
    @WebMethod
    public String subirArchivo(String usuario, String ruta, String nombre, byte[] datos) {
        String result = gestor.almacenarArchivo(usuario, ruta, nombre, datos);
        // Guardar metadatos del archivo en BD (ruta, tamaño, nodo)
        long tamanio = datos.length;
        db.send("STORE|" + usuario + "|" + ruta + "|" + nombre + "|" + tamanio + "|Nodo1");
        return result + " (💾 metadatos guardados en BD)";
    }

    @WebMethod
    public byte[] leerArchivo(String usuario, String ruta, String nombre) {
        return gestor.leerArchivo(usuario, ruta, nombre);
    }

    @WebMethod
    public String eliminarArchivo(String usuario, String ruta, String nombre) {
        String result = gestor.eliminarArchivo(usuario, ruta, nombre);
        db.send("DELETE|" + usuario + "|" + ruta + "|" + nombre);
        return result + " (🗑️ eliminado también en BD)";
    }

    @WebMethod
    public String moverArchivo(String usuario, String rutaVieja, String rutaNueva) {
        String result = gestor.moverArchivo(usuario, rutaVieja, rutaNueva);
        db.send("MOVE|" + usuario + "|" + rutaVieja + "|" + rutaNueva);
        return result + " (📦 ruta actualizada en BD)";
    }

    // --- COMPARTIR ---
    @WebMethod
    public String compartirArchivo(String owner, String invitado, String ruta, String nombre) {
        boolean ok = db.compartir(owner, invitado, ruta, nombre);
        return ok ? "🤝 Archivo compartido con " + invitado
                  : "❌ No se pudo compartir archivo";
    }
}
