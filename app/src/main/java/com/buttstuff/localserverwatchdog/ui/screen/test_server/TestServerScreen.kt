package com.buttstuff.localserverwatchdog.ui.screen.test_server

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.buttstuff.localserverwatchdog.R

@Composable
fun TestServerScreen(testServerVM: TestServerVM = viewModel()) {
    val screenState by testServerVM.screenState.collectAsState()

    val checkupResult = screenState.lastCheckupResult.takeIf { it.isNotBlank() }
        ?: stringResource(R.string.placeholder_empty_checkup_result)

    Text(
        text = stringResource(R.string.last_server_status, checkupResult),
        modifier = Modifier
            .padding(16.dp)
            .padding(top = 20.dp)
            .fillMaxWidth(),
        textAlign = TextAlign.Center
    )

    IconButton(
        onClick = { testServerVM.testServer() },
        modifier = Modifier
            .padding(horizontal = 40.dp)
            .padding(top = 60.dp)
            .fillMaxWidth()
            .fillMaxHeight()
            .aspectRatio(1f)
            .background(color = MaterialTheme.colorScheme.primaryContainer, shape = CircleShape)
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (screenState.isProgressShown) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f), color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Text(text = stringResource(R.string.ping_server), style = MaterialTheme.typography.displayLarge)
        }
    }
}
