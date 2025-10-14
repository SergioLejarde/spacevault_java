package com.spacevault.servidor.db;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.concurrent.*;

public class DBServer {

    private final int port;
    private final String jdbcUrl;
    private final String user;
    private final String pass;

    private Connection conn;

    public DBServer(int port, String jdbcUrl, String user, String pass) {
        this.port = port;
        this.jdbcUrl = jdbcUrl;
        this.user = user;
        this.pass = pass;
    }

    public void start() throws Exception {
        conn = DriverManager.getConnection(jdbcUrl, user, pass);
        System.out.println("✅ Conectado a PostgreSQL en " + jdbcUrl);
        initSchema();

        ServerSocket server = new ServerSocket(port);
        System.out.println("🗄️ DBServer escuchando en TCP " + port + " (protocolo texto)");

        ExecutorService pool = Executors.newFixedThreadPool(8);
        while (true) {
            Socket client = server.accept();
            pool.submit(() -> handle(client));
        }
    }

    private void initSchema() throws SQLException {
        try (Statement st = conn.createStatement()) {
            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS usuarios (
                  id SERIAL PRIMARY KEY,
                  usuario VARCHAR(50) UNIQUE NOT NULL,
                  password VARCHAR(100) NOT NULL
                );
            """);
            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS compartidos (
                  id SERIAL PRIMARY KEY,
                  owner   VARCHAR(50) NOT NULL,
                  invitado VARCHAR(50) NOT NULL,
                  ruta    VARCHAR(255) NOT NULL,
                  nombre  VARCHAR(255) NOT NULL
                );
            """);
        }
    }

    private void handle(Socket client) {
        try (client;
             BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream(), StandardCharsets.UTF_8));
             BufferedWriter out = new BufferedWriter(new OutputStreamWriter(client.getOutputStream(), StandardCharsets.UTF_8))) {

            String line = in.readLine();
            if (line == null) return;

            String res = process(line.trim());
            out.write(res);
            out.write("\n");
            out.flush();
        } catch (Exception e) {
            // Log mínimo
            e.printStackTrace();
        }
    }

    private String process(String cmd) {
        try {
            String[] p = cmd.split("\\|");
            switch (p[0]) {
                case "REGISTER":  // REGISTER|user|pass
                    return register(p[1], p[2]) ? "OK" : "EXISTS";
                case "LOGIN":     // LOGIN|user|pass
                    return login(p[1], p[2]) ? "OK" : "FAIL";
                case "SHARE":     // SHARE|owner|invitado|ruta|nombre
                    return share(p[1], p[2], p[3], p[4]) ? "OK" : "FAIL";
                default:
                    return "ERR:CMD";
            }
        } catch (Exception e) {
            return "ERR:" + e.getMessage();
        }
    }

    private boolean register(String usuario, String password) throws SQLException {
        String sql = "INSERT INTO usuarios (usuario, password) VALUES (?, ?) ON CONFLICT (usuario) DO NOTHING";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, usuario);
            ps.setString(2, password);
            return ps.executeUpdate() > 0;
        }
    }

    private boolean login(String usuario, String password) throws SQLException {
        String sql = "SELECT 1 FROM usuarios WHERE usuario=? AND password=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, usuario);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private boolean share(String owner, String invitado, String ruta, String nombre) throws SQLException {
        String sql = "INSERT INTO compartidos (owner, invitado, ruta, nombre) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, owner);
            ps.setString(2, invitado);
            ps.setString(3, ruta);
            ps.setString(4, nombre);
            return ps.executeUpdate() > 0;
        }
    }

    public static void main(String[] args) throws Exception {
        // Args opcionales: puerto, jdbcUrl, user, pass
        int port      = args.length > 0 ? Integer.parseInt(args[0]) : 9090;
        String url    = args.length > 1 ? args[1] : "jdbc:postgresql://localhost:5432/spacevault";
        String user   = args.length > 2 ? args[2] : "postgres";
        String pass   = args.length > 3 ? args[3] : "postgres";

        new DBServer(port, url, user, pass).start();
    }
}
