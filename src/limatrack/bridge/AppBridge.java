package limatrack.bridge;

import javafx.application.Platform;
import javafx.scene.web.WebEngine;
import limatrack.dao.AlertaDAO;
import limatrack.dao.BusDAO;
import limatrack.dao.MetricaDAO;
import limatrack.dao.ParaderoDAO;
import limatrack.dao.PosicionDAO;
import limatrack.dao.RutaDAO;
import limatrack.dao.UsuarioDAO;
import limatrack.modelo.Alerta;
import limatrack.modelo.Bus;
import limatrack.modelo.DatoDemanda;
import limatrack.modelo.MetricaDiaria;
import limatrack.modelo.Paradero;
import limatrack.modelo.PosicionGps;
import limatrack.modelo.Ruta;
import limatrack.modelo.Usuario;
import limatrack.util.Json;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Objeto Java que se inyecta como "window.app" dentro del WebView.
 * Cada método puede ser llamado directamente desde el JavaScript de
 * login.html y dashboard.html, y devuelve JSON en texto plano.
 */
public class AppBridge {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private final WebEngine engine;
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final BusDAO busDAO = new BusDAO();
    private final RutaDAO rutaDAO = new RutaDAO();
    private final AlertaDAO alertaDAO = new AlertaDAO();
    private final MetricaDAO metricaDAO = new MetricaDAO();
    private final PosicionDAO posicionDAO = new PosicionDAO();
    private final ParaderoDAO paraderoDAO = new ParaderoDAO();

    private Usuario usuarioActual;

    public AppBridge(WebEngine engine) {
        this.engine = engine;
    }

    // ------------------------------------------------------------
    // NAVEGACIÓN entre páginas HTML
    // ------------------------------------------------------------
    public void irADashboard() {
        Platform.runLater(() -> engine.load(getClass().getResource("/limatrack/web/dashboard.html").toExternalForm()));
    }

    public void irARutas() {
        Platform.runLater(() -> engine.load(getClass().getResource("/limatrack/web/rutas.html").toExternalForm()));
    }

    public void cerrarSesion() {
        usuarioActual = null;
        Platform.runLater(() -> engine.load(getClass().getResource("/limatrack/web/login.html").toExternalForm()));
    }

    // ------------------------------------------------------------
    // LOGIN
    // ------------------------------------------------------------
    public String autenticar(String correo, String pass, String rol) {
        try {
            Usuario u = usuarioDAO.autenticar(correo, pass, rol);
            if (u == null) {
                return new Json.Obj().campoBool("ok", false)
                        .campoStr("mensaje", "Correo, contraseña o rol incorrectos.").toString();
            }
            usuarioActual = u;
            return new Json.Obj().campoBool("ok", true).campo("usuario", usuarioJson(u)).toString();
        } catch (SQLException e) {
            return new Json.Obj().campoBool("ok", false)
                    .campoStr("mensaje", "Error de conexión con la base de datos: " + e.getMessage()).toString();
        }
    }

    public String usuarioActualJson() {
        if (usuarioActual == null) {
            return new Json.Obj().campoBool("ok", false).toString();
        }
        return new Json.Obj().campoBool("ok", true).campo("usuario", usuarioJson(usuarioActual)).toString();
    }

    private String usuarioJson(Usuario u) {
        return new Json.Obj()
                .campoNum("idUsuario", u.getIdUsuario())
                .campoStr("nombreCompleto", u.getNombreCompleto())
                .campoStr("primerNombre", u.getPrimerNombre())
                .campoStr("correo", u.getCorreo())
                .campoStr("rol", u.getRol())
                .campoStr("rolLegible", u.getRolLegible())
                .campoStr("iniciales", u.getIniciales())
                .campoNum("idEmpresa", u.getIdEmpresa())
                .campoStr("nombreEmpresa", u.getNombreEmpresa())
                .toString();
    }

    // ------------------------------------------------------------
    // DASHBOARD — datos
    // ------------------------------------------------------------
    public String listarBuses() {
        if (usuarioActual == null) return "[]";
        try {
            List<Bus> buses = busDAO.listarPorEmpresa(usuarioActual.getIdEmpresa());
            return Json.array(buses, this::busJson);
        } catch (SQLException e) {
            return "[]";
        }
    }

