package org.akanework.checker.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.akanework.checker.R
import org.akanework.checker.ui.theme.StatusTheme
import org.akanework.checker.ui.theme.harmonizedStatusTheme

@Composable
fun StatusCard(status: Int) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val cardTheme: StatusTheme = when (status) {
        0 -> harmonizedStatusTheme(StatusTheme.green, primaryColor)
        1 -> harmonizedStatusTheme(StatusTheme.orange, primaryColor)
        2 -> StatusTheme(
            primary = MaterialTheme.colorScheme.error,
            onPrimary = MaterialTheme.colorScheme.onError,
            primaryContainer = MaterialTheme.colorScheme.errorContainer,
            onPrimaryContainer = MaterialTheme.colorScheme.onErrorContainer
        )

        else -> throw IllegalAccessException()
    }
    val title: String = when(status){
        0 -> stringResource(id = R.string.normal_title)
        1 -> stringResource(id = R.string.notice_title)
        2 -> stringResource(id = R.string.critical_title)

        else -> throw IllegalAccessException()
    }
    val summary: String = when(status) {
        0 -> stringResource(id = R.string.normal_summary)
        1 -> stringResource(id = R.string.notice_summary)
        2 -> stringResource(id = R.string.critical_summary)

        else -> throw IllegalAccessException()
    }
    val iconPainter = when(status) {
        0 -> painterResource(id = R.drawable.ic_check_circle_24dp)
        1 -> painterResource(id = R.drawable.ic_warning_24dp)
        2 -> painterResource(id = R.drawable.ic_dangerous_24dp)

        else -> throw IllegalAccessException()
    }
    
    Surface(
        Modifier
            .fillMaxWidth()
            .padding(16.dp, 16.dp, 16.dp, 0.dp),
        color = cardTheme.primary,
        contentColor = cardTheme.onPrimary,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Icon(
                    iconPainter,
                    contentDescription = null,
                    modifier = Modifier
                        .size(24.dp)
                        .alpha(0.72f)
                )
            }
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = summary,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.alpha(0.72f)
            )
        }
    }
}

@Preview
@Composable
fun StatusCardPreview() {
    StatusCard(status = 1)
}