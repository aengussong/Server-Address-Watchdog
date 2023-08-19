package com.buttstuff.localserverwatchdog.ui.screen.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.buttstuff.localserverwatchdog.R

@Composable
fun SettingsScreen(settingsVM: SettingsVM = viewModel(), onEditAddress: () -> Unit, onEditInterval: () -> Unit) {
    val screenState by settingsVM.screenState.collectAsState()

    Column(
        Modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = CenterVertically,
            modifier = Modifier
                .padding(top = 40.dp)
                .fillMaxWidth()
                .clickable { onEditAddress() }
        ) {
            Text(text = screenState.serverAddress, style = MaterialTheme.typography.displaySmall)
            Icon(modifier = Modifier.padding(start = 8.dp), imageVector = Icons.Sharp.Edit, contentDescription = null)
        }

        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = CenterVertically,
            modifier = Modifier
                .padding(top = 40.dp)
                .fillMaxWidth()
                .clickable { onEditInterval() }
        ) {
            val hoursIndicator = stringResource(R.string.hours)
            val minutesIndicator = stringResource(R.string.minutes)
            val intervalText =
                "${screenState.interval.hours}$hoursIndicator ${screenState.interval.minutes}$minutesIndicator"

            Text(text = intervalText, style = MaterialTheme.typography.displaySmall)
            Icon(
                modifier = Modifier.padding(start = 8.dp),
                imageVector = Icons.Sharp.Edit,
                contentDescription = null
            )
        }

        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = CenterVertically,
            modifier = Modifier
                .padding(top = 40.dp)
                .fillMaxWidth()
        ) {
            Checkbox(
                checked = screenState.runOnlyOnWifi,
                onCheckedChange = { isChecked ->
                    settingsVM.updateListenOnlyOverWifi(isChecked)
                })

            Text(
                text = stringResource(id = R.string.hint_watch_only_over_wifi),
                textAlign = TextAlign.Right,
                modifier = Modifier.align(CenterVertically)
            )
        }

        val isWatchdogRunning = screenState.isWatchdogCurrentlyRunning
        val text = if (!isWatchdogRunning) R.string.btn_start_watchdog else R.string.btn_stop_watchdog

        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp)
                .padding(horizontal = 16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            onClick = { settingsVM.updateWatchdogStatus(!isWatchdogRunning) }) {
            Text(
                modifier = Modifier
                    .align(CenterVertically)
                    .padding(vertical = 16.dp),
                textAlign = TextAlign.Center,
                text = stringResource(text),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                style = MaterialTheme.typography.headlineSmall
            )
        }

        LaunchedEffect(Unit) {
            settingsVM.updateData()
        }
    }
}
