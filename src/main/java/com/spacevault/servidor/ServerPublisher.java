package com.spacevault.servidor;

import jakarta.xml.ws.Endpoint;

public class ServerPublisher {
    public static void main(String[] args) {
        String url = "http://localhost:8080/spacevault";
        System.out.println("🛰️ Publicando servicio SOAP en: " + url);
        Endpoint.publish(url, new ServicioArchivos());
        System.out.println("✅ Servicio SOAP activo y listo para recibir solicitudes.");
    }
}
