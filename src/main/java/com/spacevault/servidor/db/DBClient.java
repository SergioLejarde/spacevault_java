package com.spacevault.servidor.db;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class DBClient {
    private final String host;
    private final int port;

    public DBClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    /** Envía una línea de comando al DBServer y retorna la respuesta textual. */
    private String send(String line) {
        try (Socket s = new Socket(host, port);
             BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream(), StandardCharsets.UTF_8));
             BufferedWriter out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream(), StandardCharsets.UTF_8))) {
            out.write(line);
            out.write("\n");
            out.flush();
            return in.readLine(); // respuesta de una línea
        } catch (Exception e) {
            return "ERR:" + e.getMessage();
        }
    }

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
