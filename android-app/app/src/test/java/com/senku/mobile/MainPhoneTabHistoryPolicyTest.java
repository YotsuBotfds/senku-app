package com.senku.mobile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import com.senku.ui.primitives.BottomTabDestination;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class MainPhoneTabHistoryPolicyTest {
    @Test
    public void pushIgnoresNullDestinationAndFiltersNullExistingEntries() {
        MainPhoneTabHistoryPolicy.StackState result = MainPhoneTabHistoryPolicy.push(
            Arrays.asList(BottomTabDestination.HOME, null, BottomTabDestination.ASK),
            null
        );

        assertEquals(
            Arrays.asList(BottomTabDestination.HOME, BottomTabDestination.ASK),
            result.stack
        );
    }

    @Test
    public void pushSkipsConsecutiveDuplicateDestination() {
        MainPhoneTabHistoryPolicy.StackState result = MainPhoneTabHistoryPolicy.push(
            Arrays.asList(BottomTabDestination.HOME, BottomTabDestination.ASK),
            BottomTabDestination.ASK
        );

        assertEquals(
            Arrays.asList(BottomTabDestination.HOME, BottomTabDestination.ASK),
            result.stack
        );
    }

    @Test
    public void pushCapsHistoryAtEightEntriesByDroppingOldest() {
        List<BottomTabDestination> stack = Arrays.asList(
            BottomTabDestination.HOME,
            BottomTabDestination.ASK,
            BottomTabDestination.PINS,
            BottomTabDestination.HOME,
            BottomTabDestination.ASK,
            BottomTabDestination.PINS,
            BottomTabDestination.HOME,
            BottomTabDestination.ASK
        );

        MainPhoneTabHistoryPolicy.StackState result =
            MainPhoneTabHistoryPolicy.push(stack, BottomTabDestination.PINS);

        assertEquals(MainPhoneTabHistoryPolicy.MAX_HISTORY_SIZE, result.stack.size());
        assertEquals(
            Arrays.asList(
                BottomTabDestination.ASK,
                BottomTabDestination.PINS,
                BottomTabDestination.HOME,
                BottomTabDestination.ASK,
                BottomTabDestination.PINS,
                BottomTabDestination.HOME,
                BottomTabDestination.ASK,
                BottomTabDestination.PINS
            ),
            result.stack
        );
    }

    @Test
    public void popSkipsCurrentActiveTabUntilDifferentDestinationFound() {
        MainPhoneTabHistoryPolicy.PopResult result = MainPhoneTabHistoryPolicy.popPrevious(
            Arrays.asList(
                BottomTabDestination.HOME,
                BottomTabDestination.PINS,
                BottomTabDestination.ASK,
                BottomTabDestination.ASK
            ),
            BottomTabDestination.ASK
        );

        assertEquals(BottomTabDestination.PINS, result.destination);
        assertEquals(Collections.singletonList(BottomTabDestination.HOME), result.stack);
    }

    @Test
    public void popReturnsNullWhenOnlyCurrentOrEmptyHistoryRemain() {
        MainPhoneTabHistoryPolicy.PopResult onlyCurrent = MainPhoneTabHistoryPolicy.popPrevious(
            Arrays.asList(BottomTabDestination.ASK, BottomTabDestination.ASK),
            BottomTabDestination.ASK
        );
        MainPhoneTabHistoryPolicy.PopResult empty =
            MainPhoneTabHistoryPolicy.popPrevious(Collections.emptyList(), BottomTabDestination.HOME);

        assertNull(onlyCurrent.destination);
        assertEquals(Collections.emptyList(), onlyCurrent.stack);
        assertNull(empty.destination);
        assertEquals(Collections.emptyList(), empty.stack);
    }

    @Test
    public void returnedStacksAreImmutableCopies() {
        MainPhoneTabHistoryPolicy.StackState pushed =
            MainPhoneTabHistoryPolicy.push(Collections.emptyList(), BottomTabDestination.HOME);

        try {
            pushed.stack.add(BottomTabDestination.ASK);
            fail("Expected pushed stack to be immutable");
        } catch (UnsupportedOperationException expected) {
            assertEquals(Collections.singletonList(BottomTabDestination.HOME), pushed.stack);
        }
    }
}
