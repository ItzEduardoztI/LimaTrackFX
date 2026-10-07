package limatrack.dao;

import limatrack.db.Conexion;
import limatrack.modelo.DatoDemanda;
import limatrack.modelo.MetricaDiaria;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MetricaDAO {

    public MetricaDiaria obtenerMetricasHoy(int idEmpresa) throws SQLException {
        String sql = "SELECT unidades_activas, unidades_totales, cumplimiento_pct, "
                + "pasajeros_totales, eta_promedio_min, precision_ml_pct "
                + "FROM metricas_diarias WHERE id_empresa = ? "
                + "ORDER BY fecha DESC LIMIT 1";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idEmpresa);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    MetricaDiaria m = new MetricaDiaria();
                    m.setUnidadesActivas(rs.getInt("unidades_activas"));
                    m.setUnidadesTotales(rs.getInt("unidades_totales"));
                    m.setCumplimientoPct(rs.getDouble("cumplimiento_pct"));
                    m.setPasajerosTotales(rs.getInt("pasajeros_totales"));
                    m.setEtaPromedioMin(rs.getInt("eta_promedio_min"));
                    m.setPrecisionMlPct(rs.getDouble("precision_ml_pct"));
                    return m;
                }
            }
        }
        // valores por defecto si aún no hay fila para hoy
        MetricaDiaria vacio = new MetricaDiaria();
        vacio.setUnidadesActivas(0);
        vacio.setUnidadesTotales(0);
        vacio.setCumplimientoPct(0);
        vacio.setPasajerosTotales(0);
        vacio.setEtaPromedioMin(0);
        vacio.setPrecisionMlPct(0);
        return vacio;
    }

    public List<DatoDemanda> obtenerDemandaHoy(int idEmpresa) throws SQLException {
        List<DatoDemanda> lista = new ArrayList<>();
        String sql = "SELECT hora, pasajeros, nivel FROM demanda_horaria "
                + "WHERE id_empresa = ? AND fecha = "
                + "(SELECT MAX(fecha) FROM demanda_horaria WHERE id_empresa = ?) "
                + "ORDER BY hora";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idEmpresa);
            ps.setInt(2, idEmpresa);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new DatoDemanda(rs.getInt("hora"), rs.getInt("pasajeros"), rs.getString("nivel")));
                }
            }
        }
        return lista;
    }

    public void incrementarPasajerosHoy(int idEmpresa, int delta) throws SQLException {
        String sql = "UPDATE metricas_diarias SET pasajeros_totales = pasajeros_totales + ? "
                + "WHERE id_empresa = ? AND fecha = CURDATE()";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, delta);
            ps.setInt(2, idEmpresa);
            ps.executeUpdate();
        }
    }
}
