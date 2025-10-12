package com.spacevault.nodos;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class NodeServer {
    public static void main(String[] args) {
        try {
            int port = 1099; // puerto por defecto
            if (args.length > 0) {
                port = Integer.parseInt(args[0]);
            }

            // Crear o usar el registro RMI existente
            try {
                LocateRegistry.createRegistry(port);
                System.out.println("📡 Registro RMI creado en puerto " + port);
            } catch (Exception e) {
                System.out.println("⚠️ Registro RMI ya existe, usando el existente en puerto " + port);
            }

            // Crear e iniciar el nodo
            NodeRemoteImpl nodo = new NodeRemoteImpl();
            Registry registry = LocateRegistry.getRegistry("localhost", port);
            registry.rebind("Nodo1", nodo);

            System.out.println("🛰️ Nodo RMI activo en puerto " + port + " y listo para recibir archivos");

            // Mantener el proceso activo indefinidamente
            synchronized (NodeServer.class) {
                NodeServer.class.wait();
            }

        } catch (Exception e) {
            System.err.println("❌ Error al iniciar el nodo RMI:");
            e.printStackTrace();
        }
    }
}
