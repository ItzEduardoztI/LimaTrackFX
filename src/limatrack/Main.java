package limatrack;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import limatrack.bridge.AppBridge;
import netscape.javascript.JSObject;

/**
 * Punto de entrada de LimaTrack (versión JavaFX + WebView).
 * Carga los mismos archivos HTML/CSS/JS del prototipo original y los conecta
 * a MySQL a través del objeto "app" (ver limatrack.bridge.AppBridge).
 */
public class Main extends Application {

    @Override
    public void start(Stage stage) {
        WebView webView = new WebView();
        WebEngine engine = webView.getEngine();
        // UA moderno para que OpenStreetMap acepte las peticiones de tiles
        engine.setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/124.0 Safari/537.36");
        AppBridge bridge = new AppBridge(engine);

        // Cada vez que se termina de cargar una página (login.html o dashboard.html)
        // se vuelve a inyectar el puente Java, porque el objeto "window" se reinicia
        // con cada navegación.
        engine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                JSObject window = (JSObject) engine.executeScript("window");
                window.setMember("app", bridge);
            }
        });

        engine.load(getClass().getResource("/limatrack/web/login.html").toExternalForm());

        Scene scene = new Scene(webView, 1200, 720);
        stage.setTitle("LimaTrack — Sistema de Monitoreo de Flota");
        stage.setMinWidth(1000);
        stage.setMinHeight(620);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
        Platform.setImplicitExit(true);
        launch(args);
    }
}
