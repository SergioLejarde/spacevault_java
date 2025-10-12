package com.spacevault.servidor;

import jakarta.jws.WebMethod;
import jakarta.jws.WebService;

@WebService(
    name = "ServicioArchivos",
    targetNamespace = "http://servidor.spacevault.com/"
)
public interface ServicioArchivosInterface {
    @WebMethod String registrarUsuario(String usuario, String password);
    @WebMethod String loginUsuario(String usuario, String password);
    @WebMethod String crearDirectorio(String usuario, String ruta);
    @WebMethod String subirArchivo(String usuario, String ruta, String nombre, byte[] datos);
    @WebMethod byte[] leerArchivo(String usuario, String ruta, String nombre);
}
