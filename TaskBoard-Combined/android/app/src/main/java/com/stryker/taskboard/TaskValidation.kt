package com.stryker.taskboard

// Count Unicode code points, matching the web client's Array.from check.
internal fun isValidTaskTitle(value: String): Boolean {
    val title = value.trim()
    return title.codePointCount(0, title.length) in 1..80
}
