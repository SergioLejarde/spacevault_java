package com.spacevault.servidor.db;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class DBClient {
    private final String host;
    private final int mainPort;
    private final int replicaPort;

    public DBClient(String host, int mainPort) {
        this(host, mainPort, mainPort + 1000); // 🔹 por defecto réplica en +1000 (9100)
    }

    public DBClient(String host, int mainPort, int replicaPort) {
        this.host = host;
        this.mainPort = mainPort;
        this.replicaPort = replicaPort;
    }

    /** Envía una línea de comando a un servidor específico y muestra errores claros. */
    private String sendToPort(String line, int port) {
        System.out.println("📤 Enviando a puerto " + port + ": " + line);

        try (Socket s = new Socket(host, port);
             BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream(), StandardCharsets.UTF_8));
             BufferedWriter out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream(), StandardCharsets.UTF_8))) {

            out.write(line);
            out.write("\n");
            out.flush();

            String response = in.readLine();
            System.out.println("📥 Respuesta desde puerto " + port + ": " + response);
            return response;

        } catch (Exception e) {
            System.err.println("⚠️ Error enviando al puerto " + port + ": " + e.getMessage());
            return "ERR:" + e.getMessage();
        }
    }

    /** 🔹 Envía el comando a ambos servidores (principal y réplica) */
    public String send(String line) {
        String res1 = sendToPort(line, mainPort);
        String res2 = sendToPort(line, replicaPort);

        if ("OK".equals(res1) || "OK".equals(res2)) {
            return "OK";
        } else if (res1.startsWith("ERR") && res2.startsWith("ERR")) {
            System.err.println("❌ Error: ambos DBServers fallaron: " + res1 + " | " + res2);
        }
        return res1;
    }

    /** 🔹 Método para que el servidor SOAP envíe comandos directos */
    public void sendCommand(String cmd) {
        String res = send(cmd);
        if (!"OK".equals(res)) {
            System.err.println("⚠️ DBServer devolvió: " + res);
        }
    }

    // ----- Operaciones específicas -----

    public boolean registrarUsuario(String user, String pass) {
        String r = send("REGISTER|" + user + "|" + pass);
        return "OK".equals(r);
    }

    public boolean login(String user, String pass) {
        String r = send("LOGIN|" + user + "|" + pass);
        return "OK".equals(r);
    }

    public boolean compartir(String owner, String invitado, String ruta, String nombre) {
        String r = send("SHARE|" + owner + "|" + invitado + "|" + ruta + "|" + nombre);
        return "OK".equals(r);
    }
}
