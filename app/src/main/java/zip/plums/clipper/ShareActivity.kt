package zip.plums.clipper

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.preference.PreferenceManager

class ShareActivity : ComponentActivity() {
    var text = ""
    lateinit var cm: ClipboardManager

    lateinit var sharedPreferences: SharedPreferences

    var displayPosition = "bottom"
    var delimiter = "space"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (
            intent.action == Intent.ACTION_SEND
            && intent.type?.startsWith("text/") == true
        ) {
            setContent {
                cm = LocalClipboardManager.current
                HandleSendText(intent) // Handle text being sent
            }
        } else if (intent.action == Intent.ACTION_SEND_MULTIPLE) {
            Log.d("main", "-----------------")
            Log.d("main", "received multiple")
            Log.d("main", "-----------------")
        } else if (intent.action == Intent.ACTION_SENDTO) {
            Log.d("main", "-----------------")
            Log.d("main", "received sendTo")
            Log.d("main", "-----------------")
        }

        sharedPreferences =  PreferenceManager.getDefaultSharedPreferences(this /* Activity context */)
    }

    @Composable
    private fun HandleSendText(intent: Intent) {

        delimiter = sharedPreferences.getString("delimiter", "space")?: "space"

        Log.d("main", "-----------------")
        Log.d("main", "EXTRA_TITLE")
        Log.d("main", intent.getStringExtra(Intent.EXTRA_TITLE).toString())
        Log.d("main", "EXTRA_SUBJECT")
        Log.d("main", intent.getStringExtra(Intent.EXTRA_SUBJECT).toString())
        Log.d("main", "EXTRA_TEXT")
        Log.d("main", intent.getStringExtra(Intent.EXTRA_TEXT).toString())
        Log.d("main", "-----------------")


        val extras = intent.extras
        if (extras != null) {
            Log.d("TAG", "Intentのキー確認")
            val iterator = extras.keySet().iterator()
            while (iterator.hasNext()) {
                val key = iterator.next()
                Log.d("TAG", key)
            }
        }

        Log.d("TAG", delimiter)

        val delimiterText = when (delimiter) {
            "space" -> " "
            "crlf" -> "\r\n"
            "cr" -> "\n"
            "lf" -> "\n"
            "space_pipe" -> " | "
            else -> {" "}
        }

        val title = intent.getStringExtra(Intent.EXTRA_TITLE)
        val subject = intent.getStringExtra(Intent.EXTRA_SUBJECT)
        val body = intent.getStringExtra(Intent.EXTRA_TEXT)

        if (title != null && subject != null) {
            text = if (title != subject) {
                "$title$delimiterText$subject"
            } else {
                title
            }
        } else {
            title?.let { text = title }
            subject?.let { text = subject }
        }

        body?.let {
            if (text != "") {
                text += "$delimiterText$body"
            } else {
                text = body
            }
        }

        DisplayParts()
    }

    @Preview
    @Composable
    fun DisplayParts() {
        val textOrig = text.toString()
        var text by remember { mutableStateOf(text) }

        val focusRequester = remember { FocusRequester() }

        displayPosition = sharedPreferences.getString("position", "bottom")?: "bottom"

        Column(
            modifier = Modifier
                .systemBarsPadding()
                .windowInsetsPadding(WindowInsets.safeDrawing)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.End,

                verticalArrangement = when (displayPosition) {
                    "top" -> Arrangement.Top
                    "bottom" -> Arrangement.Bottom
                    else -> {
                        Arrangement.Bottom
                    }
                }

            ) {
                TextField(
                    value = text,
                    onValueChange = { text = it },
                    modifier = Modifier
                        .focusRequester(focusRequester)
                        .fillMaxWidth()
                )
                LaunchedEffect(Unit) {
                    focusRequester.requestFocus()
                }
                Row {
                    Button(
                        onClick = {
                            intent = Intent(Intent.ACTION_SEND)
                            intent.setType("text/plain")
                            intent.putExtra(Intent.EXTRA_TEXT,text)
                            startActivity(intent)
                        }) {
                        Icon(Icons.Filled.Share, "share")
                    }
                    Button(
                        onClick = { text = textOrig }) {
                        Icon(Icons.Filled.Refresh, "restore")
                    }
                    Button(
                        onClick = {
                            cm.setText(AnnotatedString.Builder(text).toAnnotatedString())
                            makeToast()
                            finish()
                        }) {
                        Icon(Icons.Filled.Done, "copy")
                    }
                }
            }
        }
    }

    fun makeToast() {
        Toast.makeText(
            this,
            "クリップボードにコピーしました",
            Toast.LENGTH_SHORT
        ).show()
    }
}