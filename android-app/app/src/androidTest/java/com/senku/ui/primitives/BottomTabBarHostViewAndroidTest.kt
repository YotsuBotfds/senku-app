package com.senku.ui.primitives

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.senku.ui.theme.SenkuAppTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class BottomTabBarHostViewAndroidTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun callbackFreshness_handlerUpdateWithoutRecomposition() {
        val log = mutableListOf<String>()
        val handlerA = BottomTabSelectionHandler { log.add("A:$it") }
        val handlerB = BottomTabSelectionHandler { log.add("B:$it") }
        var currentHandler: BottomTabSelectionHandler? = handlerA
        val tabs = BottomTabBarHostView.defaultTabs()

        composeRule.setContent {
            SenkuAppTheme {
                SenkuBottomTabBar(
                    tabs = tabs,
                    activeTab = BottomTabDestination.HOME,
                    layoutMode = BottomTabBarLayoutMode.HORIZONTAL_BAR,
                    onTabSelected = { dest -> currentHandler?.onTabSelected(dest) },
                )
            }
        }

        composeRule.onNodeWithText("Search").performClick()
        composeRule.runOnIdle {
            assertEquals(listOf("A:SEARCH"), log)
        }

        currentHandler = handlerB
        log.clear()

        composeRule.onNodeWithText("Ask").performClick()
        composeRule.runOnIdle {
            assertEquals(listOf("B:ASK"), log)
        }

        currentHandler = null
        log.clear()

        composeRule.onNodeWithText("Threads").performClick()
        composeRule.runOnIdle {
            assertEquals(emptyList<String>(), log)
        }
    }

    @Test
    fun callbackFreshness_sameTabClickedAfterHandlerSwap() {
        var aCalls = 0
        var bCalls = 0
        val handlerA = BottomTabSelectionHandler { aCalls++ }
        val handlerB = BottomTabSelectionHandler { bCalls++ }
        var currentHandler: BottomTabSelectionHandler? = handlerA
        val tabs = BottomTabBarHostView.defaultTabs()

        composeRule.setContent {
            SenkuAppTheme {
                SenkuBottomTabBar(
                    tabs = tabs,
                    activeTab = BottomTabDestination.HOME,
                    layoutMode = BottomTabBarLayoutMode.HORIZONTAL_BAR,
                    onTabSelected = { dest -> currentHandler?.onTabSelected(dest) },
                )
            }
        }

        composeRule.onNodeWithText("Search").performClick()
        composeRule.runOnIdle {
            assertEquals(1, aCalls)
            assertEquals(0, bCalls)
        }

        currentHandler = handlerB

        composeRule.onNodeWithText("Search").performClick()
        composeRule.runOnIdle {
            assertEquals(1, aCalls)
            assertEquals(1, bCalls)
        }

        currentHandler = null

        composeRule.onNodeWithText("Search").performClick()
        composeRule.runOnIdle {
            assertEquals(1, aCalls)
            assertEquals(1, bCalls)
        }
    }
}
