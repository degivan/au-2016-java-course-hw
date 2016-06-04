package spbau.mit.divan.practice.lazy;

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.function.Supplier;

/**
 * Created by Degtjarenko Ivan on 21.05.2016.
 */
public class LazyFactory {
    private static class SingleThreadLazy<T> implements Lazy<T> {
        private Supplier<T> supplier;
        private T result;

        private SingleThreadLazy(Supplier<T> supplier) {
            this.supplier = supplier;
        }

        @Override
        public T get() {
            if(supplier != null) {
                result = supplier.get();
                supplier = null;
            }
            return result;
        }
    }

    private static class MultipleThreadLazy<T> implements Lazy<T> {
        private boolean called = false;
        private final Supplier<T> supplier;
        private volatile T result;

        private MultipleThreadLazy(Supplier<T> supplier) {
            this.supplier = supplier;
        }

        @Override
        public T get() {
            if(!called) {
                synchronized (this) {
                    if(!called) {
                        result = supplier.get();
                        called = true;
                    }
                }
            }
            return result;
        }
    }

    private static class LockFreeMultipleThreadLazy<T> implements Lazy<T> {
        private static final AtomicReferenceFieldUpdater<LockFreeMultipleThreadLazy, Object> UPDATER =
                AtomicReferenceFieldUpdater.newUpdater(LockFreeMultipleThreadLazy.class,
                        Object.class, "result");

        private Supplier<T> supplier;
        private volatile T result = null;

        LockFreeMultipleThreadLazy(Supplier<T> supplier) {
            this.supplier = supplier;
        }

        @Override
        public T get() {
            if (result == null) {
                Supplier<T> currentSupplier = supplier;
                if (currentSupplier != null) {
                    if (UPDATER.compareAndSet(this, null, currentSupplier.get())) {
                        supplier = null;
                    }
                }
            }
            return result;
        }
    }

    public static <T> Lazy<T> createSingleThreadLazy(Supplier<T> supplier) {
        return new SingleThreadLazy<>(supplier);
    }

    public static <T> Lazy<T> createMultipleThreadLazy(Supplier<T> supplier) {
        return new MultipleThreadLazy<>(supplier);
    }

    public static <T> Lazy<T> createLockFreeMultipleThreadLazy(Supplier<T> supplier) {
        return new LockFreeMultipleThreadLazy<>(supplier);
    }
}
