package com.spacevault.servidor;

import jakarta.jws.WebMethod;
import jakarta.jws.WebService;

// Interfaz del servicio con namespace y nombre definidos explícitamente
@WebService(
    name = "ServicioArchivos",
    targetNamespace = "http://servidor.spacevault.com/"
)
public interface ServicioArchivosInterface {
    @WebMethod String subirArchivo(String nombre, String contenido);
    @WebMethod String leerArchivo(String nombre);
    @WebMethod String listarNodos();
}
