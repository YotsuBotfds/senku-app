package com.senku.ui.tablet

import org.junit.Assert.assertEquals
import org.junit.Test

class TabletDetailAppRailPolicyTest {
    @Test
    fun tabletDetailAppRailActiveDestinationFollowsDetailMode() {
        assertEquals(
            TabletDetailAppRailDestination.Library,
            tabletDetailAppRailActiveDestination(TabletDetailMode.Guide),
        )
        assertEquals(
            TabletDetailAppRailDestination.Ask,
            tabletDetailAppRailActiveDestination(TabletDetailMode.Answer),
        )
        assertEquals(
            TabletDetailAppRailDestination.Ask,
            tabletDetailAppRailActiveDestination(TabletDetailMode.Thread),
        )
    }

    @Test
    fun tabletDetailAppRailSavedActionUsesSavedNavigationCallbackOnly() {
        val calls = mutableListOf<String>()

        tabletDetailAppRailDispatchAction(
            TabletDetailAppRailAction.Saved,
            onLibraryClick = { calls += "library" },
            onAskClick = { calls += "ask" },
            onSavedClick = { calls += "saved" },
        )

        assertEquals(listOf("saved"), calls)
    }

    @Test
    fun tabletDetailAppRailAskActionUsesAskNavigationCallbackOnly() {
        val calls = mutableListOf<String>()

        tabletDetailAppRailDispatchAction(
            TabletDetailAppRailAction.Ask,
            onLibraryClick = { calls += "library" },
            onAskClick = { calls += "ask" },
            onSavedClick = { calls += "saved" },
        )

        assertEquals(listOf("ask"), calls)
    }
}
