package com.senku.ui.host

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import com.senku.ui.primitives.SenkuTopBar
import com.senku.ui.primitives.TopBarActionKind
import com.senku.ui.primitives.TopBarActionSpec
import com.senku.ui.theme.SenkuAppTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class SenkuTopBarHostViewAndroidTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun callbackFreshness_handlerUpdateWithoutRecomposition() {
        val log = mutableListOf<String>()
        val handlerA = TopBarActionHandler { log.add("A:$it") }
        val handlerB = TopBarActionHandler { log.add("B:$it") }
        var currentHandler: TopBarActionHandler? = handlerA
        val actions = listOf(
            TopBarActionSpec.back(contentDescription = "Navigate back"),
            TopBarActionSpec.home(contentDescription = "Go home"),
        )

        composeRule.setContent {
            SenkuAppTheme {
                SenkuTopBar(
                    title = "Test",
                    actions = actions,
                    onActionClick = { action -> currentHandler?.onAction(action) },
                )
            }
        }

        composeRule.onNodeWithContentDescription("Navigate back").performClick()
        composeRule.runOnIdle {
            assertEquals(listOf("A:Back"), log)
        }

        currentHandler = handlerB
        log.clear()

        composeRule.onNodeWithContentDescription("Go home").performClick()
        composeRule.runOnIdle {
            assertEquals(listOf("B:Home"), log)
        }

        currentHandler = null
        log.clear()

        composeRule.onNodeWithContentDescription("Navigate back").performClick()
        composeRule.runOnIdle {
            assertEquals(emptyList<String>(), log)
        }
    }

    @Test
    fun callbackFreshness_sameActionClickedAfterHandlerSwap() {
        var aCalls = 0
        var bCalls = 0
        val handlerA = TopBarActionHandler { aCalls++ }
        val handlerB = TopBarActionHandler { bCalls++ }
        var currentHandler: TopBarActionHandler? = handlerA
        val actions = listOf(
            TopBarActionSpec.back(contentDescription = "Navigate back"),
        )

        composeRule.setContent {
            SenkuAppTheme {
                SenkuTopBar(
                    title = "Test",
                    actions = actions,
                    onActionClick = { _ -> currentHandler?.onAction(TopBarActionKind.Back) },
                )
            }
        }

        composeRule.onNodeWithContentDescription("Navigate back").performClick()
        composeRule.runOnIdle {
            assertEquals(1, aCalls)
            assertEquals(0, bCalls)
        }

        currentHandler = handlerB

        composeRule.onNodeWithContentDescription("Navigate back").performClick()
        composeRule.runOnIdle {
            assertEquals(1, aCalls)
            assertEquals(1, bCalls)
        }

        currentHandler = null

        composeRule.onNodeWithContentDescription("Navigate back").performClick()
        composeRule.runOnIdle {
            assertEquals(1, aCalls)
            assertEquals(1, bCalls)
        }
    }
}
