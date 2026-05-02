package com.senku.ui.home

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.senku.ui.theme.SenkuAppTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class CategoryShelfHostViewAndroidTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun testItems() = listOf(
        CategoryShelfItemModel(
            bucketKey = "water",
            label = "Water",
            countLabel = "10",
            accentColor = 0xFF7A9AB4.toInt(),
            enabled = true,
            contentDescription = "Water category",
        ),
        CategoryShelfItemModel(
            bucketKey = "fire",
            label = "Fire",
            countLabel = "5",
            accentColor = 0xFFC48A5A.toInt(),
            enabled = true,
            contentDescription = "Fire category",
        ),
        CategoryShelfItemModel(
            bucketKey = "shelter",
            label = "Shelter",
            countLabel = "3",
            accentColor = 0xFF7A9A5A.toInt(),
            enabled = true,
            contentDescription = "Shelter category",
        ),
    )

    @Test
    fun callbackFreshness_handlerUpdateWithoutRecomposition() {
        val log = mutableListOf<String>()
        val handlerA = CategoryShelfSelectionHandler { log.add("A:${it.bucketKey}") }
        val handlerB = CategoryShelfSelectionHandler { log.add("B:${it.bucketKey}") }
        var currentHandler: CategoryShelfSelectionHandler? = handlerA
        val items = testItems()

        composeRule.setContent {
            SenkuAppTheme {
                CategoryShelf(
                    items = items,
                    layoutMode = CategoryShelfLayoutMode.PHONE_GRID,
                    selectionEnabled = true,
                    onCategorySelected = { item -> currentHandler?.onCategorySelected(item) },
                )
            }
        }

        composeRule.onNodeWithText("Water").performClick()
        composeRule.runOnIdle {
            assertEquals(listOf("A:water"), log)
        }

        currentHandler = handlerB
        log.clear()

        composeRule.onNodeWithText("Fire").performClick()
        composeRule.runOnIdle {
            assertEquals(listOf("B:fire"), log)
        }

        currentHandler = null
        log.clear()

        composeRule.onNodeWithText("Shelter").performClick()
        composeRule.runOnIdle {
            assertEquals(emptyList<String>(), log)
        }
    }

    @Test
    fun callbackFreshness_sameCardClickedAfterHandlerSwap() {
        var aCalls = 0
        var bCalls = 0
        val handlerA = CategoryShelfSelectionHandler { aCalls++ }
        val handlerB = CategoryShelfSelectionHandler { bCalls++ }
        var currentHandler: CategoryShelfSelectionHandler? = handlerA
        val items = testItems()

        composeRule.setContent {
            SenkuAppTheme {
                CategoryShelf(
                    items = items,
                    layoutMode = CategoryShelfLayoutMode.PHONE_GRID,
                    selectionEnabled = true,
                    onCategorySelected = { item -> currentHandler?.onCategorySelected(item) },
                )
            }
        }

        composeRule.onNodeWithText("Water").performClick()
        composeRule.runOnIdle {
            assertEquals(1, aCalls)
            assertEquals(0, bCalls)
        }

        currentHandler = handlerB

        composeRule.onNodeWithText("Water").performClick()
        composeRule.runOnIdle {
            assertEquals(1, aCalls)
            assertEquals(1, bCalls)
        }

        currentHandler = null

        composeRule.onNodeWithText("Water").performClick()
        composeRule.runOnIdle {
            assertEquals(1, aCalls)
            assertEquals(1, bCalls)
        }
    }
}
