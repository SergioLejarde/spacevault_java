package com.spacevault.servidor;

import jakarta.jws.WebService;
import jakarta.jws.WebMethod;
import com.spacevault.servidor.nodos.GestorNodos;

// Implementación del servicio, debe coincidir con la interfaz
@WebService(
    endpointInterface = "com.spacevault.servidor.ServicioArchivosInterface",
    serviceName = "ServicioArchivosService",
    portName = "ServicioArchivosPort",
    targetNamespace = "http://servidor.spacevault.com/"
)
public class ServicioArchivos implements ServicioArchivosInterface {

    private GestorNodos gestor = new GestorNodos();

    @WebMethod
    public String subirArchivo(String nombre, String contenido) {
        System.out.println("📦 Recibido archivo: " + nombre);
        return gestor.almacenarArchivo(nombre, contenido);
    }

    @WebMethod
    public String leerArchivo(String nombre) {
        System.out.println("📖 Leyendo archivo: " + nombre);
        return gestor.leerArchivo(nombre);
    }

    @WebMethod
    public String listarNodos() {
        return gestor.listarNodos();
    }
}
