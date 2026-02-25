package com.example.widget_kotlin.WIDGETS.BASE_WIDGET.COMPOSE

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

class DeleteList: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent{
            Input()
        }
    }

    @Composable
    fun Input() {
        var showDialog by remember { mutableStateOf(true) }

        MaterialTheme {
            Column {
                if(showDialog){
                    DeleteDialog({
                        val intent = Intent().apply { putExtra("success", false) }
                        setResult(Activity.RESULT_OK, intent)
                        finish()
                    })
                    {
                        val intent = Intent().apply { putExtra("success", true) }
                        setResult(Activity.RESULT_OK, intent)
                        finish()
                    }
                }
            }
        }
    }
    @Composable
    fun DeleteDialog(
        onDismiss: () -> Unit, onSuccess: () ->Unit
    ) {
        AlertDialog(
            onDismissRequest = onDismiss,
            containerColor = Color.White,
            title = {
                Text("Delete list?")
            },
            text = {
                Text("Are you sure you want to delete this list?")
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