    private String busJson(Bus b) {
        return new Json.Obj()
                .campoNum("idBus", b.getIdBus())
                .campoStr("codigo", b.getCodigo())
                .campoStr("placa", b.getPlaca())
                .campoStr("modelo", b.getModelo())
                .campoStr("codigoRuta", b.getCodigoRuta())
                .campoStr("nombreRuta", b.getNombreRuta())
                .campoStr("conductor", b.getNombreConductor())
                .campoStr("estado", b.getEstado())
                .campoStr("estadoLegible", b.getEstadoLegible())
                .campoStr("ubicacion", b.getUbicacionActual())
                .campoNum("velocidad", b.getVelocidadActual())
                .campoNum("ocupacion", b.getOcupacionPct())
                .campoNum("confianzaMl", b.getConfianzaMlPct())
                .campoNum("retrasoMin", b.getRetrasoMin())
                .campoBool("activo", b.isActivo())
                .toString();
    }

    public String listarRutas() {
        if (usuarioActual == null) return "[]";
        try {
            List<Ruta> rutas = rutaDAO.listarEstadoPorEmpresa(usuarioActual.getIdEmpresa());
            return Json.array(rutas, this::rutaJson);
        } catch (SQLException e) {
            return "[]";
        }
    }

    public String listarBusesPorRuta(int idRuta) {
        if (usuarioActual == null) return "[]";
        try {
            List<Bus> todos = busDAO.listarPorEmpresa(usuarioActual.getIdEmpresa());
            List<Bus> filtrados = todos.stream().filter(b -> b.getIdRuta() == idRuta).toList();
            return Json.array(filtrados, this::busJson);
        } catch (SQLException e) {
            return "[]";
        }
    }

    private String rutaJson(Ruta r) {
        return new Json.Obj()
                .campoNum("idRuta", r.getIdRuta())
                .campoStr("codigo", r.getCodigo())
                .campoStr("nombre", r.getNombre())
                .campoStr("origen", r.getOrigen())
                .campoStr("destino", r.getDestino())
                .campoNum("totalBuses", r.getTotalBuses())
                .campoNum("cumplimientoPct", r.getCumplimientoPct())
                .campoStr("estadoGeneral", r.getEstadoGeneral())
                .toString();
    }

    /** Crea una ruta nueva para la empresa del usuario actual. Devuelve JSON con ok/mensaje. */
    public String crearRuta(String codigo, String nombre, String origen, String destino,
                             double distanciaKm, int tiempoEstimadoMin, double tarifa) {
        if (usuarioActual == null) {
            return new Json.Obj().campoBool("ok", false).campoStr("mensaje", "No hay sesión activa.").toString();
        }
        if (codigo == null || codigo.isBlank() || origen == null || origen.isBlank() || destino == null || destino.isBlank()) {
            return new Json.Obj().campoBool("ok", false).campoStr("mensaje", "El código, origen y destino son obligatorios.").toString();
        }
        try {
            String nombreFinal = (nombre == null || nombre.isBlank()) ? (origen.trim() + " → " + destino.trim()) : nombre.trim();
            int idNueva = rutaDAO.crearRuta(codigo.trim(), nombreFinal, origen.trim(), destino.trim(), distanciaKm, tiempoEstimadoMin, tarifa, usuarioActual.getIdEmpresa());
            if (idNueva > 0) {
                paraderoDAO.generarTiemposPorDefecto(idNueva, tiempoEstimadoMin);
            }
            return new Json.Obj().campoBool("ok", true).toString();
        } catch (SQLException e) {
            String msg = e.getMessage() != null && e.getMessage().toLowerCase().contains("duplicate")
                    ? "Ya existe una ruta con ese código."
                    : "Error al guardar en la base de datos: " + e.getMessage();
            return new Json.Obj().campoBool("ok", false).campoStr("mensaje", msg).toString();
        }
    }

