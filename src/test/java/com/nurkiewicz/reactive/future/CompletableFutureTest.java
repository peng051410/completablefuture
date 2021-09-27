package com.nurkiewicz.reactive.future;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.Test;

/**
 * CompleteFuture Sample
 *
 * @author tomyli
 * @date 2021/9/27
 * @since 1.0
 */
public class CompletableFutureTest {


    @Test
    public void testThenApply() throws ExecutionException, InterruptedException {

        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> 22)
                .thenApply(i -> i * 2);

        assertEquals(44, (int) (future.get()));
    }

    @Test
    public void testThenApply2() throws ExecutionException, InterruptedException {

        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> 22)
                .thenApply(i -> i * 2)
                .thenApply(i -> i / 2);

        assertEquals(22, (int) (future.get()));
    }

    @Test
    public void testThenCompose() throws ExecutionException, InterruptedException {

//        CompletableFuture<CompletableFuture<String>> future1 = CompletableFuture
//                .supplyAsync(() -> 22).thenApply(this::getName);

        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> 22)
                .thenCompose(this::getName);

        assertEquals("name: 22", future.get());
    }

    private CompletableFuture<String> getName(int num) {
        return CompletableFuture.completedFuture("name: " + num);
    }

    @Test
    public void testThenApplyWithSleep() {

        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
            sleep(2);
            System.out.println(Thread.currentThread().getName() + ": in supplyAsync");
            return 22;
        }).thenApply(i -> {
            sleep(1);
            System.out.println(Thread.currentThread().getName() + ": in thenApplyAsync");
            return i * 3;
        });

        try {
            future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testThenAcceptAsync() {

        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
            sleep(2);
            System.out.println(Thread.currentThread().getName() + ": in supplyAsync");
            return 22;
        }).thenApplyAsync(i -> {
            sleep(1);
            System.out.println(Thread.currentThread().getName() + ": in thenApplyAsync");
            return i * 3;
        });

        try {
            future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testThenAccept() {

        CompletableFuture<Void> future = CompletableFuture.supplyAsync(() -> {
            sleep(1);
            return 22;
        }).thenAccept(System.out::println);

        assertTrue(future.isDone());
    }

    @Test
    public void testThenCombine() throws ExecutionException, InterruptedException {

        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> 22);
        CompletableFuture<Integer> future1 = CompletableFuture.supplyAsync(() -> 22);
        CompletableFuture<Integer> future2 = future.thenCombine(future1, Integer::sum);

        assertEquals(44, (int) future2.get());
    }

    @Test
    public void testCompletionException() throws ExecutionException, InterruptedException {

        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
            int result = 1 / 0;
            return 22;
        });

        CompletableFuture<Integer> exceptionally = future.exceptionally((throwable -> {
            assertTrue(throwable instanceof CompletionException);
            assertTrue(throwable.getCause() instanceof ArithmeticException);
            return 20;
        }));
        assertEquals(20, (int) exceptionally.get());

    }

    @Test
    public void testAllOf() throws ExecutionException, InterruptedException {

        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> 1);
        CompletableFuture<Integer> future1 = CompletableFuture.supplyAsync(() -> 2);
        CompletableFuture<Integer> future2 = CompletableFuture.supplyAsync(() -> 3);

        CompletableFuture<Void> allFuture = CompletableFuture.allOf(future, future1, future2);
        allFuture.get();
    }


    @Test
    public void testAnyOf() throws ExecutionException, InterruptedException {

        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
            sleep(2);
            return 2222;
        });
        CompletableFuture<Integer> future1 = CompletableFuture.supplyAsync(() -> {
            sleep(1);
            return 3333;
        });
        CompletableFuture<Integer> future2 = CompletableFuture.supplyAsync(() -> {
            sleep(3);
            return 4444;
        });

        CompletableFuture<Object> anyOfFuture = CompletableFuture.anyOf(future, future1, future2);
        assertEquals(3333, (int) anyOfFuture.get());
    }

    @Test
    public void testException() throws ExecutionException, InterruptedException {

        CompletableFuture<Integer> in_exception = CompletableFuture.supplyAsync(() -> {
            int foo = 1 / 0;
            return 3;
        }).exceptionally(th -> {
            System.out.println("in exception");
            return 0;
        });
        assertEquals(0, ((int) in_exception.get()));
    }

    @Test
    public void testExceptionNormal() throws ExecutionException, InterruptedException {

        CompletableFuture<Integer> in_exception = CompletableFuture.supplyAsync(() -> {
            return 3;
        }).exceptionally(th -> {
            System.out.println("in exception");
            return 0;
        });
        assertEquals(3, ((int) in_exception.get()));
    }

    @Test
    public void testExceptionMulti() throws ExecutionException, InterruptedException {

        CompletableFuture<Integer> in_exception = CompletableFuture.supplyAsync(() -> {
            System.out.println("in supply");
            int foo = 1 / 0;
            return 3;
        }).thenApply(i -> {
            System.out.println("in apply");
            return 5;
        }).exceptionally(th -> {
            System.out.println("in exception");
            return 0;
        });
        assertEquals(0, ((int) in_exception.get()));
    }

    @Test
    public void testAllOfException() throws ExecutionException, InterruptedException {

        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
            sleep(1);
            int foo = 1 / 0;
            return 1;
        });
        CompletableFuture<Integer> future1 = CompletableFuture.supplyAsync(() -> {
            sleep(2);
            return 2;
        });
        CompletableFuture<Integer> future2 = CompletableFuture.supplyAsync(() -> {
            sleep(3);
            return 3;
        });

        CompletableFuture<Void> allFuture = CompletableFuture.allOf(future, future1, future2);
        try {
            allFuture.get(1, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testHandleException() throws ExecutionException, InterruptedException {

        CompletableFuture<Integer> in_exception = CompletableFuture.supplyAsync(() -> {
            int foo = 1 / 0;
            return 3;
        }).handle((r, th) -> {
            if (th != null) {
                System.out.println("got exception");
                return 0;
            }
            return r;
        });
        assertEquals(0, ((int) in_exception.get()));
    }

    @Test
    public void testHandleNormal() throws ExecutionException, InterruptedException {

        CompletableFuture<Integer> in_exception = CompletableFuture.supplyAsync(() -> {
            return 3;
        }).handle((r, th) -> {
            if (th != null) {
                System.out.println("got exception");
                return 0;
            }
            return r;
        });
        assertEquals(3, ((int) in_exception.get()));
    }

    @Test
    public void testThenRun() {

        CompletableFuture<Void> future = CompletableFuture.supplyAsync(() -> 22)
                .thenRun(() -> System.out.println("hello"));

        assertTrue(future.isDone());
    }

    @Test
    public void testCompleteMulti() throws ExecutionException, InterruptedException {

        CompletableFuture<String> future = new CompletableFuture<>();
        future.complete("1234");
        future.completeExceptionally(new RuntimeException("i'm exception"));
        assertEquals("1234", future.get());
    }

    private void sleep(int second) {
        try {
            TimeUnit.SECONDS.sleep(second);
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }
}
