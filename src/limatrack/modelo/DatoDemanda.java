package limatrack.modelo;

public class DatoDemanda {
    private int hora;
    private int pasajeros;
    private String nivel; // NORMAL, SATURADO, PICO

    public DatoDemanda(int hora, int pasajeros, String nivel) {
        this.hora = hora;
        this.pasajeros = pasajeros;
        this.nivel = nivel;
    }

    public int getHora() { return hora; }
    public int getPasajeros() { return pasajeros; }
    public String getNivel() { return nivel; }

    public String getEtiquetaHora() {
        if (hora == 0) return "12am";
        if (hora < 12) return hora + "am";
        if (hora == 12) return "12pm";
        return (hora - 12) + "pm";
    }
}
