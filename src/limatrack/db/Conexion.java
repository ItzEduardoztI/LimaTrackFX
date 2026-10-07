package limatrack.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexion {

    private static final String HOST     = "localhost";
    private static final String PUERTO   = "3306";
    private static final String BD       = "limatrack_db";
    private static final String USUARIO  = "root";
    private static final String PASSWORD = "tu_contraseña"; // <-- tu contraseña real de MySQL

    private static final String URL =
            "jdbc:mysql://" + HOST + ":" + PUERTO + "/" + BD
            + "?useSSL=false&serverTimezone=America/Lima&allowPublicKeyRetrieval=true&characterEncoding=UTF-8";

    private static Connection conexion;

    private Conexion() { }

    /**
     * Devuelve una conexión activa. Si no existe o se cerró, crea una nueva.
     */
    public static Connection getConexion() throws SQLException {
        if (conexion == null || conexion.isClosed()) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
            } catch (ClassNotFoundException e) {
                throw new SQLException("No se encontró el driver de MySQL (mysql-connector-j). "
                        + "Agrega el .jar a las Librerías del proyecto en NetBeans.", e);
            }
            conexion = DriverManager.getConnection(URL, USUARIO, PASSWORD);
        }
        return conexion;
    }

    public static void cerrar() {
        try {
            if (conexion != null && !conexion.isClosed()) {
                conexion.close();
            }
        } catch (SQLException ignored) {
        }
    }
}
