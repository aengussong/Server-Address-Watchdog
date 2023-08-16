package com.buttstuff.localserverwatchdog

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.lifecycleScope
import com.buttstuff.localserverwatchdog.domain.WatchdogManager
import com.buttstuff.localserverwatchdog.ui.theme.LocalServerWatchdogTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val permissionHandler = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { map ->
        if (map.values.all { true }) {
            startWatchdog()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LocalServerWatchdogTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    Greeting("Android")
                }
            }
        }

        val am = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val permissions = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !NotificationManagerCompat.from(this).areNotificationsEnabled()) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !am.canScheduleExactAlarms()) {
            permissions.add(Manifest.permission.SCHEDULE_EXACT_ALARM)
        } else {
            startWatchdog()
        }

        if (permissions.isNotEmpty()) {
            permissionHandler.launch(permissions.toTypedArray())
        }
    }

    private fun startWatchdog() = lifecycleScope.launch {
        WatchdogManager.getInstance().checkServer(this@MainActivity)
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    LocalServerWatchdogTheme {
        Greeting("Android")
    }
}