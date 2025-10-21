package com.spacevault.servidor;

import jakarta.jws.WebService;
import jakarta.jws.WebMethod;

import com.spacevault.nodos.GestorNodos;
import com.spacevault.servidor.db.DBClient;

@WebService(
    endpointInterface = "com.spacevault.servidor.ServicioArchivosInterface",
    serviceName = "ServicioArchivosService",
    portName = "ServicioArchivosPort",
    targetNamespace = "http://servidor.spacevault.com/"
)
public class ServicioArchivos implements ServicioArchivosInterface {

    private final GestorNodos gestor = new GestorNodos();
    private final DBClient db = new DBClient("localhost", 9090, 9100);


    // 🔐 Registro y autenticación
    @WebMethod
    public String registrarUsuario(String usuario, String password) {
        boolean ok = db.registrarUsuario(usuario, password);
        return ok ? "✅ Usuario registrado en base de datos" : "⚠️ Usuario ya existe o error de conexión";
    }

    @WebMethod
    public String loginUsuario(String usuario, String password) {
        boolean ok = db.login(usuario, password);
        return ok ? "✅ Bienvenido a SpaceVault, " + usuario + " 🚀" : "❌ Usuario o contraseña incorrectos";
    }

    // 📂 Operaciones de archivos
    @WebMethod
    public String crearDirectorio(String usuario, String ruta) {
        String res = gestor.crearDirectorio(usuario, ruta);
        db.sendCommand("MKDIR|" + usuario + "|" + ruta + "|-");
        return res;
    }

    @WebMethod
    public String subirArchivo(String usuario, String ruta, String nombre, byte[] datos) {
        String res = gestor.almacenarArchivo(usuario, ruta, nombre, datos);
        db.sendCommand("STORE|" + usuario + "|" + ruta + "|" + nombre + "|" + datos.length + "|Nodo1");
        return res;
    }

    @WebMethod
    public byte[] leerArchivo(String usuario, String ruta, String nombre) {
        return gestor.leerArchivo(usuario, ruta, nombre);
    }

    // 🗑️ Eliminar archivo o directorio
    @WebMethod
    public String eliminarArchivo(String usuario, String ruta, String nombre) {
        String res = gestor.eliminarArchivo(usuario, ruta, nombre);
        db.sendCommand("DELETE|" + usuario + "|" + ruta + "|" + nombre);
        return res;
    }

    // 📦 Mover o renombrar archivo/directorio
    @WebMethod
    public String moverArchivo(String usuario, String origen, String destino) {
        String res = gestor.moverArchivo(usuario, origen, destino);
        db.sendCommand("MOVE|" + usuario + "|" + origen + "|" + destino);
        return res;
    }

    // 🤝 Compartir archivo con otro usuario
    @WebMethod
    public String compartirArchivo(String owner, String invitado, String ruta, String nombre) {
        boolean ok = db.compartir(owner, invitado, ruta, nombre);
        return ok ? "🤝 Archivo compartido con " + invitado : "❌ No se pudo compartir archivo";
    }
}
