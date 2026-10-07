package limatrack.dao;

import limatrack.db.Conexion;
import limatrack.modelo.Bus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BusDAO {

    private static final String SELECT_BASE =
        "SELECT b.id_bus, b.codigo, b.placa, b.modelo, b.id_ruta, r.codigo AS codigo_ruta, "
        + "r.nombre AS nombre_ruta, u.nombre_completo AS nombre_conductor, b.estado, "
        + "b.ubicacion_actual, b.velocidad_actual, b.ocupacion_pct, b.confianza_ml_pct, b.retraso_min "
        + "FROM buses b "
        + "LEFT JOIN rutas r ON r.id_ruta = b.id_ruta "
        + "LEFT JOIN usuarios u ON u.id_usuario = b.id_conductor "
        + "WHERE b.id_empresa = ? ";

    public List<Bus> listarPorEmpresa(int idEmpresa) throws SQLException {
        List<Bus> lista = new ArrayList<>();
        String sql = SELECT_BASE + "ORDER BY (b.estado NOT IN ('MANTENIMIENTO','SIN_CONDUCTOR','INACTIVO')) DESC, b.codigo";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idEmpresa);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public Bus obtenerPorId(int idBus) throws SQLException {
        String sql = "SELECT b.id_bus, b.codigo, b.placa, b.modelo, b.id_ruta, r.codigo AS codigo_ruta, "
                + "r.nombre AS nombre_ruta, u.nombre_completo AS nombre_conductor, b.estado, "
                + "b.ubicacion_actual, b.velocidad_actual, b.ocupacion_pct, b.confianza_ml_pct, b.retraso_min "
                + "FROM buses b "
                + "LEFT JOIN rutas r ON r.id_ruta = b.id_ruta "
                + "LEFT JOIN usuarios u ON u.id_usuario = b.id_conductor "
                + "WHERE b.id_bus = ?";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idBus);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapear(rs);
            }
        }
        return null;
    }

    public void actualizarEstado(int idBus, String nuevoEstado) throws SQLException {
        String sql = "UPDATE buses SET estado = ? WHERE id_bus = ?";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nuevoEstado);
            ps.setInt(2, idBus);
            ps.executeUpdate();
        }
    }

    /** Inserta una unidad nueva. Nace en estado SIN_CONDUCTOR (aparece en "Inactivas" hasta que se active). */
    public void crearBus(String codigo, String placa, String modelo, int anio, int capacidad, int idEmpresa) throws SQLException {
        String sql = "INSERT INTO buses (codigo, placa, modelo, anio_fabricacion, capacidad_pasajeros, id_empresa, estado) "
                + "VALUES (?, ?, ?, ?, ?, ?, 'SIN_CONDUCTOR')";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, codigo);
            ps.setString(2, placa);
            ps.setString(3, modelo);
            ps.setInt(4, anio);
            ps.setInt(5, capacidad);
            ps.setInt(6, idEmpresa);
            ps.executeUpdate();
        }
    }

    /** Activa una unidad inactiva: le asigna una ruta y la pone en circulación (estado A_TIEMPO). */
    public void activarUnidad(int idBus, int idRuta) throws SQLException {
        String sql = "UPDATE buses SET estado = 'A_TIEMPO', id_ruta = ?, velocidad_actual = 0, "
                + "ocupacion_pct = 0, confianza_ml_pct = 90, retraso_min = 0 WHERE id_bus = ?";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idRuta);
            ps.setInt(2, idBus);
            ps.executeUpdate();
        }
    }

    /** Retira una unidad de circulación (la pasa a Inactivas), quitándole la ruta asignada. */
    public void desactivarUnidad(int idBus, String motivoEstado) throws SQLException {
        String sql = "UPDATE buses SET estado = ?, id_ruta = NULL, velocidad_actual = 0, ocupacion_pct = 0 WHERE id_bus = ?";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, motivoEstado);
            ps.setInt(2, idBus);
            ps.executeUpdate();
        }
    }

    private Bus mapear(ResultSet rs) throws SQLException {
        Bus b = new Bus();
        b.setIdBus(rs.getInt("id_bus"));
        b.setCodigo(rs.getString("codigo"));
        b.setPlaca(rs.getString("placa"));
        b.setModelo(rs.getString("modelo"));
        b.setIdRuta(rs.getInt("id_ruta"));
        b.setCodigoRuta(rs.getString("codigo_ruta"));
        b.setNombreRuta(rs.getString("nombre_ruta"));
        b.setNombreConductor(rs.getString("nombre_conductor"));
        b.setEstado(rs.getString("estado"));
        b.setUbicacionActual(rs.getString("ubicacion_actual"));
        b.setVelocidadActual(rs.getDouble("velocidad_actual"));
        b.setOcupacionPct(rs.getInt("ocupacion_pct"));
        b.setConfianzaMlPct(rs.getInt("confianza_ml_pct"));
        b.setRetrasoMin(rs.getInt("retraso_min"));
        return b;
    }
}
