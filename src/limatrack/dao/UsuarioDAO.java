package limatrack.dao;

import limatrack.db.Conexion;
import limatrack.modelo.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UsuarioDAO {

    /**
     * Autentica un usuario por correo, contraseña y rol seleccionado en el login.
     * Retorna el objeto Usuario si las credenciales son correctas, o null si no.
     */
    public Usuario autenticar(String correo, String contrasena, String rol) throws SQLException {
        String sql = "SELECT u.id_usuario, u.nombre_completo, u.correo, u.rol, u.iniciales, "
                + "u.id_empresa, e.nombre AS nombre_empresa "
                + "FROM usuarios u JOIN empresas e ON e.id_empresa = u.id_empresa "
                + "WHERE u.correo = ? AND u.contrasena = ? AND u.rol = ? AND u.activo = 1";

        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, correo);
            ps.setString(2, contrasena);
            ps.setString(3, rol);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }
        }
        return null;
    }

    private Usuario mapear(ResultSet rs) throws SQLException {
        Usuario u = new Usuario();
        u.setIdUsuario(rs.getInt("id_usuario"));
        u.setNombreCompleto(rs.getString("nombre_completo"));
        u.setCorreo(rs.getString("correo"));
        u.setRol(rs.getString("rol"));
        u.setIniciales(rs.getString("iniciales"));
        u.setIdEmpresa(rs.getInt("id_empresa"));
        u.setNombreEmpresa(rs.getString("nombre_empresa"));
        return u;
    }
}
