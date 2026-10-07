package limatrack.modelo;

public class Usuario {
    private int idUsuario;
    private String nombreCompleto;
    private String correo;
    private String rol;          // OPERADOR, CONDUCTOR, ADMIN
    private String iniciales;
    private int idEmpresa;
    private String nombreEmpresa;

    public Usuario() { }

    public Usuario(int idUsuario, String nombreCompleto, String correo, String rol,
                    String iniciales, int idEmpresa, String nombreEmpresa) {
        this.idUsuario = idUsuario;
        this.nombreCompleto = nombreCompleto;
        this.correo = correo;
        this.rol = rol;
        this.iniciales = iniciales;
        this.idEmpresa = idEmpresa;
        this.nombreEmpresa = nombreEmpresa;
    }

    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }

    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }

    public String getPrimerNombre() {
        if (nombreCompleto == null || nombreCompleto.isBlank()) return "";
        return nombreCompleto.trim().split(" ")[0];
    }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }

    public String getIniciales() { return iniciales; }
    public void setIniciales(String iniciales) { this.iniciales = iniciales; }

    public int getIdEmpresa() { return idEmpresa; }
    public void setIdEmpresa(int idEmpresa) { this.idEmpresa = idEmpresa; }

    public String getNombreEmpresa() { return nombreEmpresa; }
    public void setNombreEmpresa(String nombreEmpresa) { this.nombreEmpresa = nombreEmpresa; }

    public String getRolLegible() {
        switch (rol) {
            case "OPERADOR": return "Operador";
            case "CONDUCTOR": return "Conductor";
            case "ADMIN": return "Administrador";
            default: return rol;
        }
    }
}
