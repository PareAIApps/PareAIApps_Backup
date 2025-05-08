package pnj.pk.pareaipk.ui.chatbot

import android.os.Bundle
import android.util.Log
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import pnj.pk.pareaipk.databinding.ActivityChatbotBinding

class ChatbotActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatbotBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatbotBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Sembunyikan ActionBar
        supportActionBar?.hide()

        // Set the action bar title
        supportActionBar?.title = "Chatbot Nara"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val webView = binding.webview
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.setSupportMultipleWindows(true)
        webView.settings.setSupportZoom(true)
        webView.settings.defaultTextEncodingName = "utf-8"

        // Enable content access and universal access to fix CORS issues
        webView.settings.allowContentAccess = true
        webView.settings.allowFileAccess = true
        webView.settings.setAllowFileAccessFromFileURLs(true)
        webView.settings.setAllowUniversalAccessFromFileURLs(true)

        // Set up WebChromeClient for logging JavaScript console messages
        webView.webChromeClient = object : WebChromeClient() {
            override fun onConsoleMessage(message: ConsoleMessage?): Boolean {
                Log.d("WebViewConsole", message?.message() ?: "No message")
                return super.onConsoleMessage(message)
            }
        }

        // Set up WebViewClient for handling errors and URL loading
        webView.webViewClient = object : WebViewClient() {
            override fun onReceivedError(
                view: WebView?, errorCode: Int, description: String?, failingUrl: String?
            ) {
                Log.e("WebViewError", "Error loading URL: $description")
                super.onReceivedError(view, errorCode, description, failingUrl)
            }

            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                // Prevent loading external URLs in the WebView
                return super.shouldOverrideUrlLoading(view, request)
            }
        }

        // Load the local HTML file or remote URL for the chatbot
        webView.loadUrl("file:///android_asset/chatbot.html")
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
