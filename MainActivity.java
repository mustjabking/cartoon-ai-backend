package com.example.duckaiwebview;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.webView);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);

        // Custom WebViewClient jo headers ko modify karega
        webView.setWebViewClient(new WebViewClient() {
            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                try {
                    OkHttpClient client = new OkHttpClient();
                    Request okRequest = new Request.Builder().url(url).build();
                    Response response = client.newCall(okRequest).execute();

                    // X-Frame-Options aur Frame-Options headers ko hatao
                    Response modifiedResponse = response.newBuilder()
                            .removeHeader("x-frame-options")
                            .removeHeader("frame-options")
                            .build();

                    // Response headers ko WebResourceResponse mein daalo
                    Map<String, String> headers = new HashMap<>();
                    for (String name : modifiedResponse.headers().names()) {
                        headers.put(name, modifiedResponse.header(name));
                    }

                    return new WebResourceResponse(
                            modifiedResponse.header("Content-Type", "text/html"),
                            modifiedResponse.header("Content-Encoding", "utf-8"),
                            modifiedResponse.code(),
                            modifiedResponse.message(),
                            headers,
                            modifiedResponse.body().byteStream()
                    );
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                    return null;
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }
        });

        // Local HTML file load karo (assets folder se)
        webView.loadUrl("file:///android_asset/index.html");
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
