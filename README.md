# LimaTrack FX — Versión JavaFX + WebView (diseño idéntico al HTML)

Esta es la nueva versión de LimaTrack: en vez de Swing, usa **JavaFX con un
componente WebView**, que carga tus archivos HTML/CSS/JS **originales, tal
cual**, con las mismas animaciones (el spinner de carga, el bus animado del
login, las transiciones de hover, etc.). Los datos ya no son falsos: vienen
de tu base de datos MySQL a través de un "puente" Java ↔ JavaScript.

> Ya tienes la base de datos `limatrack_db` creada de la versión anterior —
> **no necesitas volver a ejecutar el script SQL**, solo asegúrate de que
> siga existiendo en tu MySQL Workbench.

---

## 1. Qué cambia respecto a la versión Swing

| | Versión anterior (Swing) | Esta versión (JavaFX) |
|---|---|---|
| Interfaz | Dibujada con componentes Java nativos | Los mismos archivos `login.html` / `dashboard.html` que me pasaste, renderizados con el motor del navegador |
| Animaciones | No soportadas | Idénticas al HTML original (spinner, fadeUp, pulse, bus animado, transiciones) |
| Datos | Vía JDBC directo desde Java | Vía JDBC desde Java, expuestos a JavaScript como JSON |
| Login / dashboard | 2 ventanas Java (`LoginFrame`, `DashboardFrame`) | 1 ventana con `WebView`, que navega entre `login.html` y `dashboard.html` |

---

## 2. Descargar el SDK de JavaFX (paso nuevo, obligatorio)

JavaFX **ya no viene incluido con el JDK** desde hace varias versiones, hay que agregarlo aparte.

1. Ve a: **https://gluonhq.com/products/javafx/**
2. Elige tu versión de JDK (si no sabes cuál, en NetBeans ve a **Help → About** y mira la versión de Java).
3. Elige tu sistema operativo (Windows / macOS / Linux) y tipo **SDK**.
4. Descarga el `.zip` y descomprímelo en un lugar fijo, por ejemplo:
   - Windows: `C:\javafx-sdk-21.0.4`
   - macOS/Linux: `/home/tu_usuario/javafx-sdk-21.0.4`
5. Dentro encontrarás una carpeta `lib/` con varios archivos `javafx-*.jar` — **anota esa ruta completa**, la usarás dos veces más abajo.

---

## 3. Abrir el proyecto en NetBeans

1. Descomprime `LimaTrackFX-NetBeans.zip` en tu PC.
2. NetBeans → **File → Open Project...** → selecciona la carpeta `LimaTrackFX`.
3. Copia tu archivo `mysql-connector-j-X.X.X.jar` (el mismo que ya usaste antes) dentro de la carpeta `lib/` de este nuevo proyecto.
4. Clic derecho en **Libraries** → **Add JAR/Folder...** → selecciona ese `.jar` del conector MySQL.
5. Ahora agrega los de JavaFX: clic derecho en **Libraries** → **Add JAR/Folder...** → navega hasta la carpeta `lib/` **del SDK de JavaFX que descargaste** (no la del proyecto) → selecciona **todos** los archivos `javafx-*.jar` (puedes seleccionar varios con `Ctrl+clic` o `Shift+clic`) → **Open**.

   Deberías terminar viendo en "Libraries" algo como:
   ```
   javafx-base-21.0.4.jar
   javafx-controls-21.0.4.jar
   javafx-fxml-21.0.4.jar
   javafx-graphics-21.0.4.jar
   javafx-web-21.0.4.jar
   mysql-connector-j-X.X.X.jar
   ```

---

## 4. Configurar las opciones de ejecución (VM Options)

JavaFX necesita que le indiques, al ejecutar, dónde están sus módulos.

1. Clic derecho sobre el proyecto **LimaTrackFX** → **Properties**.
2. En el panel izquierdo, ve a **Run**.
3. En el campo **VM Options**, pega esto (cambia la ruta por la tuya, la misma del Paso 2):

   ```
   --module-path "C:\javafx-sdk-21.0.4\lib" --add-modules javafx.controls,javafx.web,javafx.fxml
   ```

   - En Mac/Linux, usa `/` normal: `--module-path "/home/usuario/javafx-sdk-21.0.4/lib" --add-modules javafx.controls,javafx.web,javafx.fxml`

4. Clic en **OK**.

---

## 5. Configurar tu contraseña de MySQL (otra vez, en este proyecto nuevo)

Como es un proyecto distinto, este paso hay que repetirlo aquí:

1. Abre `Source Packages → limatrack.db → Conexion.java`.
2. Cambia:
   ```java
   private static final String PASSWORD = "root"; // <-- tu contraseña de MySQL
   ```
3. Guarda (`Ctrl+S`).

---

## 6. Ejecutar

1. Clic derecho en el proyecto → **Run** (o `F6`).
2. Debería abrirse una ventana con el **login exactamente igual al HTML** que me pasaste: el mapa animado del bus, el spinner al iniciar sesión, todo.
3. Inicia sesión con cualquiera de los usuarios de siempre (contraseña `Lima2024`):
   - operador@lipetsa.pe
   - conductor@lipetsa.pe
   - admin@lipetsa.pe
4. Verás la pantalla de bienvenida (igual al HTML) → clic en **"Ir al panel principal"** → se abre el dashboard con tus datos reales de MySQL, con el mismo diseño oscuro y las mismas animaciones del prototipo.

---

## 7. Qué es funcional en esta versión

- Login y roles validados contra MySQL (igual que antes).
- Flota, rutas, métricas y gráfico de demanda cargados desde la base de datos.
- Clic en un bus de la barra lateral → actualiza el panel de detalle (velocidad, ocupación, confianza ML).
- Botón **"Cambiar estado"** → abre un menú y actualiza el estado del bus en la base de datos.
- Botón **"Notificar conductor"** y **"Ver historial"** (historial GPS real).
- Campana de notificaciones con contador de alertas pendientes → clic para ver el listado y marcarlas como atendidas.
- Clic en el avatar (arriba a la derecha) → cierra sesión y vuelve al login.
- Actualización automática cada 15 segundos (vuelve a consultar la base de datos).

---

## 8. Errores comunes

| Error | Causa | Solución |
|---|---|---|
| Pantalla en blanco / no abre ventana | Falta el `--module-path` en VM Options | Repite el Paso 4 |
| `Error: JavaFX runtime components are missing` | No agregaste los jars de JavaFX a Libraries, o el VM Options está mal escrito | Repite Pasos 3 y 4, revisa que la ruta no tenga errores de tipeo |
| "No se pudo conectar con la aplicación Java (window.app no disponible)" en el login | El WebView tardó en cargar antes de que Java inyectara el puente | Espera un segundo y vuelve a intentar iniciar sesión; si persiste, revisa la consola de NetBeans (pestaña "Output") por errores en rojo |
| Access denied de MySQL | Contraseña incorrecta en `Conexion.java` | Repite el Paso 5 |
| Librería en rojo en NetBeans | Nombre de archivo `.jar` no coincide con lo registrado | Vuelve a hacer clic derecho → Add JAR/Folder y selecciónalo de nuevo |

---

¿Se traba en algún paso? Copia el mensaje de error exacto (de la ventana emergente o de la pestaña "Output" de NetBeans) y lo revisamos juntos.
