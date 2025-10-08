import javax.swing.*;
import java.awt.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class FileCellRenderer extends DefaultListCellRenderer {
    private final Map<String, ImageIcon> iconMap = new HashMap<>();
    private static final int ICON_SIZE = 64;

    public FileCellRenderer() {
        // Carga y escala los íconos de forma más robusta
        ImageIcon imgIcon = loadAndScaleIcon("icons/image.png", "Imagen");
        ImageIcon pdfIcon = loadAndScaleIcon("icons/pdf.png", "PDF");
        ImageIcon wordIcon = loadAndScaleIcon("icons/word.png", "Word");
        ImageIcon videoIcon = loadAndScaleIcon("icons/video.png", "Video"); // Ícono nuevo
        ImageIcon fileIcon = loadAndScaleIcon("icons/file.png", "Archivo");

        // Mapeo de extensiones de imagen
        iconMap.put("jpg", imgIcon);
        iconMap.put("jpeg", imgIcon);
        iconMap.put("png", imgIcon);
        iconMap.put("gif", imgIcon);

        // Mapeo de extensiones de documentos
        iconMap.put("pdf", pdfIcon);
        iconMap.put("doc", wordIcon);
        iconMap.put("docx", wordIcon);

        // Mapeo de extensiones de video (NUEVO)
        iconMap.put("mp4", videoIcon);
        iconMap.put("webm", videoIcon);
        iconMap.put("mkv", videoIcon);
        iconMap.put("avi", videoIcon);
        
        iconMap.put("other", fileIcon);
    }

    private ImageIcon loadAndScaleIcon(String path, String description) {
        URL resourceUrl = getClass().getResource(path);
        if (resourceUrl == null) {
            // Si no se encuentra el ícono, imprime un error claro en la consola.
            System.err.println("ADVERTENCIA: No se pudo encontrar el ícono en la ruta: " + path);
            System.err.println("Asegúrate de que la carpeta 'icons' exista y esté en el lugar correcto.");
            return null; // Devolver null para manejarlo después
        }
        ImageIcon icon = new ImageIcon(resourceUrl);
        Image scaledImg = icon.getImage().getScaledInstance(ICON_SIZE, ICON_SIZE, Image.SCALE_SMOOTH);
        return new ImageIcon(scaledImg);
    }

    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        
        String fileName = (String) value;
        String extension = getFileExtension(fileName);
        
        ImageIcon icon = iconMap.getOrDefault(extension, iconMap.get("other"));
        if (icon == null) {
            // Si un ícono falló al cargar, usa el de 'otro' como respaldo.
            icon = iconMap.get("other");
        }
        label.setIcon(icon);
        
        label.setHorizontalTextPosition(JLabel.CENTER);
        label.setVerticalTextPosition(JLabel.BOTTOM);
        label.setText("<html><center>" + getShortenedFileName(fileName) + "</center></html>");
        label.setHorizontalAlignment(JLabel.CENTER);
        label.setPreferredSize(new Dimension(100, 100));
        
        return label;
    }
    
    private String getShortenedFileName(String fileName) {
        if (fileName.length() > 20) {
            return fileName.substring(0, 17) + "...";
        }
        return fileName;
    }

    private String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
            return fileName.substring(dotIndex + 1).toLowerCase();
        }
        return "";
    }
}