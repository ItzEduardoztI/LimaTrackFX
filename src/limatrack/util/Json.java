package limatrack.util;

import java.util.List;
import java.util.function.Function;

/**
 * Constructor de JSON muy simple, sin dependencias externas, suficiente
 * para enviar datos desde Java hacia el JavaScript del WebView.
 */
public final class Json {

    private Json() { }

    public static String esc(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            switch (c) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (c < 0x20) sb.append(String.format("\\u%04x", (int) c));
                    else sb.append(c);
            }
        }
        return sb.toString();
    }

    public static String str(String s) {
        return s == null ? "null" : "\"" + esc(s) + "\"";
    }

    /** Construye un objeto JSON simple a partir de pares de campo -> valor JSON ya formateado. */
    public static class Obj {
        private final StringBuilder sb = new StringBuilder("{");
        private boolean primero = true;

        public Obj campo(String nombre, String valorJson) {
            if (!primero) sb.append(",");
            sb.append("\"").append(nombre).append("\":").append(valorJson);
            primero = false;
            return this;
        }

        public Obj campoStr(String nombre, String valor) { return campo(nombre, str(valor)); }
        public Obj campoNum(String nombre, Number valor) { return campo(nombre, valor == null ? "null" : valor.toString()); }
        public Obj campoBool(String nombre, boolean valor) { return campo(nombre, String.valueOf(valor)); }

        @Override
        public String toString() {
            return sb.toString() + "}";
        }
    }

    public static <T> String array(List<T> lista, Function<T, String> mapper) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < lista.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(mapper.apply(lista.get(i)));
        }
        sb.append("]");
        return sb.toString();
    }
}
