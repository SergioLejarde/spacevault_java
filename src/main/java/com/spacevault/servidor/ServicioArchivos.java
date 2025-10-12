package com.spacevault.servidor;

import jakarta.jws.WebService;
import jakarta.jws.WebMethod;
import com.spacevault.servidor.nodos.GestorNodos;
import java.util.concurrent.ConcurrentHashMap;

@WebService(
    endpointInterface = "com.spacevault.servidor.ServicioArchivosInterface",
    serviceName = "ServicioArchivosService",
    portName = "ServicioArchivosPort",
    targetNamespace = "http://servidor.spacevault.com/"
)
public class ServicioArchivos implements ServicioArchivosInterface {

    private ConcurrentHashMap<String, String> usuarios = new ConcurrentHashMap<>();
    private GestorNodos gestor = new GestorNodos();

    @WebMethod
    public String registrarUsuario(String usuario, String password) {
        if (usuarios.containsKey(usuario)) return "❌ Usuario ya existe";
        usuarios.put(usuario, password);
        return "✅ Usuario registrado con éxito";
    }

    @WebMethod
    public String loginUsuario(String usuario, String password) {
        if (!usuarios.containsKey(usuario)) return "❌ Usuario no encontrado";
        if (!usuarios.get(usuario).equals(password)) return "❌ Contraseña incorrecta";
        return "✅ Bienvenido a SpaceVault, " + usuario + " 🚀";
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
}
