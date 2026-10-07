package limatrack.modelo;

public class Ruta {
    private int idRuta;
    private String codigo;
    private String nombre;
    private String origen;
    private String destino;
    private int totalBuses;
    private int cumplimientoPct;
    private String estadoGeneral; // Normal, Demorada, Desvío

    public int getIdRuta() { return idRuta; }
    public void setIdRuta(int idRuta) { this.idRuta = idRuta; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getOrigen() { return origen; }
    public void setOrigen(String origen) { this.origen = origen; }

    public String getDestino() { return destino; }
    public void setDestino(String destino) { this.destino = destino; }

    public int getTotalBuses() { return totalBuses; }
    public void setTotalBuses(int totalBuses) { this.totalBuses = totalBuses; }

    public int getCumplimientoPct() { return cumplimientoPct; }
    public void setCumplimientoPct(int cumplimientoPct) { this.cumplimientoPct = cumplimientoPct; }

    public String getEstadoGeneral() { return estadoGeneral; }
    public void setEstadoGeneral(String estadoGeneral) { this.estadoGeneral = estadoGeneral; }
}
