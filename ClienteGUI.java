// ClienteGUI.java
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.io.File;
import java.util.List; // Se añade la importación que faltaba

public class ClienteGUI extends JFrame {
    // Referencia al motor de comunicación
    private final Cliente cliente;

    // Componentes de la GUI
    private JTextField hostTextField;
    private JButton conectarButton, desconectarButton;
    private JList<String> listaArchivosServidor;
    private DefaultListModel<String> modelArchivosServidor;
    private JButton refrescarButton, descargarButton;
    private JTextArea logTextArea;

    public ClienteGUI() {
        super("Cliente de Archivos GUI");
        this.cliente = new Cliente(); // Instancia del motor de comunicación

        // --- Configuración de la Ventana Principal ---
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        setLocationRelativeTo(null);

        // --- Panel de Conexión (Norte) ---
        JPanel panelConexion = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelConexion.setBorder(new TitledBorder("Conexión"));
        hostTextField = new JTextField("localhost", 15);
        conectarButton = new JButton("Conectar");
        desconectarButton = new JButton("Desconectar");
        desconectarButton.setEnabled(false);
        panelConexion.add(new JLabel("Host del Servidor:"));
        panelConexion.add(hostTextField);
        panelConexion.add(conectarButton);
        panelConexion.add(desconectarButton);
        add(panelConexion, BorderLayout.NORTH);

        // --- Panel Central (Archivos y Acciones) ---
        JPanel panelCentral = new JPanel(new BorderLayout(10, 10));
        
        modelArchivosServidor = new DefaultListModel<>();
        listaArchivosServidor = new JList<>(modelArchivosServidor);
        JScrollPane scrollServidor = new JScrollPane(listaArchivosServidor);
        scrollServidor.setBorder(new TitledBorder("Archivos en el Servidor"));
        panelCentral.add(scrollServidor, BorderLayout.CENTER);

        JPanel panelAcciones = new JPanel(new GridLayout(2, 1, 5, 5));
        refrescarButton = new JButton("Refrescar Lista");
        descargarButton = new JButton("Descargar Archivo");
        panelAcciones.add(refrescarButton);
        panelAcciones.add(descargarButton);
        panelCentral.add(panelAcciones, BorderLayout.EAST);
        add(panelCentral, BorderLayout.CENTER);
        
        refrescarButton.setEnabled(false);
        descargarButton.setEnabled(false);
        

        logTextArea = new JTextArea(8, 0); 
        logTextArea.setEditable(false);
        JScrollPane scrollLog = new JScrollPane(logTextArea);
        scrollLog.setBorder(new TitledBorder("Log de Actividad"));
        add(scrollLog, BorderLayout.SOUTH);

        configurarActionListeners();
    }

    private void configurarActionListeners() {
        conectarButton.addActionListener(e -> {
            String host = hostTextField.getText().trim();
            if (host.isEmpty()) {
                log("Por favor, ingrese la dirección del host.");
                return;
            }
            
            new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    log("Intentando conectar con " + host + "...");
                    return cliente.conectar(host);
                }

                @Override
                protected void done() {
                    try {
                        if (get()) {
                            log("Conexión exitosa.");
                            conectarButton.setEnabled(false);
                            hostTextField.setEnabled(false);
                            desconectarButton.setEnabled(true);
                            refrescarButton.setEnabled(true);
                            descargarButton.setEnabled(true);
                            refrescarListaArchivos();
                        } else {
                            log("Fallo en la conexión. Revise el host o si el servidor está activo.");
                        }
                    } catch (Exception ex) {
                        log("Error durante la conexión: " + ex.getMessage());
                    }
                }
            }.execute();
        });

        desconectarButton.addActionListener(e -> {
            cliente.desconectar();
            log("Desconectado del servidor.");
            conectarButton.setEnabled(true);
            hostTextField.setEnabled(true);
            desconectarButton.setEnabled(false);
            refrescarButton.setEnabled(false);
            descargarButton.setEnabled(false);
            modelArchivosServidor.clear();
        });

        refrescarButton.addActionListener(e -> refrescarListaArchivos());

        descargarButton.addActionListener(e -> {
            String archivoSeleccionado = listaArchivosServidor.getSelectedValue();
            if (archivoSeleccionado == null) {
                log("Por favor, seleccione un archivo de la lista para descargar.");
                return;
            }
            
            String nombreArchivo = archivoSeleccionado.split(" \\(")[0];
            descargarButton.setEnabled(false);
            
            new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    log("Solicitando archivo: " + nombreArchivo + "...");
                    return cliente.descargarArchivo(nombreArchivo);
                }

                @Override
                protected void done() {
                    try {
                        if (get()) {
                            log("¡Éxito! Archivo '" + nombreArchivo + "' guardado en la carpeta 'descargas_cliente'.");
                        } else {
                            log("Error: El archivo '" + nombreArchivo + "' no se pudo descargar.");
                        }
                    } catch (Exception ex) {
                        log("Error crítico durante la descarga: " + ex.getMessage());
                    } finally {
                        descargarButton.setEnabled(true);
                    }
                }
            }.execute();
        });
    }

    private void refrescarListaArchivos() {
        new SwingWorker<List<String>, Void>() {
            @Override
            protected List<String> doInBackground() throws Exception {
                log("Solicitando lista de archivos...");
                return cliente.listarArchivos();
            }

            @Override
            protected void done() {
                try {
                    List<String> archivos = get();
                    modelArchivosServidor.clear();
                    if (archivos.isEmpty()) {
                        log("El servidor no tiene archivos disponibles.");
                    } else {
                        archivos.forEach(modelArchivosServidor::addElement);
                        log("Lista de archivos actualizada.");
                    }
                } catch (Exception ex) {
                    log("Error al obtener la lista de archivos: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private void log(String mensaje) {
        SwingUtilities.invokeLater(() -> logTextArea.append(mensaje + "\n"));
    }
}