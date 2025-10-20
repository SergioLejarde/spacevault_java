package com.spacevault.servidor.db;

public class DBServerReplica {
    public static void main(String[] args) throws Exception {
        int port = 9100;
        String url = "jdbc:postgresql://localhost:5432/spacevaultjava_replica"; // ✅ Base diferente
        String user = "sergiolejarde";
        String pass = ""; // sin contraseña

        System.out.println("🧬 Iniciando servidor de réplica...");
        System.out.println("🔁 Este nodo actuará como réplica del DBServer principal (puerto 9090).");
        System.out.println("💾 Base de datos de réplica: spacevaultjava_replica");

        new DBServer(port, url, user, pass).start();
    }
}
