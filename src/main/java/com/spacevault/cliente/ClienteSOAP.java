package com.spacevault.cliente;

import javax.xml.namespace.QName;
import jakarta.xml.ws.Service;
import java.net.URL;
import java.util.*;
import javax.swing.*;
import java.io.*;
import com.spacevault.servidor.ServicioArchivosInterface;

public class ClienteSOAP {
    private static ServicioArchivosInterface servicio;
    private static String usuario = null;

    public static void main(String[] args) {
        try {
            URL url = new URL("http://localhost:8080/spacevault?wsdl");
            QName qname = new QName("http://servidor.spacevault.com/", "ServicioArchivosService");
            Service service = Service.create(url, qname);
            servicio = service.getPort(ServicioArchivosInterface.class);

            mostrarBanner();

            Scanner sc = new Scanner(System.in);
            int opcion;
            do {
                System.out.println("\n🌌 Menú principal de SpaceVault 🚀");
                System.out.println("1. Registrarse");
                System.out.println("2. Iniciar sesión");
                System.out.println("3. Crear directorio");
                System.out.println("4. Subir archivo");
                System.out.println("5. Leer archivo");
                System.out.println("6. Eliminar archivo/directorio");
                System.out.println("7. Mover o renombrar archivo/directorio");
                System.out.println("8. Compartir archivo con otro usuario");
                System.out.println("9. Salir");
                System.out.print("Selecciona una opción: ");
                opcion = Integer.parseInt(sc.nextLine());

                switch (opcion) {
                    case 1 -> registrar(sc);
                    case 2 -> login(sc);
                    case 3 -> crearDirectorio(sc);
                    case 4 -> subirArchivo();
                    case 5 -> leerArchivo(sc);
                    case 6 -> eliminar(sc);
                    case 7 -> mover(sc);
                    case 8 -> compartir(sc);
                    case 9 -> System.out.println("👋 Cerrando SpaceVault...");
                    default -> System.out.println("⚠️ Opción no válida.");
                }
            } while (opcion != 9);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void mostrarBanner() {
        System.out.println("=====================================================");
        System.out.println("🌌  SPACEVAULT CLOUD TERMINAL v1.0  🌌");
        System.out.println("Sistema distribuido de almacenamiento (SOAP + RMI + DB)");
        System.out.println("-----------------------------------------------------");
        System.out.println("👨‍🚀 Autor: Sergio A. Lejarde");
        System.out.println("🏫 Universidad Pontificia Bolivariana");
        System.out.println("=====================================================");
    }

    private static void registrar(Scanner sc) {
        System.out.print("Usuario: ");
        String u = sc.nextLine();
        System.out.print("Contraseña: ");
        String p = sc.nextLine();
        System.out.println(servicio.registrarUsuario(u, p));
    }

    private static void login(Scanner sc) {
        System.out.print("Usuario: ");
        String u = sc.nextLine();
        System.out.print("Contraseña: ");
        String p = sc.nextLine();
        String r = servicio.loginUsuario(u, p);
        System.out.println(r);
        if (r.startsWith("✅")) usuario = u;
    }

    private static void crearDirectorio(Scanner sc) {
        if (!verificarSesion()) return;
        System.out.print("Ruta (ej: /docs/trabajos): ");
        String ruta = sc.nextLine();
        System.out.println(servicio.crearDirectorio(usuario, ruta));
    }

    private static void subirArchivo() {
        if (!verificarSesion()) return;
        try {
            JFileChooser fc = new JFileChooser();
            fc.setDialogTitle("Selecciona un archivo para subir");
            if (fc.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) return;
            File f = fc.getSelectedFile();

            byte[] datos;
            try (FileInputStream in = new FileInputStream(f)) {
                datos = in.readAllBytes();
            }

            String ruta = JOptionPane.showInputDialog("Ruta destino (ej: /docs):");
            if (ruta == null || ruta.isBlank()) ruta = "/";
            String res = servicio.subirArchivo(usuario, ruta, f.getName(), datos);
            System.out.println(res);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void leerArchivo(Scanner sc) {
        if (!verificarSesion()) return;
        try {
            System.out.print("Ruta (ej: /docs): ");
            String ruta = sc.nextLine();
            System.out.print("Nombre del archivo: ");
            String nombre = sc.nextLine();
            byte[] data = servicio.leerArchivo(usuario, ruta, nombre);
            if (data == null) {
                System.out.println("❌ Archivo no encontrado en el nodo.");
                return;
            }
            File out = new File(System.getProperty("user.home") + "/Downloads/" + nombre);
            try (FileOutputStream fos = new FileOutputStream(out)) {
                fos.write(data);
            }
            System.out.println("✅ Archivo descargado en: " + out.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 🗑️ Eliminar archivo o directorio
    private static void eliminar(Scanner sc) {
        if (!verificarSesion()) return;
        try {
            System.out.print("Ruta (ej: /docs): ");
            String ruta = sc.nextLine();
            System.out.print("Nombre del archivo/directorio: ");
            String nombre = sc.nextLine();
            String res = servicio.eliminarArchivo(usuario, ruta, nombre);
            System.out.println(res);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 📦 Mover o renombrar archivo o directorio
    private static void mover(Scanner sc) {
        if (!verificarSesion()) return;
        try {
            System.out.print("Ruta original (ej: /docs/antiguo.txt): ");
            String origen = sc.nextLine();
            System.out.print("Ruta nueva (ej: /docs/nuevo.txt): ");
            String destino = sc.nextLine();
            String res = servicio.moverArchivo(usuario, origen, destino);
            System.out.println(res);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 🤝 Compartir archivo con otro usuario
    private static void compartir(Scanner sc) {
        if (!verificarSesion()) return;
        try {
            System.out.print("Usuario con quien compartir: ");
            String invitado = sc.nextLine();
            System.out.print("Ruta del archivo (ej: /docs): ");
            String ruta = sc.nextLine();
            System.out.print("Nombre del archivo: ");
            String nombre = sc.nextLine();
            String res = servicio.compartirArchivo(usuario, invitado, ruta, nombre);
            System.out.println(res);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static boolean verificarSesion() {
        if (usuario == null) {
            System.out.println("⚠️ Inicia sesión primero.");
            return false;
        }
        return true;
    }
}
