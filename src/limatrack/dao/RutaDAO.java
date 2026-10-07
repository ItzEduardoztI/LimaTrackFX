package limatrack.dao;

import limatrack.db.Conexion;
import limatrack.modelo.Ruta;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RutaDAO {

    public List<Ruta> listarEstadoPorEmpresa(int idEmpresa) throws SQLException {
        List<Ruta> lista = new ArrayList<>();
        String sql = "SELECT id_ruta, codigo, nombre, origen, destino, total_buses, "
                + "cumplimiento_pct, estado_general FROM vw_estado_rutas "
                + "WHERE id_empresa = ? ORDER BY codigo";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idEmpresa);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Ruta r = new Ruta();
                    r.setIdRuta(rs.getInt("id_ruta"));
                    r.setCodigo(rs.getString("codigo"));
                    r.setNombre(rs.getString("nombre"));
                    r.setOrigen(rs.getString("origen"));
                    r.setDestino(rs.getString("destino"));
                    r.setTotalBuses(rs.getInt("total_buses"));
                    r.setCumplimientoPct(rs.getInt("cumplimiento_pct"));
                    r.setEstadoGeneral(rs.getString("estado_general"));
                    lista.add(r);
                }
            }
        }
        return lista;
    }

    /** Crea una ruta nueva para la empresa indicada. Devuelve el id_ruta generado. */
    public int crearRuta(String codigo, String nombre, String origen, String destino,
                          double distanciaKm, int tiempoEstimadoMin, double tarifa, int idEmpresa) throws SQLException {
        String sql = "INSERT INTO rutas (codigo, nombre, origen, destino, distancia_km, tiempo_estimado_min, tarifa, id_empresa) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, codigo);
            ps.setString(2, nombre);
            ps.setString(3, origen);
            ps.setString(4, destino);
            ps.setDouble(5, distanciaKm);
            ps.setInt(6, tiempoEstimadoMin);
            ps.setDouble(7, tarifa);
            ps.setInt(8, idEmpresa);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return -1;
    }
}
