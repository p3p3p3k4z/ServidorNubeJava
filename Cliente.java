// Cliente.java
import javax.swing.SwingUtilities;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class Cliente {
    private Socket clienteSocket;
    private ObjectOutputStream salida;
    private ObjectInputStream entrada;

    // --- MÉTODOS DEL MOTOR DE COMUNICACIÓN ---
    
    public boolean conectar(String host) {
        try {
            clienteSocket = new Socket(host, 11000);
            salida = new ObjectOutputStream(clienteSocket.getOutputStream());
            salida.flush();
            entrada = new ObjectInputStream(clienteSocket.getInputStream());
            entrada.readObject(); // Lee mensaje de bienvenida
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public List<String> listarArchivos() throws IOException, ClassNotFoundException {
        enviarSolicitud("LISTAR_ARCHIVOS");
        String respuesta = (String) entrada.readObject();
        if (respuesta.startsWith("--- Archivos Disponibles ---")) {
            List<String> archivos = new ArrayList<>();
            String[] lineas = respuesta.split("\n");
            for (int i = 1; i < lineas.length; i++) {
                archivos.add(lineas[i].substring(2));
            }
            return archivos;
        }
        return Collections.emptyList();
    }

    public boolean descargarArchivo(String nombreArchivo) throws IOException, ClassNotFoundException {
        enviarSolicitud("SOLICITAR_ARCHIVO:" + nombreArchivo);
        Object respuesta = entrada.readObject();

        if (respuesta instanceof String && ((String) respuesta).equals("ARCHIVO_ENCONTRADO")) {
            Archivo archivoRecibido = (Archivo) entrada.readObject();
            guardarArchivo(archivoRecibido);
            return true;
        }
        return false;
    }
    
    public void desconectar() {
        try {
            if (clienteSocket != null && !clienteSocket.isClosed()) {
                enviarSolicitud("SALIR");
            }
        } catch (IOException e) {
            // Ignorar
        } finally {
            try {
                if (salida != null) salida.close();
                if (entrada != null) entrada.close();
                if (clienteSocket != null) clienteSocket.close();
            } catch (IOException e) {
                // Ignorar
            }
        }
    }
    
    private void guardarArchivo(Archivo archivo) throws IOException {
        File descargasDir = new File("descargas_cliente");
        if (!descargasDir.exists()) descargasDir.mkdir();
        File archivoDestino = new File(descargasDir, archivo.getNombre());
        try (FileOutputStream fos = new FileOutputStream(archivoDestino)) {
            fos.write(archivo.getDatos());
        }
    }

    private void enviarSolicitud(String solicitud) throws IOException {
        salida.writeObject(solicitud);
        salida.flush();
    }
    
    // --- PUNTO DE ENTRADA PRINCIPAL ---
    
    public static void main(String[] args) {
        // Por defecto, se ejecuta la GUI.
        // Si se pasa cualquier argumento (como --console o -c), se ejecuta en modo consola.
        if (args.length > 0) {
            System.out.println("Iniciando cliente en modo consola...");
            ejecutarModoConsola();
        } else {
            System.out.println("Iniciando cliente en modo GUI...");
            SwingUtilities.invokeLater(() -> new ClienteGUI().setVisible(true));
        }
    }
    
    // --- LÓGICA DEL MODO CONSOLA ---
    
    public static void ejecutarModoConsola() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Ingrese el host del servidor (ej. localhost): ");
        String host = scanner.nextLine();
        
        Cliente cliente = new Cliente();
        
        if (!cliente.conectar(host)) {
            System.err.println("No se pudo conectar al servidor. Terminando.");
            return;
        }
        
        System.out.println("¡Conexión exitosa!");

        try {
            while (true) {
                System.out.println("\n--- MENÚ DE CONSOLA ---");
                System.out.println("1. Listar archivos");
                System.out.println("2. Descargar archivo");
                System.out.println("3. Salir");
                System.out.print("Opción: ");
                String opcion = scanner.nextLine();

                if ("1".equals(opcion)) {
                    System.out.println("Obteniendo lista de archivos...");
                    List<String> archivos = cliente.listarArchivos();
                    if (archivos.isEmpty()) {
                        System.out.println("No hay archivos en el servidor.");
                    } else {
                        System.out.println("--- Archivos Disponibles ---");
                        archivos.forEach(System.out::println);
                    }
                } else if ("2".equals(opcion)) {
                    System.out.print("Nombre del archivo a descargar: ");
                    String nombreArchivo = scanner.nextLine();
                    if (cliente.descargarArchivo(nombreArchivo)) {
                        System.out.println("¡Archivo descargado con éxito en 'descargas_cliente'!");
                    } else {
                        System.out.println("Error: No se pudo descargar el archivo.");
                    }
                } else if ("3".equals(opcion)) {
                    break;
                } else {
                    System.out.println("Opción no válida.");
                }
            }
        } catch (Exception e) {
            System.err.println("Error durante la comunicación: " + e.getMessage());
        } finally {
            cliente.desconectar();
            System.out.println("Conexión cerrada. Adiós.");
            scanner.close();
        }
    }
}