package com.senku.ui.sources

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.senku.ui.theme.SenkuAppTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class SourceRowTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun sourceRowShowsAnchorMetaAndReturnsRowOnClick() {
        val source = SourceRowModel(
            guideId = "GD-214",
            title = "Water cache rotation",
            category = "water",
            isAnchor = true,
        )
        var clicked: SourceRowModel? = null

        composeRule.setContent {
            SenkuAppTheme {
                SourceRow(
                    source = source,
                    onClick = { clicked = it },
                )
            }
        }

        composeRule.onAllNodesWithText("GD-214").assertCountEquals(1)
        composeRule.onAllNodesWithText("Water cache rotation").assertCountEquals(1)
        composeRule.onAllNodesWithText("water · anchor guide").assertCountEquals(1)
        composeRule.onNodeWithText("Water cache rotation").performClick()
        composeRule.runOnIdle {
            assertEquals(source, clicked)
        }
    }

    @Test
    fun sourceRowNormalizesCategoryAndUsesFallbackTitle() {
        composeRule.setContent {
            SenkuAppTheme {
                SourceRow(
                    source = SourceRowModel(
                        guideId = "GD-118",
                        title = "",
                        category = "resource-management",
                    ),
                    onClick = {},
                )
            }
        }

        composeRule.onAllNodesWithText("GD-118").assertCountEquals(1)
        composeRule.onAllNodesWithText("Source guide").assertCountEquals(1)
        composeRule.onAllNodesWithText("resource management").assertCountEquals(1)
        composeRule.onAllNodesWithText("anchor guide").assertCountEquals(0)
    }
}
