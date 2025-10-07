import java.io.Serializable;

public class Archivo implements Serializable {
    private static final long serialVersionUID = 1L;
    private String nombre;
    private byte[] datos;
    
    public Archivo(String nombre, byte[] datos) {
        this.nombre = nombre;
        this.datos = datos;
    }
    
    public String getNombre() {
        return nombre;
    }
    
    public byte[] getDatos() {
        return datos;
    }
    
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    
    public void setDatos(byte[] datos) {
        this.datos = datos;
    }
}