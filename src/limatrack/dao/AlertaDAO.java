package limatrack.dao;

import limatrack.db.Conexion;
import limatrack.modelo.Alerta;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class AlertaDAO {

    public List<Alerta> listarPendientesPorEmpresa(int idEmpresa) throws SQLException {
        List<Alerta> lista = new ArrayList<>();
        String sql = "SELECT a.id_alerta, a.id_bus, b.codigo AS codigo_bus, a.tipo, a.descripcion, "
                + "a.severidad, a.fecha_hora, a.atendida "
                + "FROM alertas a JOIN buses b ON b.id_bus = a.id_bus "
                + "WHERE b.id_empresa = ? AND a.atendida = 0 "
                + "ORDER BY a.fecha_hora DESC";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idEmpresa);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Alerta a = new Alerta();
                    a.setIdAlerta(rs.getInt("id_alerta"));
                    a.setIdBus(rs.getInt("id_bus"));
                    a.setCodigoBus(rs.getString("codigo_bus"));
                    a.setTipo(rs.getString("tipo"));
                    a.setDescripcion(rs.getString("descripcion"));
                    a.setSeveridad(rs.getString("severidad"));
                    Timestamp ts = rs.getTimestamp("fecha_hora");
                    a.setFechaHora(ts != null ? ts.toLocalDateTime() : null);
                    a.setAtendida(rs.getBoolean("atendida"));
                    lista.add(a);
                }
            }
        }
        return lista;
    }

    public int contarPendientesPorEmpresa(int idEmpresa) throws SQLException {
        String sql = "SELECT COUNT(*) FROM alertas a JOIN buses b ON b.id_bus = a.id_bus "
                + "WHERE b.id_empresa = ? AND a.atendida = 0";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idEmpresa);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    public void marcarAtendida(int idAlerta, int idUsuario) throws SQLException {
        String sql = "UPDATE alertas SET atendida = 1, atendida_por = ?, fecha_atencion = NOW() WHERE id_alerta = ?";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            ps.setInt(2, idAlerta);
            ps.executeUpdate();
        }
    }

    public void registrarAlerta(int idBus, String tipo, String descripcion, String severidad) throws SQLException {
        String sql = "INSERT INTO alertas (id_bus, tipo, descripcion, severidad) VALUES (?,?,?,?)";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idBus);
            ps.setString(2, tipo);
            ps.setString(3, descripcion);
            ps.setString(4, severidad);
            ps.executeUpdate();
        }
    }
}
