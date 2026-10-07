package limatrack.modelo;

import java.time.LocalDateTime;

public class PosicionGps {
    private double latitud;
    private double longitud;
    private double velocidad;
    private int ocupacionPct;
    private LocalDateTime fechaHora;

    public PosicionGps(double latitud, double longitud, double velocidad, int ocupacionPct, LocalDateTime fechaHora) {
        this.latitud = latitud;
        this.longitud = longitud;
        this.velocidad = velocidad;
        this.ocupacionPct = ocupacionPct;
        this.fechaHora = fechaHora;
    }

    public double getLatitud() { return latitud; }
    public double getLongitud() { return longitud; }
    public double getVelocidad() { return velocidad; }
    public int getOcupacionPct() { return ocupacionPct; }
    public LocalDateTime getFechaHora() { return fechaHora; }
}
