import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class FileCellRenderer extends DefaultListCellRenderer {
    private final Map<String, ImageIcon> iconMap = new HashMap<>();
    private static final int ICON_SIZE = 64; // Tamaño uniforme para todos los íconos (64x64)

    public FileCellRenderer() {
        // Carga los íconos originales
        ImageIcon imgIcon = new ImageIcon(getClass().getResource("icons/image.png"));
        ImageIcon pdfIcon = new ImageIcon(getClass().getResource("icons/pdf.png"));
        ImageIcon wordIcon = new ImageIcon(getClass().getResource("icons/word.png"));
        ImageIcon fileIcon = new ImageIcon(getClass().getResource("icons/file.png"));
        
        // Escala los íconos al tamaño deseado y los guarda
        iconMap.put("jpg", scaleIcon(imgIcon));
        iconMap.put("jpeg", scaleIcon(imgIcon));
        iconMap.put("png", scaleIcon(imgIcon));
        iconMap.put("gif", scaleIcon(imgIcon));
        iconMap.put("pdf", scaleIcon(pdfIcon));
        iconMap.put("doc", scaleIcon(wordIcon));
        iconMap.put("docx", scaleIcon(wordIcon));
        iconMap.put("other", scaleIcon(fileIcon));
    }

    // Método para escalar los íconos a un tamaño estándar
    private ImageIcon scaleIcon(ImageIcon icon) {
        Image img = icon.getImage();
        Image scaledImg = img.getScaledInstance(ICON_SIZE, ICON_SIZE, Image.SCALE_SMOOTH);
        return new ImageIcon(scaledImg);
    }

    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        
        String fileName = (String) value;
        String extension = getFileExtension(fileName);
        
        ImageIcon icon = iconMap.getOrDefault(extension, iconMap.get("other"));
        label.setIcon(icon);
        
        // --- AJUSTES PARA LA VISTA EN CUADRÍCULA ---
        // Pone el texto debajo del ícono
        label.setHorizontalTextPosition(JLabel.CENTER);
        label.setVerticalTextPosition(JLabel.BOTTOM);
        
        // Asegura que el texto no sea demasiado largo para la celda
        // Usamos HTML para permitir que el texto se divida en varias líneas si es necesario
        label.setText("<html><center>" + getShortenedFileName(fileName) + "</center></html>");
        
        // Alineación y tamaño de la celda
        label.setHorizontalAlignment(JLabel.CENTER);
        label.setPreferredSize(new Dimension(100, 100)); // Tamaño fijo para cada celda
        
        return label;
    }
    
    // Acorta el nombre del archivo si es muy largo
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