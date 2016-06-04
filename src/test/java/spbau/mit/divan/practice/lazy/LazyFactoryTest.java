package spbau.mit.divan.practice.lazy;

import org.junit.Test;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.function.Supplier;

import static org.junit.Assert.*;

/**
 * Created by Degtjarenko Ivan on 21.05.2016.
 */
public class LazyFactoryTest {

    @Test
    public void testCreateSingleThreadLazy() throws Exception {
        simpleTestSingleThreadLazy();
        nullTestSingleThreadLazy();
        lazinessTestSingleThreadLazy();
    }

    private void lazinessTestSingleThreadLazy() {
        Wrapper<Boolean> asked = new Wrapper<>(false);

        Lazy<String> lazy = LazyFactory.createSingleThreadLazy(() -> {
            assertTrue(asked.x);
            return "test";
        });

        asked.x = true;
        String a = lazy.get();

        assertEquals("test", a);
    }

    private void nullTestSingleThreadLazy() {
        Lazy<String> lazy = LazyFactory.createSingleThreadLazy(new Supplier<String>() {
            private boolean called = false;
            @Override
            public String get() {
                assertFalse(called);
                called = true;
                return null;
            }
        });

        assertNull(lazy.get());
        assertNull(lazy.get());
    }

    private void simpleTestSingleThreadLazy() {
        Lazy<String> lazy = LazyFactory.createSingleThreadLazy(new Supplier<String>() {
            private boolean called = false;
            @Override
            public String get() {
                assertFalse(called);
                called = true;
                return "test";
            }
        });

        String a = lazy.get();
        String b = lazy.get();

        assertEquals("test", a);
        assertSame(a, b);
    }

    @Test
    public void testCreateMultipleThreadLazy() throws Exception {
        Wrapper<Boolean> asked = new Wrapper<>(false);
        Wrapper<Integer> counter = new Wrapper<>(0);
        String res = "test";

        Supplier<String> supplier = () -> {
            assertTrue(asked.x);
            counter.x++;
            return res;
        };

        Lazy<String> lazy = LazyFactory.createMultipleThreadLazy(supplier);
        lazinessTestMultipleThreadLazy(lazy, res, asked);

        assertEquals(1, (int) counter.x);
    }

    @Test
    public void testCreateLockFreeMultipleThreadLazy() throws Exception {
        Wrapper<Boolean> asked = new Wrapper<>(false);
        Map<String, Integer> counter = new HashMap<>();
        String res = "test";

        Supplier<String> supplier = () -> {
            assertTrue(asked.x);

            String key = Thread.currentThread().getName();
            counter.put(key, counter.containsKey(key) ? counter.get(key) + 1 : 0);

            return res;
        };

        Lazy<String> lazyLockFree = LazyFactory.createLockFreeMultipleThreadLazy(supplier);
        lazinessTestMultipleThreadLazy(lazyLockFree, res, asked);

        counter.entrySet().stream()
                .forEach(e -> assertTrue(e.getValue() < 1));
    }

    private <T> void lazinessTestMultipleThreadLazy(Lazy<T> lazy, String res, Wrapper<Boolean> asked) throws InterruptedException {
        List<Future<T>> futures = new LinkedList<>();
        int limit = 500;
        ExecutorService executorService = Executors.newFixedThreadPool(limit);
        CountDownLatch countDownLatch = new CountDownLatch(limit);

        for (int i = 0; i < limit; i++) {
            futures.add(executorService.submit(() -> {
                countDownLatch.countDown();
                countDownLatch.await();
                asked.x = true;
                return lazy.get();
            }));
        }

        countDownLatch.await();

        futures.stream()
                .map(tFuture -> {
                    try {
                        return tFuture.get();
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }
                    return null;
                }).forEach(t -> {
            assertEquals(res, t);
            assertSame(t, res);
        });
    }

    private static class Wrapper<T> {
        public volatile T x;

        public Wrapper(T a) {
            x = a;
        }
    }
}