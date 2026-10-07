package limatrack.modelo;

public class MetricaDiaria {
    private int unidadesActivas;
    private int unidadesTotales;
    private double cumplimientoPct;
    private int pasajerosTotales;
    private int etaPromedioMin;
    private double precisionMlPct;

    public int getUnidadesActivas() { return unidadesActivas; }
    public void setUnidadesActivas(int unidadesActivas) { this.unidadesActivas = unidadesActivas; }

    public int getUnidadesTotales() { return unidadesTotales; }
    public void setUnidadesTotales(int unidadesTotales) { this.unidadesTotales = unidadesTotales; }

    public double getCumplimientoPct() { return cumplimientoPct; }
    public void setCumplimientoPct(double cumplimientoPct) { this.cumplimientoPct = cumplimientoPct; }

    public int getPasajerosTotales() { return pasajerosTotales; }
    public void setPasajerosTotales(int pasajerosTotales) { this.pasajerosTotales = pasajerosTotales; }

    public int getEtaPromedioMin() { return etaPromedioMin; }
    public void setEtaPromedioMin(int etaPromedioMin) { this.etaPromedioMin = etaPromedioMin; }

    public double getPrecisionMlPct() { return precisionMlPct; }
    public void setPrecisionMlPct(double precisionMlPct) { this.precisionMlPct = precisionMlPct; }
}
