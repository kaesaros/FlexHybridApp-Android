package app.dvkyun.flexhybridand

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import android.util.Log
import android.webkit.WebView
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.InputStream
import java.io.Reader
import java.util.regex.Pattern

object FlexUtil {

    fun convertJSONArray(value: JSONArray): Array<Any?> {
        return Array(value.length())
        { i ->
            val element = value[i]
            if (element is Int || element is Double || element is Boolean || element is String) {
                element
            } else if (element is JSONArray) {
                convertJSONArray(element)
            } else if (element is JSONObject) {
                convertJSONObject(element)
            } else if (element == null) {
                null
            } else {
                throw FlexException(FlexException.ERROR3)
            }
        }
    }

    fun convertJSONObject(value: JSONObject): Map<*,*> {
        val result = HashMap<String, Any?>()
        value.keys().forEach {
            if (value.isNull(it)) {
                result[it] = null
            } else {
                val element = value[it]
                if (element is Int || element is Double || element is Boolean || element is String) {
                    result[it] = element
                } else if (element is JSONArray) {
                    result[it] = convertJSONArray(element)
                } else if (element is JSONObject) {
                    result[it] = convertJSONObject(element)
                } else {
                    throw FlexException(FlexException.ERROR3)
                }
            }
        }
        return result
    }

    internal fun getActivity(context: Context): Activity? {
        if (context is ContextWrapper) {
            return if (context is Activity) {
                context
            } else {
                getActivity(context.baseContext)
            }
        }
        return null
    }

    internal fun evaluateJavaScript(webView: WebView?, javascript: String) {
        if(webView == null) {
            throw FlexException(FlexException.ERROR4)
        }
        webView.post {
            val js = "javascript:$javascript"
            if (Build.VERSION.SDK_INT >= 19) {
                webView.evaluateJavascript(js, null)
            } else {
                webView.loadUrl(js)
            }
        }
    }

    internal fun convertValue(value: Any?): String {
        return if (value is Int || value is Long || value is Double || value is Float || value is Boolean) {
            "$value"
        } else if (value is String || value is Char) {
            "'${value}'"
        } else if (value is Array<*>) {
            val vString = StringBuilder()
            vString.append("[")
            value.forEach {
                if (it == null) {
                    vString.append("null,")
                } else if (it is Int || it is Long || it is Double || it is Float || it is Boolean) {
                    vString.append("${it},")
                } else if (it is String || it is Char) {
                    vString.append("'${it}',")
                } else if (it is Array<*> || it is Iterable<*> || it is Map<*,*> || it is JSONArray || it is JSONObject) {
                    vString.append("${convertValue(it)},")
                } else {
                    throw FlexException(FlexException.ERROR3)
                }
            }
            vString.append("]")
            vString.toString()
        } else if (value is Iterable<*>) {
            val vString = StringBuilder()
            vString.append("[")
            value.forEach {
                if (it == null) {
                    vString.append("null,")
                } else if (it is Int || it is Long || it is Double || it is Float || it is Boolean) {
                    vString.append("${it},")
                } else if (it is String || it is Char) {
                    vString.append("'${it}',")
                } else if (it is Array<*> || it is Iterable<*> || it is Map<*,*> || it is JSONArray || it is JSONObject) {
                    vString.append("${convertValue(it)},")
                } else {
                    throw FlexException(FlexException.ERROR3)
                }
            }
            vString.append("]")
            vString.toString()
        } else if (value is JSONArray) {
            val vString = StringBuilder()
            vString.append("[")
            for(i in 0 until value.length()) {
                val element = value[i]
                if (element == null) {
                    vString.append("null,")
                } else if (element is Int || element is Long || element is Double || element is Float || element is Boolean) {
                    vString.append("${element},")
                } else if (element is String || element is Char) {
                    vString.append("'${element}',")
                } else if (element is Array<*> || element is Iterable<*> || element is Map<*,*> || element is JSONArray || element is JSONObject) {
                    vString.append("${convertValue(element)},")
                } else {
                    throw FlexException(FlexException.ERROR3)
                }
            }
            vString.append("]")
            vString.toString()
        } else if (value is Map<*,*>) {
            val vString = StringBuilder()
            vString.append("{")
            value.forEach {
                if (it.key !is String) {
                    throw FlexException(FlexException.ERROR3)
                }
                if (it.value == null) {
                    vString.append("${it.key}: null,")
                } else if (it.value is Int || it.value is Long || it.value is Double || it.value is Float || it.value is Boolean || it.value == null) {
                    vString.append("${it.key}:${it.value},")
                } else if (it.value is String || it.value is Char) {
                    vString.append("${it.key}:'${it.value}',")
                } else if (it.value is Array<*> || it.value is Iterable<*> || it.value is Map<*,*> || it.value is JSONArray || it.value is JSONObject) {
                    vString.append("${it.key}:${convertValue(it.value!!)},")
                } else {
                    throw FlexException(FlexException.ERROR3)
                }
            }
            vString.append("}")
            vString.toString()
        } else if (value is JSONObject) {
            val vString = StringBuilder()
            vString.append("{")
            value.keys().forEach {
                if(value.isNull(it)) {
                    vString.append("${it}: null,")
                } else {
                    val element = value[it]
                    if (element is Int || element  is Long || element  is Double || element is Float || element  is Boolean) {
                        vString.append("${it}:${element},")
                    } else if (element is String || element is Char) {
                        vString.append("${it}:'${element}',")
                    } else if (element is Array<*> || element is Iterable<*> || element is Map<*,*> || element is JSONArray || element is JSONObject) {
                        vString.append("${it}:${convertValue(element)},")
                    } else {
                        throw FlexException(FlexException.ERROR3)
                    }
                }
            }
            vString.append("}")
            vString.toString()
        } else if(value == null) {
            "null"
        } else {
            throw FlexException(FlexException.ERROR3)
        }
    }

    private const val TAG = "FLEXHYBRID"

    internal fun INFO(msg: Any?) {
        android.util.Log.i(TAG, msg.toString())
    }

    internal fun DEBUG(msg: Any?) {
        android.util.Log.d(TAG, msg.toString())
    }

    internal fun VERBOSE(msg: Any?) {
        android.util.Log.v(TAG, msg.toString())
    }

    internal fun WARN(msg: Any?) {
        android.util.Log.w(TAG, msg.toString())
    }

    internal fun ERR(msg: Any?) {
        android.util.Log.e(TAG, msg.toString())
    }

    internal fun fileToString(inputStream: InputStream): String {
        try {
            val bufferedReader = BufferedReader(inputStream.reader())
            val sb = StringBuilder()
            var line: String?
            while (bufferedReader.readLine().also { line = it } != null) {
                sb.append(line).append("\n")
            }
            bufferedReader.close()
            return sb.toString()
        } catch (e: java.lang.Exception) {
            throw FlexException(e)
        }
    }

    internal fun fileToString(reader: Reader): String {
        try {
            val bufferedReader = BufferedReader(reader)
            val sb = StringBuilder()
            var line: String?
            while (bufferedReader.readLine().also { line = it } != null) {
                sb.append(line).append("\n")
            }
            bufferedReader.close()
            return sb.toString()
        } catch (e: java.lang.Exception) {
            throw FlexException(e)
        }
    }

}