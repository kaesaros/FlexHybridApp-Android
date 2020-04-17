package app.dvkyun.flexhybrid

import android.graphics.Bitmap
import android.webkit.WebView
import android.webkit.WebViewClient

class FlexWebViewClient: WebViewClient() {

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        if(view is FlexWebView && url != null && url.contains(view.baseUrl!!)) {
            FlexStatic.evaluateJavaScript(view, view.flexJsString)
        }
    }

    override fun doUpdateVisitedHistory(view: WebView?, url: String?, isReload: Boolean) {
        super.doUpdateVisitedHistory(view, url, isReload)
    }

}
