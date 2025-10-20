package com.spacevault.nodos;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class NodeServer2 {
    public static void main(String[] args) {
        try {
            System.setProperty("java.rmi.server.hostname", "localhost");

            int port = 1100;
            Registry registry = LocateRegistry.createRegistry(port);
            NodeRemoteImpl impl = new NodeRemoteImpl("data-Node2");
            registry.rebind("Nodo2", impl);

            System.out.println("🛰️ Nodo RMI **Nodo2** activo en puerto " + port + " (base: data-Node2)");
            System.out.println("⏳ Esperando peticiones RMI en Nodo2...");
            Thread.sleep(Long.MAX_VALUE);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
