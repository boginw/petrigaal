package petrigaal.app;

import javafx.concurrent.Worker;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import java.net.URL;
import java.util.Objects;

public class SvgViewer {
    private final WebEngine webEngine;
    private final WebView webView;

    public SvgViewer(String image) {
        webView = new WebView();
        webEngine = webView.getEngine();
        webEngine.setJavaScriptEnabled(true);

        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                loadImage(image);
            }
        });

        URL url = getClass().getResource("/web/index.html");
        webEngine.load(Objects.requireNonNull(url).toString());
    }

    public WebView getWebView() {
        return webView;
    }

    public void loadImage(String image) {
        webEngine.executeScript("loadImage('%s')".formatted(image));
    }
}
