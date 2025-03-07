package me.nate.spigot.util;

import java.util.Arrays;
import javax.annotation.CheckReturnValue;
import org.jetbrains.annotations.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

public final class Objects {

    private Objects() {
    }

    // Guava's com.google.common.base.Objects methods start
    @CheckReturnValue
    public static boolean equal(@Nullable Object a, @Nullable Object b) {
        return java.util.Objects.equals(a, b);
    }

    public static int hashCode(@Nullable Object... objects) {
        return Arrays.hashCode(objects);
    }

    public static <T> T firstNonNull(@Nullable T first, @Nullable T second) {
        return first != null ? first : checkNotNull(second);
    }

    public static ToStringHelper toStringHelper(Object self) {
        return new ToStringHelper(simpleName(self.getClass()));
    }

    public static ToStringHelper toStringHelper(Class<?> clazz) {
        return new ToStringHelper(simpleName(clazz));
    }

    public static ToStringHelper toStringHelper(String className) {
        return new ToStringHelper(className);
    }

    private static String simpleName(Class<?> clazz) {
        String name = clazz.getName();

        // the nth anonymous class has a class name ending in "Outer$n"
        // and local inner classes have names ending in "Outer.$1Inner"
        name = name.replaceAll("\\$[0-9]+", "\\$");

        // we want the name of the inner class all by its lonesome
        int start = name.lastIndexOf('$');

        // if this isn't an inner class, just find the start of the
        // top level class name.
        if (start == -1) {
            start = name.lastIndexOf('.');
        }
        return name.substring(start + 1);
    }

    public static final class ToStringHelper {
        private final String className;
        private final ToStringHelper.ValueHolder holderHead = new ToStringHelper.ValueHolder();
        private ToStringHelper.ValueHolder holderTail = this.holderHead;
        private boolean omitNullValues = false;

        /**
         * Use {@link Objects#toStringHelper(Object)} to create an instance.
         */
        private ToStringHelper(String className) {
            this.className = checkNotNull(className);
        }

        public ToStringHelper omitNullValues() {
            this.omitNullValues = true;
            return this;
        }

        public ToStringHelper add(String name, @javax.annotation.Nullable Object value) {
            return this.addHolder(name, value);
        }

        public ToStringHelper add(String name, boolean value) {
            return this.addHolder(name, String.valueOf(value));
        }

        public ToStringHelper add(String name, char value) {
            return this.addHolder(name, String.valueOf(value));
        }

        public ToStringHelper add(String name, double value) {
            return this.addHolder(name, String.valueOf(value));
        }

        public ToStringHelper add(String name, float value) {
            return this.addHolder(name, String.valueOf(value));
        }

        public ToStringHelper add(String name, int value) {
            return this.addHolder(name, String.valueOf(value));
        }

        public ToStringHelper add(String name, long value) {
            return this.addHolder(name, String.valueOf(value));
        }

        public ToStringHelper addValue(@Nullable Object value) {
            return this.addHolder(value);
        }

        public ToStringHelper addValue(boolean value) {
            return this.addHolder(String.valueOf(value));
        }

        public ToStringHelper addValue(char value) {
            return this.addHolder(String.valueOf(value));
        }

        public ToStringHelper addValue(double value) {
            return this.addHolder(String.valueOf(value));
        }

        public ToStringHelper addValue(float value) {
            return this.addHolder(String.valueOf(value));
        }

        public ToStringHelper addValue(int value) {
            return this.addHolder(String.valueOf(value));
        }

        public ToStringHelper addValue(long value) {
            return this.addHolder(String.valueOf(value));
        }

        @Override
        public String toString() {
            // create a copy to keep it consistent in case value changes
            boolean omitNullValuesSnapshot = this.omitNullValues;
            String nextSeparator = "";
            StringBuilder builder = new StringBuilder(32).append(this.className)
                    .append('{');
            for (ToStringHelper.ValueHolder valueHolder = this.holderHead.next; valueHolder != null;
                 valueHolder = valueHolder.next) {
                if (!omitNullValuesSnapshot || valueHolder.value != null) {
                    builder.append(nextSeparator);
                    nextSeparator = ", ";

                    if (valueHolder.name != null) {
                        builder.append(valueHolder.name).append('=');
                    }
                    builder.append(valueHolder.value);
                }
            }
            return builder.append('}').toString();
        }

        private ToStringHelper.ValueHolder addHolder() {
            ToStringHelper.ValueHolder valueHolder = new ToStringHelper.ValueHolder();
            this.holderTail = this.holderTail.next = valueHolder;
            return valueHolder;
        }

        private ToStringHelper addHolder(@javax.annotation.Nullable Object value) {
            ToStringHelper.ValueHolder valueHolder = this.addHolder();
            valueHolder.value = value;
            return this;
        }

        private ToStringHelper addHolder(String name, @javax.annotation.Nullable Object value) {
            ToStringHelper.ValueHolder valueHolder = this.addHolder();
            valueHolder.value = value;
            valueHolder.name = checkNotNull(name);
            return this;
        }

        private static final class ValueHolder {
            String name;
            Object value;
            ToStringHelper.ValueHolder next;
        }
    }
    // Guava's methods end

    // Netty's io.netty.util.internal.Objects methods start
    /**
     * Checks that the given argument is strictly positive. If it is not, throws {@link IllegalArgumentException}.
     * Otherwise, returns the argument.
     */
    public static int checkPositive(int i, String name) {
        if (i <= 0) {
            throw new IllegalArgumentException(name + ": " + i + " (expected: > 0)");
        }
        return i;
    }

    /**
     * Checks that the given argument is strictly positive. If it is not, throws {@link IllegalArgumentException}.
     * Otherwise, returns the argument.
     */
    public static long checkPositive(long i, String name) {
        if (i <= 0) {
            throw new IllegalArgumentException(name + ": " + i + " (expected: > 0)");
        }
        return i;
    }

    /**
     * Checks that the given argument is positive or zero. If it is not , throws {@link IllegalArgumentException}.
     * Otherwise, returns the argument.
     */
    public static int checkPositiveOrZero(int i, String name) {
        if (i < 0) {
            throw new IllegalArgumentException(name + ": " + i + " (expected: >= 0)");
        }
        return i;
    }

    /**
     * Checks that the given argument is positive or zero. If it is not, throws {@link IllegalArgumentException}.
     * Otherwise, returns the argument.
     */
    public static long checkPositiveOrZero(long i, String name) {
        if (i < 0) {
            throw new IllegalArgumentException(name + ": " + i + " (expected: >= 0)");
        }
        return i;
    }
    // Netty methods end

}
