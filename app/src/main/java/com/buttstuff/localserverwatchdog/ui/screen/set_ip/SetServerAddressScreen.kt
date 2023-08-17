package com.buttstuff.localserverwatchdog.ui.screen.set_ip

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.buttstuff.localserverwatchdog.R
import com.buttstuff.localserverwatchdog.util.isError
import com.buttstuff.localserverwatchdog.util.isSuccess

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetServerAddressScreen(serverAddressViewModel: SetServerAddressViewModel = viewModel(), onSet: () -> Unit) {
    val screenState by serverAddressViewModel.screenState.collectAsState()

    val focusManager = LocalFocusManager.current

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        TextField(value = screenState.serverAddress, onValueChange = { value ->
            serverAddressViewModel.onServerAddressInputChanged(value)
        }, label = {
            Text(text = stringResource(id = R.string.hint_enter_server_address))
        },
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(top = 40.dp)
                .fillMaxWidth(),
            isError = screenState.validationResult.isError(),
            supportingText = {
                val validation = screenState.validationResult
                if (validation.isError()) {
                    Text(text = stringResource(validation.message), color = MaterialTheme.colorScheme.error)
                }
            },
            singleLine = true,
            placeholder = { Text(text = stringResource(R.string.placeholder_server_address)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {
                //todo should clear keyboard focus to hide keyboard?
                serverAddressViewModel.validateServerAddress()
            })
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            modifier = Modifier
                .padding(all = 16.dp)
                .align(Alignment.End),
            onClick = { serverAddressViewModel.validateServerAddress() }) {
            Text(text = stringResource(R.string.confirm))
        }
    }

    AnimatedVisibility(visible = screenState.isProgressShown) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }

    if (screenState.validationResult.isSuccess()) {
        onSet()
    }

    LaunchedEffect(Unit) {
        // show keyboard on screen enter
        focusManager.moveFocus(FocusDirection.Down)
    }
}
