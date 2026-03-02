package com.example.widget_kotlin.WIDGETS.BASE_WIDGET.COMPOSE

import android.app.Activity
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

class DeleteActivity: ComponentActivity() {
    companion object{
        const val TITLE = "TITLE"
        const val MESSAGE = "MESSAGE"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val title = intent.getStringExtra(TITLE) ?: "Delete"
        val message = intent.getStringExtra(MESSAGE) ?: "Are you sure you want to delete this"

        setContent{
            Input(title, message)
        }
    }

    @Composable
    fun Input(title:String, message:String) {
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
                    }, title, message)
                }
            }
        }
    }
    @Composable
    fun DeleteDialog(
        onDismiss: () -> Unit, onSuccess: () ->Unit, title: String, message: String
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
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        )
    }
}