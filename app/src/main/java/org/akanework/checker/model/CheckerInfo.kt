package org.akanework.checker.model

data class CheckerInfo (
    val title: String,
    val iconResource: Int,
    val content: List<CheckerInfoItem>
)

data class CheckerInfoItem (
    val name: String,
    val value: String,
    val isImportant: Boolean = false
)