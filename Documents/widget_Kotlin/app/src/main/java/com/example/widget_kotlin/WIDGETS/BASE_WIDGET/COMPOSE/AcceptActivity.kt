package com.example.widget_kotlin.WIDGETS.BASE_WIDGET.COMPOSE

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

class AcceptActivity: ComponentActivity() {
    companion object{
        const val TITLE = "TITLE"
        const val MESSAGE = "MESSAGE"

        const val CANCEL = "CANCEL"

        const val ACCEPT = "ACCEPT"

        fun createActivity(context: Context, title: String, message: String, cancel:String, accept:String):Intent{
            val intent = Intent(context, AcceptActivity::class.java).apply {
                putExtra(TITLE, title)
                putExtra(MESSAGE, message)
                putExtra(CANCEL, cancel)
                putExtra(ACCEPT, accept)
            }
            return intent
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val title = intent.getStringExtra(TITLE) ?: "Title"
        val message = intent.getStringExtra(MESSAGE) ?: "Message"
        val decline = intent.getStringExtra(CANCEL) ?: "Cancel"
        val accept = intent.getStringExtra(ACCEPT) ?: "Accept"

        setContent{
            Input(title, message, decline, accept)
        }
    }

    @Composable
    fun Input(title:String, message:String, decline:String, accept:String) {
        var showDialog by remember { mutableStateOf(true) }

        MaterialTheme {
            Column {
                if(showDialog){
                    DeleteDialog({
                        val intent = Intent().apply { putExtra("success", false) }
                        setResult(Activity.RESULT_OK, intent)
                        finish()
                    }, {
                        val intent = Intent().apply { putExtra("success", true) }
                        setResult(Activity.RESULT_OK, intent)
                        finish()
                    }, title, message, accept, decline)
                }
            }
        }
    }
    @Composable
    fun DeleteDialog(
        onDismiss: () -> Unit, onSuccess: () ->Unit, title: String, message: String, accept:String, decline:String
    ) {
        AlertDialog(
            onDismissRequest = onDismiss,
            containerColor = Color.White,
            title = {
                Text(title)
            },
            text = {
                Text(message)
            },
            confirmButton = {
                TextButton(onClick = onSuccess) {
                    Text(accept)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(decline)
                }
            }
        )
    }
}