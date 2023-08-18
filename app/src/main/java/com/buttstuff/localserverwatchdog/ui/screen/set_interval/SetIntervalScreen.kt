package com.buttstuff.localserverwatchdog.ui.screen.set_interval

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.ArrowBack
import androidx.compose.material.icons.sharp.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.buttstuff.localserverwatchdog.R
import kotlinx.coroutines.flow.collectLatest

@Composable
fun SetIntervalScreen(intervalVM: SetIntervalViewModel = viewModel(), onSet: () -> Unit) {
    val screenState by intervalVM.screenState.collectAsState()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        Text(
            text = stringResource(R.string.interval_set_header),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.labelSmall
        )
        Row(
            Modifier
                .padding(top = 40.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            TimeText(
                text = buildTimeText(
                    screenState.interval.hours,
                    R.string.hours,
                    shouldMarkAsFilled = !screenState.interval.hoursEmpty
                )
            )
            TimeText(
                text = buildTimeText(
                    value = screenState.interval.minutes,
                    timeUnitRes = R.string.minutes,
                    shouldMarkAsFilled = !screenState.interval.isEmpty
                )
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        val rowsCount = 3
        for (rowIndex in 0 until rowsCount) {
            Row {
                for (columnIndex in 1..rowsCount) {
                    val buttonNumber = rowIndex * rowsCount + columnIndex
                    CommandButton(onClick = { intervalVM.onCommand(Number(buttonNumber)) }, text = "$buttonNumber")
                }
            }
        }
        Row {
            CommandButton(onClick = { intervalVM.onCommand(Erase) }, icon = Icons.Sharp.ArrowBack)
            CommandButton(onClick = { intervalVM.onCommand(Number(0)) }, text = "0")
            CommandButton(onClick = { intervalVM.onCommand(Done) }, icon = Icons.Sharp.Check)

        }
    }

    LaunchedEffect(Unit) {
        intervalVM.sideEffect.collectLatest { effect ->
            if (effect is SetInterval) onSet()
        }
    }
}

@Composable
private fun TimeText(text: AnnotatedString, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.displayLarge,
        modifier = modifier.padding(horizontal = 16.dp)
    )
}

@Composable
private fun buildTimeText(value: Int, @StringRes timeUnitRes: Int, shouldMarkAsFilled: Boolean): AnnotatedString {
    val textColor =
        if (!shouldMarkAsFilled) MaterialTheme.colorScheme.secondary else MaterialTheme.typography.displayLarge.color
    return buildAnnotatedString {
        withStyle(style = SpanStyle(color = textColor)) {
            // append leading zero
            if (value < 10) append("0")
            append(value.toString())
            withStyle(style = SpanStyle(fontSize = MaterialTheme.typography.labelLarge.fontSize)) {
                append(stringResource(timeUnitRes))
            }
        }
    }
}

@Composable
private fun CommandButton(onClick: () -> Unit, text: String, modifier: Modifier = Modifier) {
    Button(
        onClick = { onClick() }, shape = CircleShape, modifier = modifier
            .size(96.dp)
            .padding(8.dp)
    ) {
        Text(text)
    }
}

@Composable
private fun CommandButton(onClick: () -> Unit, icon: ImageVector, modifier: Modifier = Modifier) {
    IconButton(
        onClick = { onClick() },
        modifier = modifier
            .size(96.dp)
            .padding(8.dp)
            .background(color = MaterialTheme.colorScheme.primary, shape = CircleShape)
    ) {
        Icon(imageVector = icon, contentDescription = null)
    }
}

sealed interface Command
data class Number(val number: Int) : Command
object Erase : Command
object Done : Command
