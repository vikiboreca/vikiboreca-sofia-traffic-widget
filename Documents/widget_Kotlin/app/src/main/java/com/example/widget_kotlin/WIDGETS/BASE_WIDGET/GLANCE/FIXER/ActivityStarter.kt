package com.example.widget_kotlin.WIDGETS.BASE_WIDGET.GLANCE.FIXER

import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import com.example.widget_kotlin.WIDGETS.BASE_WIDGET.COMPOSE.PopUpActivity

class ActivityStarter(val activityClass: Class<out ComponentActivity>){
     fun startIntent(context:Context){
        val intent = android.content.Intent(context, activityClass).apply {
            flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
         Log.d("nigger", "startedActivity of type ${activityClass.simpleName}")
    }

    companion object{
        @Composable
        fun startResultActivity(pass:()->Unit, fail:()->Unit = {}, onExtra: ((Intent?) -> Unit)? = null): ManagedActivityResultLauncher<Intent, ActivityResult> {
            val launcher = rememberLauncherForActivityResult(
                ActivityResultContracts.StartActivityForResult()
            ) { result ->
                Log.d("LAUNCHER_DEBUG", "launcher CREATED")
                Log.d("RESULT_DEBUG", "code=${result.resultCode}")
                Log.d("RESULT_DEBUG", "data=${result.data}")
                Log.d("RESULT_DEBUG", "success=${result.data?.getBooleanExtra("success", false)}")
                if (result.resultCode == RESULT_OK) {
                    val success =
                        result.data?.getBooleanExtra("success", false) ?: false
                    if (success) {
                        onExtra?.invoke(result.data)
                        pass()
                    }
                    else{
                        fail()
                    }
                }
                else if(result.resultCode == RESULT_CANCELED){
                    fail()
                }
            }
            return launcher
        }
    }

}
