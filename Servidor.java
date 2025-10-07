import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Servidor {
    private ObjectOutputStream salida;
    private ObjectInputStream entrada;
    private ServerSocket servidor;
    private Socket cliente;
    private File directorioArchivos;
    private Scanner scanner;
    private String archivoSolicitado;
    private boolean ejecutando = true;

    public Servidor() {
        this.scanner = new Scanner(System.in);
        this.directorioArchivos = new File("archivos_servidor");
        if (!directorioArchivos.exists()) {
            directorioArchivos.mkdir();
        }
    }

    public void ejecutarServidor() {
        System.out.println("=== SERVIDOR ===");
        System.out.println("Directorio de archivos: " + directorioArchivos.getAbsolutePath());
        
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n\nRecibida señal de terminación (Ctrl+C). Cerrando servidor...");
            ejecutando = false;
            cerrarServidor();
        }));
        
        try {
            servidor = new ServerSocket(11000);
            System.out.println("Servidor iniciado en puerto 11000");
            System.out.println("Esperando conexiones...");
            System.out.println("Presione Ctrl+C para terminar el servidor\n");
            
            while (ejecutando) {
                try {
                    esperarConexion();
                    obtenerFlujos();
                    procesarConexion();
                } catch (EOFException e) {
                    System.out.println("El cliente terminó la conexión");
                } catch (SocketException e) {
                    if (ejecutando) {
                        System.out.println("Error de socket: " + e.getMessage());
                    }
                } catch (IOException e) {
                    if (ejecutando) {
                        System.out.println("Error de E/S: " + e.getMessage());
                    }
                } finally {
                    cerrarConexion();
                }
            }
        } catch (IOException e) {
            System.out.println("Error al iniciar servidor: " + e.getMessage());
        } finally {
            scanner.close();
            System.out.println("Servidor terminado.");
        }
    }

    private void esperarConexion() throws IOException {
        System.out.println("\n--- Esperando conexión de cliente ---");
        cliente = servidor.accept();
        System.out.println("Conexión recibida de: " + cliente.getInetAddress().getHostName());
    }

    private void obtenerFlujos() throws IOException {
        salida = new ObjectOutputStream(cliente.getOutputStream());
        entrada = new ObjectInputStream(cliente.getInputStream());
        System.out.println("Flujos de comunicación establecidos");
    }

    private void procesarConexion() throws IOException {
        enviarDatos("Conectado al servidor de archivos. Puede solicitar archivos.");
        
        try {
            while (true) {
                Object objetoRecibido = entrada.readObject();
                
                if (objetoRecibido instanceof String) {
                    String mensaje = (String) objetoRecibido;
                    System.out.println("Cliente: " + mensaje);
                    
                    if (mensaje.equals("CLIENTE:FIN")) {
                        System.out.println("Cliente cerró la conexión");
                        break;
                    } else if (mensaje.startsWith("SOLICITUD_ARCHIVO:")) {
                        String nombreArchivo = mensaje.substring(18);
                        procesarSolicitudArchivo(nombreArchivo);
                    } else if (mensaje.equals("INICIAR_TRANSFERENCIA")) {
                        transferirArchivo();
                    } else if (mensaje.equals("LISTAR_ARCHIVOS")) {
                        listarArchivosDisponibles();
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            System.out.println("Error: Objeto desconocido recibido");
        } catch (SocketException e) {
            System.out.println("Cliente desconectado abruptamente");
        }
    }

    private void listarArchivosDisponibles() {
        try {
            File[] archivos = directorioArchivos.listFiles();
            
            if (archivos == null || archivos.length == 0) {
                enviarDatos("SIN_ARCHIVOS");
                System.out.println("Cliente solicitó lista de archivos - No hay archivos disponibles");
                return;
            }
            
            StringBuilder lista = new StringBuilder();
            lista.append("Archivos disponibles en el servidor:\n");
            
            for (File archivo : archivos) {
                if (archivo.isFile()) {
                    lista.append("  ").append(archivo.getName())
                         .append(" (").append(archivo.length()).append(" bytes)\n");
                }
            }
            
            enviarDatos(lista.toString());
            System.out.println("Lista de archivos enviada al cliente - " + archivos.length + " archivos disponibles");
            
        } catch (IOException e) {
            System.out.println("Error al listar archivos: " + e.getMessage());
            try {
                enviarDatos("ERROR: No se pudo obtener la lista de archivos");
            } catch (IOException ex) {
                System.out.println("Error al enviar mensaje de error: " + ex.getMessage());
            }
        }
    }

    private void procesarSolicitudArchivo(String nombreArchivo) {
        try {
            archivoSolicitado = nombreArchivo;
            File archivo = new File(directorioArchivos, nombreArchivo);
            
            if (archivo.exists() && archivo.isFile()) {
                enviarDatos("ARCHIVO_ENCONTRADO: " + nombreArchivo + " (" + archivo.length() + " bytes)");
                System.out.println("Archivo encontrado: " + archivo.getAbsolutePath());
                System.out.println("Tamaño: " + archivo.length() + " bytes");
            } else {
                enviarDatos("ARCHIVO_NO_ENCONTRADO: El archivo '" + nombreArchivo + "' no existe");
                System.out.println("Archivo no encontrado: " + nombreArchivo);
            }
        } catch (Exception e) {
            System.out.println("Error al procesar solicitud: " + e.getMessage());
        }
    }

    private void transferirArchivo() {
        try {
            if (archivoSolicitado == null) {
                enviarDatos("ERROR: No hay archivo seleccionado para transferir");
                return;
            }
            
            File archivo = new File(directorioArchivos, archivoSolicitado);
            
            if (!archivo.exists()) {
                enviarDatos("ERROR: El archivo ya no está disponible");
                return;
            }
            
            // Leer archivo en bytes
            FileInputStream fis = new FileInputStream(archivo);
            byte[] datosArchivo = new byte[(int) archivo.length()];
            fis.read(datosArchivo);
            fis.close();
            
            Archivo archivoTransferir = new Archivo(archivoSolicitado, datosArchivo);
            salida.writeObject(archivoTransferir);
            System.out.println("Archivo enviado: " + archivoSolicitado);
            System.out.println("Bytes transferidos: " + datosArchivo.length);
            
        } catch (IOException e) {
            System.out.println("Error al transferir archivo: " + e.getMessage());
            try {
                enviarDatos("ERROR: No se pudo transferir el archivo: " + e.getMessage());
            } catch (IOException ex) {
                System.out.println("Error al enviar mensaje de error: " + ex.getMessage());
            }
        }
    }

    private void enviarDatos(String mensaje) throws IOException {
        salida.writeObject("SERVIDOR: " + mensaje);
        System.out.println("Enviado: " + mensaje);
    }

    private void cerrarConexion() {
        System.out.println("Cerrando conexión con el cliente...");
        try {
            if (salida != null) salida.close();
            if (entrada != null) entrada.close();
            if (cliente != null) cliente.close();
            System.out.println("Conexión cerrada");
            System.out.println("--- Listo para nueva conexión ---");
        } catch (IOException e) {
            System.out.println("Error al cerrar conexión: " + e.getMessage());
        }
    }

    private void cerrarServidor() {
        System.out.println("Cerrando servidor...");
        ejecutando = false;
        try {
            if (servidor != null) {
                servidor.close();
            }
        } catch (IOException e) {
            System.out.println("Error al cerrar servidor: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        Servidor servidor = new Servidor();
        servidor.ejecutarServidor();
    }
}