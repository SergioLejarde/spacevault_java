package com.spacevault.servidor;

import jakarta.jws.WebMethod;
import jakarta.jws.WebService;

@WebService(
    name = "ServicioArchivos",
    targetNamespace = "http://servidor.spacevault.com/"
)
public interface ServicioArchivosInterface {

    // 🔐 Autenticación
    @WebMethod String registrarUsuario(String usuario, String password);
    @WebMethod String loginUsuario(String usuario, String password);

    // 📂 Gestión básica de archivos
    @WebMethod String crearDirectorio(String usuario, String ruta);
    @WebMethod String subirArchivo(String usuario, String ruta, String nombre, byte[] datos);
    @WebMethod byte[] leerArchivo(String usuario, String ruta, String nombre);

    // 🗑️ Funciones avanzadas (Avance 4)
    @WebMethod String eliminarArchivo(String usuario, String ruta, String nombre);
    @WebMethod String moverArchivo(String usuario, String origen, String destino);
    @WebMethod String compartirArchivo(String owner, String invitado, String ruta, String nombre);
}
