package org.woezelmann.active.reactive;

import com.google.common.collect.Lists;
import org.junit.Test;
import rx.Observable;
import rx.schedulers.Schedulers;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

public class SynchronousClientTest {
    @Test
    public void whenIsWhatExecutedWithSynchronousClient() throws Exception {

        System.out.println("creating observable from synchronous data");
        Observable<String> observable = Observable.from(SynchronousClient.provideData());

        System.out.println("querying last provided object");
        String last = observable.toBlocking().last();

        System.out.println(last);

    }

    @Test
    public void whenIsWhatExecutedWithFutureClient() throws Exception {

        System.out.println("creating observable from synchronous data");
        Observable<String> observable = Observable.fromCallable(CallableClient.provideData())
                .subscribeOn(Schedulers.io())
                .flatMap(Observable::from);

        System.out.println("querying last provided object");
        String last = observable.map(s -> {
            System.out.println("processing " + s);
            return s.toUpperCase();
        }).toBlocking().last();

        System.out.println(last);

    }

    private static class SynchronousClient {
        public static List<String> provideData() throws InterruptedException {
            System.out.println("sleeping");
            Thread.sleep(1000);

            System.out.println("providing list");
            return Lists.newArrayList(
                    "one",
                    "two",
                    "three"
            );
        }
    }

    private static class CallableClient {
        public static Callable<ArrayList<String>> provideData() {
            return () -> {
                System.out.println("sleeping");
                Thread.sleep(1000);

                System.out.println("providing list");

                return Lists.newArrayList(
                        "one",
                        "two",
                        "three"
                );
            };
        }
    }

}
