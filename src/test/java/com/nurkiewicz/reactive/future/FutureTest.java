package com.nurkiewicz.reactive.future;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import org.hamcrest.core.IsInstanceOf;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Future使用案例测试
 *
 * @author tomyli
 * @date 2021/9/24
 * @since 1.0
 */
public class FutureTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testFutureRunnable() throws InterruptedException {

        ExecutorService executorService = Executors.newFixedThreadPool(5);

        List<FutureTask<?>> list = new ArrayList<>();
        for (int k = 0; k < 10; k++) {
            int finalK = k;
            Runnable runnable = () -> System.out
                    .println("线程开始干活！！！   " + Thread.currentThread().getName() + ": " + finalK);
            FutureTask<?> future = new FutureTask<Void>(runnable, null);
            list.add(future);
        }

        for (FutureTask<?> futureTask : list) {
            executorService.execute(futureTask);
        }

        executorService.awaitTermination(1000, TimeUnit.MILLISECONDS);
    }

    @Test
    public void testFutureCallable() throws ExecutionException, InterruptedException {

        ExecutorService executorService = Executors.newFixedThreadPool(5);

        List<Future<String>> list = new ArrayList<>();
        for (int k = 0; k < 10; k++) {
            int finalK = k;
            Future<String> submit = executorService.submit(() -> {
                TimeUnit.MILLISECONDS.sleep(finalK * 100);
                System.out.println("线程开始干活！！！             " + Thread.currentThread().getName());
                return String.valueOf(finalK);
            });
            list.add(submit);
        }

        for (Future<String> future : list) {
            System.out.println(future.get());
        }

        executorService.awaitTermination(1000, TimeUnit.MILLISECONDS);
    }

    @Test(timeout = 2000)
    public void testCFNotComplete() throws ExecutionException, InterruptedException {

        CompletableFuture<String> future = new CompletableFuture<>();
        String result = future.get();

    }

    /**
     * https://stackoverflow.com/questions/53468550/how-obtrudevalue-method-of-completablefuture-works
     * ObtrudeXXX 只在想要从不正确的状态下恢复才推荐使用
     *
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Test
    public void testObtrudeValue() throws ExecutionException, InterruptedException {

        CompletableFuture<Object> fut = new CompletableFuture<>();
        fut.complete("test1");
        assertEquals("test1", fut.get()); // test1

        fut.complete("test2");
        assertEquals("test1", fut.get()); //test2 not work, value not overwritten

        fut.obtrudeValue("obtrudeValue");
        assertEquals("obtrudeValue", fut.get()); //value overwritten, though it complete

        fut.obtrudeValue("obtrudeNewValue");
        assertEquals("obtrudeNewValue", fut.get()); //obtrudeNewValue, value overwritten

        fut.complete("test2");
        assertEquals("obtrudeNewValue", fut.get()); //obtrudeNewValue, complete not work
    }

    @Test
    public void testException() throws ExecutionException, InterruptedException {

        thrown.expect(ExecutionException.class);
        thrown.expectCause(IsInstanceOf.instanceOf(RuntimeException.class));
        thrown.expectMessage("test1");
        getExceptionCompletableFuture().get();
    }

    @Test
    public void testObtrudeEx() throws ExecutionException, InterruptedException {

        thrown.expect(ExecutionException.class);
        thrown.expectCause(IsInstanceOf.instanceOf(RuntimeException.class));
        thrown.expectMessage("test2");
        getObtrudeCompletableFuture().get();

    }

    public CompletableFuture<Object> getExceptionCompletableFuture() {

        return CompletableFuture.failedFuture(new ArrayIndexOutOfBoundsException("test1"));
    }

    private CompletableFuture<Object> getObtrudeCompletableFuture() {

        CompletableFuture<Object> future = CompletableFuture.failedFuture(new ArrayIndexOutOfBoundsException("test1"));
        future.obtrudeException(new ArithmeticException("test2"));
        future.obtrudeException(new ArithmeticException("test3"));
        future.completeExceptionally(new ArithmeticException("test4"));
        return future;
    }

    @Test
    public void testObtrudeExMulti() throws ExecutionException, InterruptedException {

        thrown.expect(ExecutionException.class);
        thrown.expectCause(IsInstanceOf.instanceOf(RuntimeException.class));
        thrown.expectMessage("test3");
        getObtrudeCompletableFutureMulti().get();
    }

    private CompletableFuture<Object> getObtrudeCompletableFutureMulti() {

        CompletableFuture<Object> future = CompletableFuture.failedFuture(new ArrayIndexOutOfBoundsException("test1"));
        future.obtrudeException(new ArithmeticException("test2"));
        future.obtrudeException(new ArithmeticException("test3"));
        return future;
    }

    @Test
    public void testObtrudeValueAndEx1() throws ExecutionException, InterruptedException {

        assertEquals("2222", getObtrudeValueAndExCompletableFuture().get());
    }

    private CompletableFuture<Object> getObtrudeValueAndExCompletableFuture() {

        CompletableFuture<Object> future = CompletableFuture.failedFuture(new ArrayIndexOutOfBoundsException("test1"));
        future.obtrudeValue("2222");
        return future;
    }

    @Test
    public void testObtrudeValueAndEx2() throws ExecutionException, InterruptedException {

        thrown.expect(ExecutionException.class);
        thrown.expectCause(IsInstanceOf.instanceOf(RuntimeException.class));
        thrown.expectMessage("test4");
        getObtrudeValueAndExCompletableFuture2().get();
    }

    private CompletableFuture<Object> getObtrudeValueAndExCompletableFuture2() {

        CompletableFuture<Object> future = CompletableFuture.completedFuture("3333");
        future.obtrudeException(new ArithmeticException("test4"));
        return future;
    }



    @Test
    public void testGetNumberOfDependents() {

        CompletableFuture<Integer> c;

        c = new CompletableFuture<>();
        assertEquals(777, (int) c.getNow(777));

        c = new CompletableFuture<>();

        c.thenApplyAsync(i -> i + 42).thenApplyAsync(i -> i * 12);
        assertEquals(1, c.getNumberOfDependents());

        c.thenApplyAsync(i -> i / 2);
        assertEquals(2, c.getNumberOfDependents());

        assertFalse(c.isDone());
    }

}
