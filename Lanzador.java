// Lanzador.java
import javax.swing.SwingUtilities;
import java.io.IOException;
import java.util.Scanner;
import java.util.List;

public class Lanzador {

    public static void main(String[] args) {
        // Revisa si el primer argumento es "--console" o "-c"
        if (args.length > 0 && (args[0].equalsIgnoreCase("--console") || args[0].equalsIgnoreCase("-c"))) {
            System.out.println("Iniciando cliente en modo consola...");
            ejecutarModoConsola();
        } else {
            System.out.println("Iniciando cliente en modo GUI...");
            // Lanza la GUI en el hilo de eventos de Swing para seguridad
            SwingUtilities.invokeLater(() -> new ClienteGUI().setVisible(true));
        }
    }
    
    // Esta es la lógica original de tu cliente de consola, adaptada.
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