    public String obtenerMetricas() {
        if (usuarioActual == null) return "{}";
        try {
            // Unidades y cumplimiento se calculan EN VIVO desde los datos reales de flota/rutas,
            // para que nunca dependan de una fecha guardada que pueda quedar "vieja".
            List<Bus> buses = busDAO.listarPorEmpresa(usuarioActual.getIdEmpresa());
            List<Ruta> rutas = rutaDAO.listarEstadoPorEmpresa(usuarioActual.getIdEmpresa());
            int activas = (int) buses.stream().filter(Bus::isActivo).count();
            int totales = buses.size();
            double cumplimientoPromedio = rutas.isEmpty() ? 0
                    : rutas.stream().mapToInt(Ruta::getCumplimientoPct).average().orElse(0);

            // Pasajeros/ETA/precisión ML se toman de la última fila disponible en metricas_diarias
            // (dato de referencia; aún no hay un contador de pasajeros en tiempo real).
            MetricaDiaria m = metricaDAO.obtenerMetricasHoy(usuarioActual.getIdEmpresa());

            return new Json.Obj()
                    .campoNum("unidadesActivas", activas)
                    .campoNum("unidadesTotales", totales)
                    .campoNum("cumplimientoPct", Math.round(cumplimientoPromedio * 10.0) / 10.0)
                    .campoNum("pasajerosTotales", m.getPasajerosTotales())
                    .campoNum("etaPromedioMin", m.getEtaPromedioMin())
                    .campoNum("precisionMlPct", m.getPrecisionMlPct())
                    .toString();
        } catch (SQLException e) {
            return "{}";
        }
    }

    public String listarDemanda() {
        if (usuarioActual == null) return "[]";
        try {
            List<DatoDemanda> datos = metricaDAO.obtenerDemandaHoy(usuarioActual.getIdEmpresa());
            return Json.array(datos, d -> new Json.Obj()
                    .campoNum("hora", d.getHora())
                    .campoStr("etiquetaHora", d.getEtiquetaHora())
                    .campoNum("pasajeros", d.getPasajeros())
                    .campoStr("nivel", d.getNivel())
                    .toString());
        } catch (SQLException e) {
            return "[]";
        }
    }

    public int contarAlertasPendientes() {
        if (usuarioActual == null) return 0;
        try {
            return alertaDAO.contarPendientesPorEmpresa(usuarioActual.getIdEmpresa());
        } catch (SQLException e) {
            return 0;
        }
    }

    public String listarAlertasPendientes() {
        if (usuarioActual == null) return "[]";
        try {
            List<Alerta> alertas = alertaDAO.listarPendientesPorEmpresa(usuarioActual.getIdEmpresa());
            return Json.array(alertas, a -> new Json.Obj()
                    .campoNum("idAlerta", a.getIdAlerta())
                    .campoNum("idBus", a.getIdBus())
                    .campoStr("codigoBus", a.getCodigoBus())
                    .campoStr("tipo", a.getTipo())
                    .campoStr("descripcion", a.getDescripcion())
                    .campoStr("severidad", a.getSeveridad())
                    .campoStr("fecha", a.getFechaHora() != null ? a.getFechaHora().format(FMT) : "")
                    .toString());
        } catch (SQLException e) {
            return "[]";
        }
    }

