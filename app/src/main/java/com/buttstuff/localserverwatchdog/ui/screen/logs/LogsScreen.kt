package com.buttstuff.localserverwatchdog.ui.screen.logs

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.buttstuff.localserverwatchdog.util.SERVER_STATUS_DOWN
import com.buttstuff.localserverwatchdog.util.SERVER_STATUS_UP

@Composable
fun LogsScreen(logsVM: LogsVM = viewModel()) {
    val logs by logsVM.data.collectAsState()
    val text = buildLogsText(logs)
    val scroll = rememberScrollState(0)

    Text(
        text = text, modifier = Modifier
            .padding(24.dp)
            .fillMaxWidth()
            .fillMaxHeight()
            .verticalScroll(scroll)
    )

    LaunchedEffect(logs) {
        scroll.scrollTo(scroll.maxValue)
    }

    LaunchedEffect(Unit) {
        logsVM.updateData()
    }
}

@Composable
fun buildLogsText(lines: List<String>): AnnotatedString {
    val dateStyle = SpanStyle(color = MaterialTheme.colorScheme.tertiary)
    val statusOkStyle = SpanStyle(color = MaterialTheme.colorScheme.primary)
    val statusDownStyle = SpanStyle(color = MaterialTheme.colorScheme.error)
    return buildAnnotatedString {
        lines.forEach { line ->
            //todo magic number - find another way to provide timestamp length
            val timestampLength = 21
            val timeStampString = line.substring(0, timestampLength)
            withStyle(dateStyle) {
                append(timeStampString)
            }

            val statusOkIndex = line.indexOf(SERVER_STATUS_UP).takeIf { it > -1 }
            val statusIndex = statusOkIndex
                ?: line.indexOf(SERVER_STATUS_DOWN).takeIf { it > -1 }
                ?: line.lastIndex


            val addressString = line.substring(timestampLength, statusIndex)
            append(addressString)

            val statusStyle = if (statusOkIndex != null) statusOkStyle else statusDownStyle
            withStyle(statusStyle) {
                val statusString = line.substring(statusIndex, line.length)
                append(statusString + "\n")
            }
        }
    }
}
