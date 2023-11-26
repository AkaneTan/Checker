package org.akanework.checker.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.akanework.checker.R
import org.akanework.checker.model.CheckerInfo
import org.akanework.checker.model.CheckerInfoItem

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InfoCard(info: CheckerInfo){
    Surface(
        modifier = Modifier.padding(16.dp, 16.dp, 16.dp, 0.dp),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        contentColor = MaterialTheme.colorScheme.onSurface
    ){
        Column(
            Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(bottom = 8.dp),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = info.title.uppercase(),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelMedium
                )
                Icon(
                    painter = painterResource(id = info.iconResource),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            info.content.forEach{ item ->
                FlowRow(Modifier.padding(top = 8.dp)) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier,
                        fontWeight = if(item.isImportant) FontWeight.Bold else FontWeight.Normal
                    )
                    Text(
                        text = item.value,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f).padding(start = 16.dp),
                        fontFamily = FontFamily.Monospace,
                        fontWeight = if(item.isImportant) FontWeight.Bold else FontWeight.Normal,
                        textAlign = TextAlign.End
                    )
                }
            }
        }
    }
}

@Composable
@Preview
fun InfoCardPreview() {
    val previewInfo =
        CheckerInfo(
            title = "Title",
            iconResource = R.drawable.ic_check_circle_24dp,
            content = listOf(
                CheckerInfoItem("item1", "value1", true),
                CheckerInfoItem("item2", "value2")
            )
        )
    InfoCard(info = previewInfo)
}