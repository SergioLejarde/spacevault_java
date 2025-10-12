package com.spacevault.cliente;

import javax.xml.namespace.QName;
import jakarta.xml.ws.Service;
import java.net.URL;
import com.spacevault.servidor.ServicioArchivosInterface;

public class ClienteSOAP {
    public static void main(String[] args) {
        try {
            // URL donde el servidor SOAP publicó el servicio
            URL url = new URL("http://localhost:8080/spacevault?wsdl");

            // Debe coincidir EXACTAMENTE con targetNamespace y serviceName del servidor
            QName qname = new QName("http://servidor.spacevault.com/", "ServicioArchivosService");

            // Crear servicio y obtener el puerto
            Service service = Service.create(url, qname);
            ServicioArchivosInterface servicio = service.getPort(ServicioArchivosInterface.class);

            System.out.println("🚀 Subiendo archivo...");
            System.out.println(servicio.subirArchivo("prueba.txt", "Contenido de ejemplo"));

            System.out.println("📖 Leyendo archivo...");
            System.out.println(servicio.leerArchivo("prueba.txt"));

            System.out.println("🛰️ Nodos activos:");
            System.out.println(servicio.listarNodos());

        } catch (Exception e) {
            System.err.println("❌ Error ejecutando ClienteSOAP:");
            e.printStackTrace();
        }
    }
}
