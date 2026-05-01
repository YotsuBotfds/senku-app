package com.senku.mobile;

final class PromptHarnessReflection {
    private PromptHarnessReflection() {
    }

    static Object readPrivateField(Object target, String fieldName) {
        if (target == null || safe(fieldName).trim().isEmpty()) {
            return null;
        }
        Class<?> cursor = target.getClass();
        while (cursor != null) {
            try {
                java.lang.reflect.Field field = cursor.getDeclaredField(fieldName);
                field.setAccessible(true);
                return field.get(target);
            } catch (NoSuchFieldException ignored) {
                cursor = cursor.getSuperclass();
            } catch (Exception ignored) {
                return null;
            }
        }
        return null;
    }

    static Object invokePrivateNoArgMethod(Object target, String methodName) {
        if (target == null || safe(methodName).trim().isEmpty()) {
            return null;
        }
        Class<?> cursor = target.getClass();
        while (cursor != null) {
            try {
                java.lang.reflect.Method method = cursor.getDeclaredMethod(methodName);
                method.setAccessible(true);
                return method.invoke(target);
            } catch (NoSuchMethodException ignored) {
                cursor = cursor.getSuperclass();
            } catch (Exception ignored) {
                return null;
            }
        }
        return null;
    }

    static Object invokePrivateMethod(
        Object target,
        String methodName,
        Class<?>[] parameterTypes,
        Object... args
    ) {
        if (target == null || safe(methodName).trim().isEmpty()) {
            return null;
        }
        Class<?> cursor = target.getClass();
        while (cursor != null) {
            try {
                java.lang.reflect.Method method = cursor.getDeclaredMethod(methodName, parameterTypes);
                method.setAccessible(true);
                return method.invoke(target, args);
            } catch (NoSuchMethodException ignored) {
                cursor = cursor.getSuperclass();
            } catch (Exception ignored) {
                return null;
            }
        }
        return null;
    }

    static Object invokeNoArgMethod(Object target, String methodName) {
        if (target == null || safe(methodName).trim().isEmpty()) {
            return null;
        }
        try {
            java.lang.reflect.Method method = target.getClass().getMethod(methodName);
            method.setAccessible(true);
            return method.invoke(target);
        } catch (Exception ignored) {
            return null;
        }
    }

    static String readPrivateStringField(Object target, String fieldName) {
        Object value = readPrivateField(target, fieldName);
        return value == null ? "" : safe(String.valueOf(value));
    }

    static boolean readPrivateBooleanField(Object target, String fieldName) {
        Object value = readPrivateField(target, fieldName);
        return value instanceof Boolean && (Boolean) value;
    }

    static boolean setPrivateField(Object target, String fieldName, Object value) {
        if (target == null || safe(fieldName).trim().isEmpty()) {
            return false;
        }
        Class<?> cursor = target.getClass();
        while (cursor != null) {
            try {
                java.lang.reflect.Field field = cursor.getDeclaredField(fieldName);
                field.setAccessible(true);
                field.set(target, value);
                return true;
            } catch (NoSuchFieldException ignored) {
                cursor = cursor.getSuperclass();
            } catch (Exception ignored) {
                return false;
            }
        }
        return false;
    }

    static boolean readPrivateBooleanMethod(Object target, String methodName) {
        Object value = invokePrivateNoArgMethod(target, methodName);
        return value instanceof Boolean && (Boolean) value;
    }

    static boolean readBooleanMethod(Object target, String methodName) {
        Object value = invokeNoArgMethod(target, methodName);
        return value instanceof Boolean && (Boolean) value;
    }

    static String invokeStringMethod(Object target, String methodName) {
        Object value = invokeNoArgMethod(target, methodName);
        return value == null ? "" : safe(String.valueOf(value));
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
