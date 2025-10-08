import java.io.*;
import java.net.*;

public class Servidor {
    private ServerSocket servidorSocket; // Socket del servidor
    private Socket clienteSocket;      // Socket para la conexión con el cliente
    private ObjectOutputStream salida;
    private ObjectInputStream entrada;
    private final File directorioArchivos;

    public Servidor() {
        // Se define el directorio donde se almacenarán los archivos que el cliente puede solicitar.
        this.directorioArchivos = new File("archivos_servidor");
        if (!directorioArchivos.exists()) {
            directorioArchivos.mkdir();
            System.out.println("Directorio 'archivos_servidor' creado.");
        }
    }

    public void ejecutarServidor() {
        try {
            // --- ETAPA 1: Creación del Socket del Servidor ---
            // Se crea un ServerSocket que escuchará en el puerto 11000.
            // El puerto es el punto de comunicación en el servidor.
            servidorSocket = new ServerSocket(11000);
            System.out.println("Servidor iniciado y escuchando en el puerto 11000.");
            System.out.println("Directorio de archivos: " + directorioArchivos.getAbsolutePath());
            System.out.println("Coloque aquí los archivos que desea compartir.");

            // El servidor se mantiene en un bucle infinito para aceptar múltiples conexiones de clientes (una a la vez).
            while (true) {
                try {
                    System.out.println("\nEsperando una conexión de cliente...");

                    // --- ETAPA 2: Espera y Aceptación de Conexión (accept) ---
                    // El método accept() es bloqueante, detiene la ejecución hasta que un cliente se conecta.
                    // Cuando un cliente se conecta, devuelve un objeto Socket para comunicarse con él.
                    clienteSocket = servidorSocket.accept();
                    System.out.println("Conexión establecida con: " + clienteSocket.getInetAddress().getHostName());

                    // --- ETAPA 3: Obtención de Flujos de Entrada/Salida ---
                    // Se obtienen los flujos para enviar y recibir objetos, lo que facilita la comunicación.
                    salida = new ObjectOutputStream(clienteSocket.getOutputStream());
                    salida.flush(); // Asegura que el encabezado del stream se envíe inmediatamente.
                    entrada = new ObjectInputStream(clienteSocket.getInputStream());
                    System.out.println("Flujos de comunicación creados correctamente.");

                    // Inicia el procesamiento de la conexión con el cliente.
                    procesarConexion();

                } catch (IOException e) {
                    System.err.println("Error durante la conexión con un cliente: " + e.getMessage());
                } finally {
                    // --- ETAPA 5: Cierre de la Conexión ---
                    // Se asegura de cerrar la conexión con el cliente actual antes de esperar uno nuevo.
                    cerrarConexionCliente();
                }
            }
        } catch (IOException e) {
            System.err.println("Error fatal al iniciar el servidor: " + e.getMessage());
        }
    }

    private void procesarConexion() throws IOException {
        enviarMensaje("Conexión exitosa. Bienvenido al servidor de archivos.");

        try {
            // El servidor se mantiene escuchando las solicitudes del cliente hasta que este decida salir.
            while (true) {
                String solicitud = (String) entrada.readObject();
                System.out.println("RECIBIDO: Solicitud del cliente -> " + solicitud);

                if (solicitud.equals("LISTAR_ARCHIVOS")) {
                    enviarListaArchivos();
                } else if (solicitud.startsWith("SOLICITAR_ARCHIVO:")) {
                    String nombreArchivo = solicitud.substring(18);
                    enviarArchivo(nombreArchivo);
                } else if (solicitud.equals("SALIR")) {
                    System.out.println("El cliente ha decidido terminar la conexión.");
                    break;
                }
            }
        } catch (ClassNotFoundException e) {
            System.err.println("Error: Se recibió un objeto de tipo desconocido.");
        } catch (EOFException e) {
            System.out.println("El cliente cerró la conexión de forma inesperada.");
        }
    }

    private void enviarListaArchivos() throws IOException {
        File[] archivos = directorioArchivos.listFiles();
        if (archivos == null || archivos.length == 0) {
            enviarMensaje("No hay archivos disponibles en el servidor.");
            return;
        }

        StringBuilder lista = new StringBuilder("--- Archivos Disponibles ---\n");
        for (File f : archivos) {
            if (f.isFile()) {
                lista.append("- ").append(f.getName()).append(" (" + f.length() + " bytes)\n");
            }
        }
        enviarMensaje(lista.toString());
    }

    private void enviarArchivo(String nombreArchivo) throws IOException {
        File archivo = new File(directorioArchivos, nombreArchivo);

        if (archivo.exists() && archivo.isFile()) {
            // Notifica al cliente que el archivo fue encontrado y se enviará.
            enviarMensaje("ARCHIVO_ENCONTRADO");

            // --- ETAPA 4: Transferencia de Datos ---
            // Se lee el archivo a un arreglo de bytes y se encapsula en un objeto 'Archivo'.
            FileInputStream fis = new FileInputStream(archivo);
            byte[] buffer = new byte[(int) archivo.length()];
            fis.read(buffer);
            fis.close();

            Archivo archivoParaEnviar = new Archivo(nombreArchivo, buffer);
            salida.writeObject(archivoParaEnviar); // Se envía el objeto al cliente.
            salida.flush();
            System.out.println("ÉXITO: Archivo '" + nombreArchivo + "' enviado al cliente.");
        } else {
            // Si el archivo no existe, se notifica al cliente.
            enviarMensaje("ARCHIVO_NO_ENCONTRADO");
            System.out.println("AVISO: El cliente solicitó un archivo inexistente: '" + nombreArchivo + "'");
        }
    }

    private void enviarMensaje(String mensaje) throws IOException {
        salida.writeObject(mensaje);
        salida.flush();
        System.out.println("ENVIADO: Mensaje al cliente -> " + mensaje);
    }

    private void cerrarConexionCliente() {
        System.out.println("Cerrando conexión con el cliente...");
        try {
            if (salida != null) salida.close();
            if (entrada != null) entrada.close();
            if (clienteSocket != null) clienteSocket.close();
            System.out.println("Conexión con el cliente cerrada.");
        } catch (IOException e) {
            System.err.println("Error al cerrar la conexión con el cliente: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        new Servidor().ejecutarServidor();
    }
}