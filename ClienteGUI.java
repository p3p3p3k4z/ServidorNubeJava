import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.io.File;
import java.util.List;
import javax.imageio.ImageIO;

public class ClienteGUI extends JFrame {
    private final Cliente cliente;

    // Componentes de la GUI
    private JTextField hostTextField;
    private JButton conectarButton, desconectarButton;
    private JList<String> listaArchivosServidor;
    private DefaultListModel<String> modelArchivosServidor;
    private JButton refrescarButton, descargarButton, abrirButton;
    private JTextArea logTextArea;
    
    // Panel de vista previa
    private JPanel previewPanel;
    private JLabel imagePreviewLabel;
    private JLabel infoPreviewLabel;
    private CardLayout cardLayout;

    public ClienteGUI() {
        super("Explorador de Archivos Remotos");
        this.cliente = new Cliente();

        setSize(850, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        setLocationRelativeTo(null);

        // Panel de Conexión (Norte)
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

        // Panel Principal (Centro) con división
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.4);
        add(splitPane, BorderLayout.CENTER);

        // Panel Izquierdo: Lista de Archivos y Acciones
                // --- Panel Izquierdo: Lista de Archivos y Acciones ---
        JPanel panelIzquierdo = new JPanel(new BorderLayout(10, 10));
        modelArchivosServidor = new DefaultListModel<>();
        listaArchivosServidor = new JList<>(modelArchivosServidor);
        
        // --- AJUSTES PARA LA VISTA EN CUADRÍCULA ---
        listaArchivosServidor.setLayoutOrientation(JList.HORIZONTAL_WRAP); // 1. Organiza en filas
        listaArchivosServidor.setVisibleRowCount(-1);                     // 2. Hace que las filas se ajusten al espacio
        listaArchivosServidor.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        listaArchivosServidor.setCellRenderer(new FileCellRenderer());     // 3. Usa nuestro renderer actualizado
        
        JScrollPane scrollServidor = new JScrollPane(listaArchivosServidor);
        scrollServidor.setBorder(new TitledBorder("Archivos en el Servidor"));
        panelIzquierdo.add(scrollServidor, BorderLayout.CENTER);

        JPanel panelAcciones = new JPanel(new GridLayout(3, 1, 5, 5));
        refrescarButton = new JButton("Refrescar");
        descargarButton = new JButton("Descargar");
        abrirButton = new JButton("Abrir Archivo");
        panelAcciones.add(refrescarButton);
        panelAcciones.add(descargarButton);
        panelAcciones.add(abrirButton);
        panelIzquierdo.add(panelAcciones, BorderLayout.SOUTH);
        splitPane.setLeftComponent(panelIzquierdo);

        // Panel Derecho: Vista Previa
        cardLayout = new CardLayout();
        previewPanel = new JPanel(cardLayout);
        previewPanel.setBorder(new TitledBorder("Vista Previa"));
        
        infoPreviewLabel = new JLabel("Seleccione un archivo para ver detalles", SwingConstants.CENTER);
        previewPanel.add(infoPreviewLabel, "info");
        
        imagePreviewLabel = new JLabel();
        imagePreviewLabel.setHorizontalAlignment(SwingConstants.CENTER);
        JScrollPane imageScrollPane = new JScrollPane(imagePreviewLabel);
        previewPanel.add(imageScrollPane, "image");
        splitPane.setRightComponent(previewPanel);
        
        refrescarButton.setEnabled(false);
        descargarButton.setEnabled(false);
        abrirButton.setEnabled(false);
        
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
            if (host.isEmpty()) { log("Por favor, ingrese la dirección del host."); return; }
            
            new SwingWorker<Boolean, Void>() {
                @Override protected Boolean doInBackground() { log("Intentando conectar con " + host + "..."); return cliente.conectar(host); }
                @Override protected void done() {
                    try {
                        if (get()) {
                            log("Conexión exitosa.");
                            conectarButton.setEnabled(false); hostTextField.setEnabled(false); desconectarButton.setEnabled(true);
                            refrescarButton.setEnabled(true); descargarButton.setEnabled(true); abrirButton.setEnabled(true);
                            refrescarListaArchivos();
                        } else { log("Fallo en la conexión. Revise el host o si el servidor está activo."); }
                    } catch (Exception ex) { log("Error durante la conexión: " + ex.getMessage()); }
                }
            }.execute();
        });

        desconectarButton.addActionListener(e -> {
            cliente.desconectar();
            log("Desconectado del servidor.");
            conectarButton.setEnabled(true); hostTextField.setEnabled(true); desconectarButton.setEnabled(false);
            refrescarButton.setEnabled(false); descargarButton.setEnabled(false); abrirButton.setEnabled(false);
            modelArchivosServidor.clear();
        });

        refrescarButton.addActionListener(e -> refrescarListaArchivos());

        descargarButton.addActionListener(e -> {
            String archivoSeleccionado = listaArchivosServidor.getSelectedValue();
            if (archivoSeleccionado == null) { log("Por favor, seleccione un archivo de la lista."); return; }
            String nombreArchivo = archivoSeleccionado.split(" \\(")[0];
            
            new SwingWorker<Boolean, Void>() {
                // CORRECCIÓN: Se añade "throws Exception" para manejar posibles errores de red.
                @Override protected Boolean doInBackground() throws Exception {
                    log("Descargando: " + nombreArchivo + "...");
                    return cliente.descargarArchivo(nombreArchivo);
                }
                @Override protected void done() {
                    try {
                        if (get()) { log("¡Éxito! Archivo '" + nombreArchivo + "' guardado en 'descargas_cliente'."); }
                        else { log("Error: No se pudo descargar el archivo '" + nombreArchivo + "'."); }
                    } catch (Exception ex) { log("Error crítico durante la descarga: " + ex.getMessage()); }
                }
            }.execute();
        });

        abrirButton.addActionListener(e -> {
            String archivoSeleccionado = listaArchivosServidor.getSelectedValue();
            if (archivoSeleccionado == null) { log("Por favor, seleccione un archivo para abrir."); return; }
            String nombreArchivo = archivoSeleccionado.split(" \\(")[0];
            
            new SwingWorker<File, Void>() {
                @Override
                protected File doInBackground() throws Exception {
                    File archivoLocal = new File("descargas_cliente", nombreArchivo);
                    if (!archivoLocal.exists()) {
                        log("El archivo no está descargado. Descargando primero...");
                        if (!cliente.descargarArchivo(nombreArchivo)) {
                            throw new Exception("No se pudo descargar el archivo para abrirlo.");
                        }
                    }
                    return archivoLocal;
                }

                @Override
                protected void done() {
                    try {
                        File archivo = get();
                        if (Desktop.isDesktopSupported()) {
                            Desktop.getDesktop().open(archivo);
                            log("Abriendo '" + archivo.getName() + "' con la aplicación predeterminada.");
                        } else {
                            log("Error: No se puede abrir archivos en este sistema.");
                        }
                    } catch (Exception ex) {
                        log("Error al abrir el archivo: " + ex.getMessage());
                    }
                }
            }.execute();
        });
        
        listaArchivosServidor.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String archivoSeleccionado = listaArchivosServidor.getSelectedValue();
                if (archivoSeleccionado != null) {
                    actualizarPreview(archivoSeleccionado);
                }
            }
        });
    }

    private void refrescarListaArchivos() {
        new SwingWorker<List<String>, Void>() {
            // CORRECCIÓN: Se añade "throws Exception" para manejar posibles errores de red.
            @Override protected List<String> doInBackground() throws Exception {
                log("Solicitando lista de archivos...");
                return cliente.listarArchivos();
            }
            @Override protected void done() {
                try {
                    List<String> archivos = get();
                    modelArchivosServidor.clear();
                    if (archivos.isEmpty()) { log("El servidor no tiene archivos disponibles."); }
                    else { archivos.forEach(modelArchivosServidor::addElement); log("Lista de archivos actualizada."); }
                } catch (Exception ex) { log("Error al obtener la lista de archivos: " + ex.getMessage()); }
            }
        }.execute();
    }
    
    private void actualizarPreview(String archivoSeleccionado) {
        String nombreArchivo = archivoSeleccionado.split(" \\(")[0];
        String extension = getFileExtension(nombreArchivo);
        
        if (extension.matches("png|jpg|jpeg|gif")) {
            new SwingWorker<ImageIcon, Void>() {
                @Override
                protected ImageIcon doInBackground() throws Exception {
                    File archivoLocal = new File("descargas_cliente", nombreArchivo);
                    if (!archivoLocal.exists()) {
                        cliente.descargarArchivo(nombreArchivo);
                    }
                    Image img = ImageIO.read(archivoLocal);
                    int previewWidth = previewPanel.getWidth() - 20;
                    int previewHeight = previewPanel.getHeight() - 40;
                    Image scaledImg = img.getScaledInstance(previewWidth, previewHeight, Image.SCALE_SMOOTH);
                    return new ImageIcon(scaledImg);
                }

                @Override
                protected void done() {
                    try {
                        imagePreviewLabel.setIcon(get());
                        cardLayout.show(previewPanel, "image");
                    } catch (Exception e) {
                        infoPreviewLabel.setText("<html>No se pudo cargar la vista previa de la imagen.<br>" + nombreArchivo + "</html>");
                        cardLayout.show(previewPanel, "info");
                    }
                }
            }.execute();
        } else {
            infoPreviewLabel.setText("<html><b>Archivo:</b> " + nombreArchivo + "<br><b>Tipo:</b> Documento " + extension.toUpperCase() + "</html>");
            cardLayout.show(previewPanel, "info");
        }
    }
    
    private String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex > 0) ? fileName.substring(dotIndex + 1).toLowerCase() : "";
    }

    private void log(String mensaje) {
        SwingUtilities.invokeLater(() -> logTextArea.append(mensaje + "\n"));
    }
}