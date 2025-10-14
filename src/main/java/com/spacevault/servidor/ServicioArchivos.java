package com.spacevault.servidor;

import jakarta.jws.WebService;
import jakarta.jws.WebMethod;
import com.spacevault.servidor.nodos.GestorNodos;
import com.spacevault.servidor.db.DBClient;  // 🔹 nuevo import

@WebService(
    endpointInterface = "com.spacevault.servidor.ServicioArchivosInterface",
    serviceName = "ServicioArchivosService",
    portName = "ServicioArchivosPort",
    targetNamespace = "http://servidor.spacevault.com/"
)
public class ServicioArchivos implements ServicioArchivosInterface {

    private GestorNodos gestor = new GestorNodos();
    private DBClient db = new DBClient("localhost", 9090); // 🔹 conexión TCP al DBServer

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

    @WebMethod
    public String crearDirectorio(String usuario, String ruta) {
        return gestor.crearDirectorio(usuario, ruta);
    }

    @WebMethod
    public String subirArchivo(String usuario, String ruta, String nombre, byte[] datos) {
        return gestor.almacenarArchivo(usuario, ruta, nombre, datos);
    }

    @WebMethod
    public byte[] leerArchivo(String usuario, String ruta, String nombre) {
        return gestor.leerArchivo(usuario, ruta, nombre);
    }

    @WebMethod
    public String compartirArchivo(String owner, String invitado, String ruta, String nombre) {
        boolean ok = db.compartir(owner, invitado, ruta, nombre);
        return ok ? "🤝 Archivo compartido con " + invitado : "❌ No se pudo compartir archivo";
    }
}
