package org.woezelmann.active.reactive;

import com.google.common.collect.Lists;
import org.junit.Test;
import rx.Observable;
import rx.internal.operators.UnicastSubject;
import rx.schedulers.Schedulers;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class ConcatTest {
    @Test
    public void concatTwoLists() throws Exception {
        Observable<String> slow = Observable.fromCallable(CallableClient.provideData(5000))
                .subscribeOn(Schedulers.io())
                .flatMap(Observable::from);

        Observable<String> fast = Observable.fromCallable(CallableClient.provideData(3000))
                .subscribeOn(Schedulers.io())
                .flatMap(Observable::from);

        long time = System.currentTimeMillis();

        List<String> last = Observable.concatEager(slow, fast).toList().toBlocking().last();
        System.out.println(last);

        System.out.println(System.currentTimeMillis() - time);
    }

    private static class CallableClient {
        public static Callable<ArrayList<String>> provideData(int timeout) {
            return () -> {
                System.out.println("sleeping: " + timeout);
                Thread.sleep(timeout);

                System.out.println("providing list");

                return Lists.newArrayList(
                        "one: " + timeout,
                        "two: " + timeout,
                        "three: " + timeout
                );
            };
        }
    }
}