    public boolean marcarAlertaAtendida(int idAlerta) {
        if (usuarioActual == null) return false;
        try {
            alertaDAO.marcarAtendida(idAlerta, usuarioActual.getIdUsuario());
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public String historialBus(int idBus) {
        try {
            List<PosicionGps> historial = posicionDAO.historialPorBus(idBus, 20);
            return Json.array(historial, p -> new Json.Obj()
                    .campoNum("latitud", p.getLatitud())
                    .campoNum("longitud", p.getLongitud())
                    .campoNum("velocidad", p.getVelocidad())
                    .campoNum("ocupacion", p.getOcupacionPct())
                    .campoStr("fecha", p.getFechaHora() != null ? p.getFechaHora().format(FMT) : "")
                    .toString());
        } catch (SQLException e) {
            return "[]";
        }
    }

    public boolean cambiarEstadoBus(int idBus, String nuevoEstado) {
        try {
            busDAO.actualizarEstado(idBus, nuevoEstado);
            if (("DEMORADO".equals(nuevoEstado) || "DESVIO".equals(nuevoEstado)) && usuarioActual != null) {
                alertaDAO.registrarAlerta(idBus,
                        "DEMORADO".equals(nuevoEstado) ? "RETRASO" : "DESVIO",
                        "Cambio de estado registrado manualmente por " + usuarioActual.getNombreCompleto(),
                        "MEDIA");
            }
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    /** Crea una unidad nueva para la empresa del usuario actual. Devuelve JSON con ok/mensaje. */
    public String crearBus(String codigo, String placa, String modelo, int anio, int capacidad) {
        if (usuarioActual == null) {
            return new Json.Obj().campoBool("ok", false).campoStr("mensaje", "No hay sesión activa.").toString();
        }
        if (codigo == null || codigo.isBlank() || placa == null || placa.isBlank()) {
            return new Json.Obj().campoBool("ok", false).campoStr("mensaje", "El código y la placa son obligatorios.").toString();
        }
        try {
            busDAO.crearBus(codigo.trim(), placa.trim(), modelo == null ? "" : modelo.trim(), anio, capacidad, usuarioActual.getIdEmpresa());
            return new Json.Obj().campoBool("ok", true).toString();
        } catch (SQLException e) {
            String msg = e.getMessage() != null && e.getMessage().toLowerCase().contains("duplicate")
                    ? "Ya existe un bus con ese código o placa."
                    : "Error al guardar en la base de datos: " + e.getMessage();
            return new Json.Obj().campoBool("ok", false).campoStr("mensaje", msg).toString();
        }
    }

    /** Activa una unidad inactiva asignándole una ruta, para que empiece a circular. */
    public boolean activarUnidad(int idBus, int idRuta) {
        try {
            busDAO.activarUnidad(idBus, idRuta);
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    /** Retira una unidad de circulación (queda en "Inactivas") por mantenimiento o falta de conductor. */
    public boolean desactivarUnidad(int idBus, String motivoEstado) {
        try {
            busDAO.desactivarUnidad(idBus, motivoEstado);
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    // ------------------------------------------------------------
    // PARADEROS
    // ------------------------------------------------------------
    public String paraderosDeRuta(int idRuta) {
        try {
            List<Paradero> lista = paraderoDAO.paraderosDeRuta(idRuta);
            return Json.array(lista, p -> new Json.Obj()
                    .campoNum("idParadero", p.getIdParadero())
                    .campoStr("nombre", p.getNombre())
                    .campoStr("distrito", p.getDistrito())
                    .campoNum("orden", p.getOrden())
                    .toString());
        } catch (SQLException e) {
            return "[]";
        }
    }

    // ------------------------------------------------------------
    // CÁLCULO DE ETA (tiempo estimado de llegada)
    // Usa el promedio histórico de minutos por franja horaria (mañana/
    // tarde/noche) registrado para la ruta, y le suma el retraso en vivo
    // detectado en los buses que circulan por ella.
    // ------------------------------------------------------------
    public String calcularEta(int idRuta) {
        try {
            String franja = ParaderoDAO.franjaActual();
            int minutosBase = paraderoDAO.minutosPromedio(idRuta, franja);
            if (minutosBase < 0) {
                return new Json.Obj().campoBool("ok", false)
                        .campoStr("mensaje", "Aún no hay tiempos históricos registrados para esta ruta.").toString();
            }
            List<Bus> busesRuta = usuarioActual == null ? List.of()
                    : busDAO.listarPorEmpresa(usuarioActual.getIdEmpresa()).stream()
                            .filter(b -> b.getIdRuta() == idRuta && b.isActivo()).toList();
            int retrasoMax = busesRuta.stream().mapToInt(Bus::getRetrasoMin).max().orElse(0);
            String franjaLegible = switch (franja) {
                case "MANANA" -> "Mañana";
                case "TARDE" -> "Tarde (hora punta)";
                default -> "Noche";
            };
            return new Json.Obj().campoBool("ok", true)
                    .campoStr("franja", franjaLegible)
                    .campoNum("minutosBase", minutosBase)
                    .campoNum("retrasoActual", retrasoMax)
                    .campoNum("etaTotalMin", minutosBase + retrasoMax)
                    .toString();
        } catch (SQLException e) {
            return new Json.Obj().campoBool("ok", false).campoStr("mensaje", e.getMessage()).toString();
        }
    }

    // ------------------------------------------------------------
    // KILÓMETROS RECORRIDOS (a partir del historial de posiciones GPS)
    // ------------------------------------------------------------
    public double kmRecorridos(int idBus) {
        try {
            return Math.round(posicionDAO.calcularKmRecorridos(idBus) * 10.0) / 10.0;
        } catch (SQLException e) {
            return 0;
        }
    }

    // ------------------------------------------------------------
    // REPORTES
    // ------------------------------------------------------------
    public String generarReporte() {
        if (usuarioActual == null) return "{}";
        try {
            List<Ruta> rutas = rutaDAO.listarEstadoPorEmpresa(usuarioActual.getIdEmpresa());
            List<Bus> buses = busDAO.listarPorEmpresa(usuarioActual.getIdEmpresa());
            List<Alerta> alertas = alertaDAO.listarPendientesPorEmpresa(usuarioActual.getIdEmpresa());
            MetricaDiaria metrica = metricaDAO.obtenerMetricasHoy(usuarioActual.getIdEmpresa());
            double cumplimientoGeneral = rutas.isEmpty() ? 0
                    : rutas.stream().mapToInt(Ruta::getCumplimientoPct).average().orElse(0);

            long alertasAltas = alertas.stream().filter(a -> "ALTA".equals(a.getSeveridad())).count();
            long alertasMedias = alertas.stream().filter(a -> "MEDIA".equals(a.getSeveridad())).count();
            long alertasBajas = alertas.stream().filter(a -> "BAJA".equals(a.getSeveridad())).count();

            List<Bus> topRetraso = new ArrayList<>(buses);
            topRetraso.sort(Comparator.comparingInt(Bus::getRetrasoMin).reversed());
            List<Bus> topRetrasoTop3 = topRetraso.stream().filter(b -> b.getRetrasoMin() > 0).limit(3).toList();

            List<Object[]> conKm = new ArrayList<>();
            for (Bus b : buses) {
                double km = posicionDAO.calcularKmRecorridos(b.getIdBus());
                conKm.add(new Object[]{b, km});
            }
            conKm.sort((a, c) -> Double.compare((double) c[1], (double) a[1]));
            List<Object[]> topKm = conKm.stream().limit(3).toList();

            Json.Obj obj = new Json.Obj()
                    .campoBool("ok", true)
                    .campoNum("pasajerosTotales", metrica.getPasajerosTotales())
                    .campoNum("cumplimientoGeneral", Math.round(cumplimientoGeneral * 10.0) / 10.0)
                    .campoNum("alertasAltas", alertasAltas)
                    .campoNum("alertasMedias", alertasMedias)
                    .campoNum("alertasBajas", alertasBajas)
                    .campo("rutas", Json.array(rutas, this::rutaJson))
                    .campo("topRetraso", Json.array(topRetrasoTop3, b -> new Json.Obj()
                            .campoStr("codigo", b.getCodigo())
                            .campoStr("ruta", b.getCodigoRuta())
                            .campoNum("retrasoMin", b.getRetrasoMin())
                            .toString()))
                    .campo("topKm", Json.array(topKm, arr -> {
                        Bus b = (Bus) arr[0];
                        double km = (double) arr[1];
                        return new Json.Obj()
                                .campoStr("codigo", b.getCodigo())
                                .campoNum("km", Math.round(km * 10.0) / 10.0)
                                .toString();
                    }));
            return obj.toString();
        } catch (SQLException e) {
            return new Json.Obj().campoBool("ok", false).campoStr("mensaje", e.getMessage()).toString();
        }
    }
}
