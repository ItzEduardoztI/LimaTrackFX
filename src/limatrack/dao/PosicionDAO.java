package limatrack.dao;

import limatrack.db.Conexion;
import limatrack.modelo.PosicionGps;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class PosicionDAO {

    public List<PosicionGps> historialPorBus(int idBus, int limite) throws SQLException {
        List<PosicionGps> lista = new ArrayList<>();
        String sql = "SELECT latitud, longitud, velocidad, ocupacion_pct, fecha_hora "
                + "FROM posiciones_gps WHERE id_bus = ? ORDER BY fecha_hora DESC LIMIT ?";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idBus);
            ps.setInt(2, limite);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Timestamp ts = rs.getTimestamp("fecha_hora");
                    lista.add(new PosicionGps(
                            rs.getDouble("latitud"),
                            rs.getDouble("longitud"),
                            rs.getDouble("velocidad"),
                            rs.getInt("ocupacion_pct"),
                            ts != null ? ts.toLocalDateTime() : null));
                }
            }
        }
        return lista;
    }

    public void registrarPosicion(int idBus, double lat, double lon, double velocidad, int ocupacion) throws SQLException {
        String sql = "INSERT INTO posiciones_gps (id_bus, latitud, longitud, velocidad, ocupacion_pct) VALUES (?,?,?,?,?)";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idBus);
            ps.setDouble(2, lat);
            ps.setDouble(3, lon);
            ps.setDouble(4, velocidad);
            ps.setInt(5, ocupacion);
            ps.executeUpdate();
        }
    }

    /**
     * Calcula la distancia total recorrida por un bus sumando la distancia
     * (fórmula de Haversine) entre cada par de posiciones GPS consecutivas
     * registradas en su historial.
     */
    public double calcularKmRecorridos(int idBus) throws SQLException {
        String sql = "SELECT latitud, longitud FROM posiciones_gps WHERE id_bus = ? ORDER BY fecha_hora ASC";
        double total = 0;
        Double latAnt = null, lonAnt = null;
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idBus);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    double lat = rs.getDouble("latitud");
                    double lon = rs.getDouble("longitud");
                    if (latAnt != null) {
                        total += haversineKm(latAnt, lonAnt, lat, lon);
                    }
                    latAnt = lat;
                    lonAnt = lon;
                }
            }
        }
        return total;
    }

    private double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371.0; // radio de la Tierra en km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
