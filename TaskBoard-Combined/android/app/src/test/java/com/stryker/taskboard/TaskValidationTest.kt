package com.stryker.taskboard

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TaskValidationTest {
    @Test fun rejectsBlankTitles() {
        listOf("", "   ", "\n\t").forEach { assertFalse(isValidTaskTitle(it)) }
    }
    @Test fun acceptsTrimmedTitlesAndBoundaries() {
        assertTrue(isValidTaskTitle("  Buy milk  "))
        assertTrue(isValidTaskTitle("a"))
        assertTrue(isValidTaskTitle("a".repeat(80)))
        assertFalse(isValidTaskTitle("a".repeat(81)))
    }
    @Test fun countsEmojiConsistentlyWithWeb() {
        assertTrue(isValidTaskTitle("😀".repeat(80)))
        assertFalse(isValidTaskTitle("😀".repeat(81)))
    }
}
