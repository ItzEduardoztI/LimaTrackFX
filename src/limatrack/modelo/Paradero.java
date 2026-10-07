package limatrack.modelo;

public class Paradero {
    private int idParadero;
    private String nombre;
    private String distrito;
    private int orden;

    public int getIdParadero() { return idParadero; }
    public void setIdParadero(int idParadero) { this.idParadero = idParadero; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDistrito() { return distrito; }
    public void setDistrito(String distrito) { this.distrito = distrito; }

    public int getOrden() { return orden; }
    public void setOrden(int orden) { this.orden = orden; }
}
