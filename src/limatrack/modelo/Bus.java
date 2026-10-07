package limatrack.modelo;

public class Bus {
    private int idBus;
    private String codigo;
    private String placa;
    private String modelo;
    private int idRuta;
    private String codigoRuta;
    private String nombreRuta;
    private String nombreConductor;
    private String estado;         // A_TIEMPO, DEMORADO, DESVIO, MANTENIMIENTO, SIN_CONDUCTOR, INACTIVO
    private String ubicacionActual;
    private double velocidadActual;
    private int ocupacionPct;
    private int confianzaMlPct;
    private int retrasoMin;

    public int getIdBus() { return idBus; }
    public void setIdBus(int idBus) { this.idBus = idBus; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getPlaca() { return placa; }
    public void setPlaca(String placa) { this.placa = placa; }

    public String getModelo() { return modelo; }
    public void setModelo(String modelo) { this.modelo = modelo; }

    public int getIdRuta() { return idRuta; }
    public void setIdRuta(int idRuta) { this.idRuta = idRuta; }

    public String getCodigoRuta() { return codigoRuta; }
    public void setCodigoRuta(String codigoRuta) { this.codigoRuta = codigoRuta; }

    public String getNombreRuta() { return nombreRuta; }
    public void setNombreRuta(String nombreRuta) { this.nombreRuta = nombreRuta; }

    public String getNombreConductor() { return nombreConductor; }
    public void setNombreConductor(String nombreConductor) { this.nombreConductor = nombreConductor; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getUbicacionActual() { return ubicacionActual; }
    public void setUbicacionActual(String ubicacionActual) { this.ubicacionActual = ubicacionActual; }

    public double getVelocidadActual() { return velocidadActual; }
    public void setVelocidadActual(double velocidadActual) { this.velocidadActual = velocidadActual; }

    public int getOcupacionPct() { return ocupacionPct; }
    public void setOcupacionPct(int ocupacionPct) { this.ocupacionPct = ocupacionPct; }

    public int getConfianzaMlPct() { return confianzaMlPct; }
    public void setConfianzaMlPct(int confianzaMlPct) { this.confianzaMlPct = confianzaMlPct; }

    public int getRetrasoMin() { return retrasoMin; }
    public void setRetrasoMin(int retrasoMin) { this.retrasoMin = retrasoMin; }

    public String getEstadoLegible() {
        switch (estado) {
            case "A_TIEMPO": return "A tiempo";
            case "DEMORADO": return "Demorada";
            case "DESVIO": return "Desvío";
            case "MANTENIMIENTO": return "Mantenimiento";
            case "SIN_CONDUCTOR": return "Sin conductor";
            case "INACTIVO": return "Inactivo";
            default: return estado;
        }
    }

    public boolean isActivo() {
        return estado != null && !estado.equals("MANTENIMIENTO") && !estado.equals("SIN_CONDUCTOR") && !estado.equals("INACTIVO");
    }
}
