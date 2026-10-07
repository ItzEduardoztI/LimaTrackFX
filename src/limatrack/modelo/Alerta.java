package limatrack.modelo;

import java.time.LocalDateTime;

public class Alerta {
    private int idAlerta;
    private int idBus;
    private String codigoBus;
    private String tipo;
    private String descripcion;
    private String severidad;
    private LocalDateTime fechaHora;
    private boolean atendida;

    public int getIdAlerta() { return idAlerta; }
    public void setIdAlerta(int idAlerta) { this.idAlerta = idAlerta; }

    public int getIdBus() { return idBus; }
    public void setIdBus(int idBus) { this.idBus = idBus; }

    public String getCodigoBus() { return codigoBus; }
    public void setCodigoBus(String codigoBus) { this.codigoBus = codigoBus; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getSeveridad() { return severidad; }
    public void setSeveridad(String severidad) { this.severidad = severidad; }

    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }

    public boolean isAtendida() { return atendida; }
    public void setAtendida(boolean atendida) { this.atendida = atendida; }
}
