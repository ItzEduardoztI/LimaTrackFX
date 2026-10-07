package limatrack.dao;

import limatrack.db.Conexion;
import limatrack.modelo.Paradero;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class ParaderoDAO {

    /** Devuelve la franja horaria actual del sistema: MANANA, TARDE o NOCHE. */
    public static String franjaActual() {
        int hora = LocalTime.now().getHour();
        if (hora >= 6 && hora <= 11) return "MANANA";
        if (hora >= 12 && hora <= 18) return "TARDE";
        return "NOCHE";
    }

    public List<Paradero> paraderosDeRuta(int idRuta) throws SQLException {
        List<Paradero> lista = new ArrayList<>();
        String sql = "SELECT p.id_paradero, p.nombre, p.distrito, rp.orden "
                + "FROM ruta_paraderos rp JOIN paraderos p ON p.id_paradero = rp.id_paradero "
                + "WHERE rp.id_ruta = ? ORDER BY rp.orden";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idRuta);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Paradero p = new Paradero();
                    p.setIdParadero(rs.getInt("id_paradero"));
                    p.setNombre(rs.getString("nombre"));
                    p.setDistrito(rs.getString("distrito"));
                    p.setOrden(rs.getInt("orden"));
                    lista.add(p);
                }
            }
        }
        return lista;
    }

    /**
     * Minutos promedio históricos para recorrer la ruta completa en la franja
     * horaria indicada (MANANA/TARDE/NOCHE). Devuelve -1 si no hay dato registrado.
     */
    public int minutosPromedio(int idRuta, String franja) throws SQLException {
        String sql = "SELECT minutos_promedio FROM tiempos_promedio_tramo WHERE id_ruta = ? AND franja_horaria = ?";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idRuta);
            ps.setString(2, franja);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("minutos_promedio");
            }
        }
        return -1;
    }

    /** Inserta las 3 franjas horarias por defecto para una ruta recién creada. */
    public void generarTiemposPorDefecto(int idRuta, int tiempoEstimadoMin) throws SQLException {
        String sql = "INSERT INTO tiempos_promedio_tramo (id_ruta, franja_horaria, minutos_promedio) VALUES "
                + "(?, 'MANANA', ?), (?, 'TARDE', ?), (?, 'NOCHE', ?)";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idRuta);
            ps.setInt(2, (int) Math.round(tiempoEstimadoMin * 1.05));
            ps.setInt(3, idRuta);
            ps.setInt(4, (int) Math.round(tiempoEstimadoMin * 1.35));
            ps.setInt(5, idRuta);
            ps.setInt(6, (int) Math.round(tiempoEstimadoMin * 0.85));
            ps.executeUpdate();
        }
    }
}
