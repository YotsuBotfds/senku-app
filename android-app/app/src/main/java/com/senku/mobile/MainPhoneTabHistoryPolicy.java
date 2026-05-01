package com.senku.mobile;

import com.senku.ui.primitives.BottomTabDestination;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class MainPhoneTabHistoryPolicy {
    static final int MAX_HISTORY_SIZE = 8;

    private MainPhoneTabHistoryPolicy() {
    }

    static StackState push(List<BottomTabDestination> stack, BottomTabDestination destination) {
        ArrayList<BottomTabDestination> updated = copyNonNull(stack);
        if (destination == null) {
            return new StackState(updated);
        }
        if (!updated.isEmpty() && updated.get(updated.size() - 1) == destination) {
            return new StackState(updated);
        }
        updated.add(destination);
        while (updated.size() > MAX_HISTORY_SIZE) {
            updated.remove(0);
        }
        return new StackState(updated);
    }

    static PopResult popPrevious(List<BottomTabDestination> stack, BottomTabDestination activeTab) {
        ArrayList<BottomTabDestination> updated = copyNonNull(stack);
        while (!updated.isEmpty()) {
            BottomTabDestination previous = updated.remove(updated.size() - 1);
            if (previous != activeTab) {
                return new PopResult(updated, previous);
            }
        }
        return new PopResult(updated, null);
    }

    private static ArrayList<BottomTabDestination> copyNonNull(List<BottomTabDestination> stack) {
        ArrayList<BottomTabDestination> updated = new ArrayList<>();
        if (stack == null) {
            return updated;
        }
        for (BottomTabDestination destination : stack) {
            if (destination != null) {
                updated.add(destination);
            }
        }
        return updated;
    }

    static final class StackState {
        final List<BottomTabDestination> stack;

        StackState(List<BottomTabDestination> stack) {
            this.stack = immutableCopy(stack);
        }
    }

    static final class PopResult {
        final List<BottomTabDestination> stack;
        final BottomTabDestination destination;

        PopResult(List<BottomTabDestination> stack, BottomTabDestination destination) {
            this.stack = immutableCopy(stack);
            this.destination = destination;
        }
    }

    private static List<BottomTabDestination> immutableCopy(List<BottomTabDestination> stack) {
        return Collections.unmodifiableList(new ArrayList<>(stack == null ? Collections.emptyList() : stack));
    }
}
