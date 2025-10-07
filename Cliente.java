import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Cliente {
    private ObjectOutputStream salida;
    private ObjectInputStream entrada;
    private Socket cliente;
    private String host;
    private Scanner scanner;

    public Cliente(String host) {
        this.host = host;
        this.scanner = new Scanner(System.in);
    }

    public void ejecutarCliente() {
        System.out.println("=== CLIENTE ===");
        
        try {
            conectarAlServidor();
            obtenerFlujos();
            procesarConexion();
        } catch (EOFException e) {
            System.out.println("El servidor terminó la conexión");
        } catch (IOException e) {
            System.out.println("Error de conexión: " + e.getMessage());
        } finally {
            cerrarConexion();
        }
    }

    private void conectarAlServidor() throws IOException {
        System.out.println("Intentando conectar al servidor " + host + ":11000...");
        cliente = new Socket(InetAddress.getByName(host), 11000);
        System.out.println("Conectado al servidor: " + cliente.getInetAddress().getHostName());
    }

    private void obtenerFlujos() throws IOException {
        salida = new ObjectOutputStream(cliente.getOutputStream());
        entrada = new ObjectInputStream(cliente.getInputStream());
        System.out.println("Flujos de comunicación establecidos");
    }

    private void procesarConexion() throws IOException {
        System.out.println("\nConexión establecida. Puede solicitar archivos.");
        
        try {
            // Leer mensaje de bienvenida del servidor
            String mensaje = (String) entrada.readObject();
            System.out.println("Servidor: " + mensaje);
            
            while (true) {
                System.out.println("\n--- OPCIONES ---");
                System.out.println("1. Listar archivos disponibles");
                System.out.println("2. Solicitar archivo");
                System.out.println("3. Salir");
                System.out.print("Seleccione opción: ");
                
                String opcion = scanner.nextLine().trim();
                
                if (opcion.equals("3")) {
                    enviarDatos("FIN");
                    break;
                } else if (opcion.equals("1")) {
                    listarArchivos();
                } else if (opcion.equals("2")) {
                    solicitarArchivo();
                } else {
                    System.out.println("Opción no válida");
                }
            }
        } catch (ClassNotFoundException e) {
            System.out.println("Error: Objeto desconocido recibido");
        }
    }


    private void listarArchivos() {
        try {
            System.out.println("Solicitando lista de archivos disponibles...");
            salida.writeObject("LISTAR_ARCHIVOS");
            

            Object respuesta = entrada.readObject();
            
            if (respuesta instanceof String) {
                String listaArchivos = (String) respuesta;
                if (listaArchivos.equals("SIN_ARCHIVOS")) {
                    System.out.println("El servidor no tiene archivos disponibles.");
                } else {
                    System.out.println("\n=== ARCHIVOS DISPONIBLES EN EL SERVIDOR ===");
                    System.out.println(listaArchivos);
                    System.out.println("=============================================");
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error al solicitar lista de archivos: " + e.getMessage());
        }
    }

    private void solicitarArchivo() {
        try {
            System.out.print("Ingrese el nombre del archivo a solicitar: ");
            String nombreArchivo = scanner.nextLine().trim();
            
            if (nombreArchivo.isEmpty()) {
                System.out.println("Nombre de archivo no válido");
                return;
            }
            

            salida.writeObject("SOLICITUD_ARCHIVO:" + nombreArchivo);
            System.out.println("Solicitando archivo: " + nombreArchivo);
            

            Object respuesta = entrada.readObject();
            
            if (respuesta instanceof String) {
                String mensaje = (String) respuesta;
                System.out.println("Servidor: " + mensaje);
                
                if (mensaje.contains("ARCHIVO_ENCONTRADO")) {
                    System.out.print("¿Desea descargar el archivo? (s/n): ");
                    String confirmacion = scanner.nextLine().trim().toLowerCase();
                    
                    if (confirmacion.equals("s") || confirmacion.equals("si")) {
                        iniciarDescarga(nombreArchivo);
                    } else {
                        System.out.println("Descarga cancelada");
                    }
                } else if (mensaje.contains("ARCHIVO_NO_ENCONTRADO")) {
                    System.out.println("El archivo no existe en el servidor");
                    System.out.println("Use la opción 1 para ver los archivos disponibles");
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error durante la solicitud: " + e.getMessage());
        }
    }

    private void iniciarDescarga(String nombreArchivo) {
        try {
            salida.writeObject("INICIAR_TRANSFERENCIA");
            System.out.println("Iniciando descarga...");
            

            Object objetoRecibido = entrada.readObject();
            
            if (objetoRecibido instanceof Archivo) {
                Archivo archivo = (Archivo) objetoRecibido;
                guardarArchivo(archivo);
            } else if (objetoRecibido instanceof String) {
                System.out.println("Servidor: " + objetoRecibido);
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error durante la descarga: " + e.getMessage());
        }
    }

    private void guardarArchivo(Archivo archivo) {
        try {
            File directorioDescargas = new File("descargas_cliente");
            if (!directorioDescargas.exists()) {
                directorioDescargas.mkdir();
            }
            

            File archivoDestino = new File(directorioDescargas, archivo.getNombre());
            FileOutputStream fos = new FileOutputStream(archivoDestino);
            fos.write(archivo.getDatos());
            fos.close();
            
            System.out.println("Archivo recibido y guardado: " + archivoDestino.getAbsolutePath());
            System.out.println("Tamaño: " + archivo.getDatos().length + " bytes");
            
        } catch (IOException e) {
            System.out.println("Error al guardar archivo: " + e.getMessage());
        }
    }

    private void enviarDatos(String mensaje) {
        try {
            salida.writeObject("CLIENTE:" + mensaje);
            System.out.println("Enviado: " + mensaje);
        } catch (IOException e) {
            System.out.println("Error al enviar mensaje: " + e.getMessage());
        }
    }

    private void cerrarConexion() {
        System.out.println("\nCerrando conexión...");
        try {
            if (salida != null) salida.close();
            if (entrada != null) entrada.close();
            if (cliente != null) cliente.close();
            scanner.close();
            System.out.println("Conexión cerrada exitosamente");
        } catch (IOException e) {
            System.out.println("Error al cerrar conexión: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        String host = "localhost";
        if (args.length > 0) {
            host = args[0];
        }
        
        Cliente cliente = new Cliente(host);
        cliente.ejecutarCliente();
    }
}