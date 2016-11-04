package org.woezelmann.active.reactive;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.glassfish.jersey.client.rx.rxjava.RxObservable;
import org.junit.Rule;
import org.junit.Test;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func4;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

public class ReactiveJerseyClientTest {

    private Client client = ClientBuilder.newClient();

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8090);

    @Test
    public void testItWithServer() throws Exception {
        stubFor(get(urlEqualTo("/one"))
                .willReturn(aResponse()
                        .withFixedDelay(2000)
                        .withStatus(200)
                        .withBody("one")));

        stubFor(get(urlEqualTo("/two"))
                .willReturn(aResponse()
                        .withFixedDelay(2000)
                        .withStatus(200)
                        .withBody("two")));

        stubFor(get(urlEqualTo("/three"))
                .willReturn(aResponse()
                        .withFixedDelay(3000)
                        .withStatus(200)
                        .withBody("three")));



        Observable<String> first = RxObservable.from(client)
                .target("http://localhost:8090").path("/one")
                .request()
                .rx()
                .get(String.class);

        Observable<String> second = RxObservable.from(client)
                .target("http://localhost:8090").path("/two")
                .request()
                .rx()
                .get(String.class);

        Observable<String> thrid = RxObservable.from(client)
                .target("http://localhost:8090").path("/three")
                .request()
                .rx()
                .get(String.class);

        long l = System.currentTimeMillis();

        String last = Observable.zip(first, second, thrid, (f, s, t) -> f + " " + s + " " + t).toBlocking().last();

        System.out.println(System.currentTimeMillis() - l);
        System.out.println(last);

    }

    @org.junit.Test
    public void testIt() throws Exception {

        Observable<Integer> sleep1 = Observable.from(new Integer[]{1});//.subscribeOn(Schedulers.newThread());
        Observable<Integer> sleep2 = Observable.from(new Integer[]{2});//.subscribeOn(Schedulers.newThread());
        Observable<Integer> sleep3 = Observable.from(new Integer[]{3});//.subscribeOn(Schedulers.newThread());
        Observable<Integer> sleep4 = Observable.from(new Integer[]{4});//.subscribeOn(Schedulers.newThread());

        sleep1.map(sleepAndCalculate());
        sleep2.map(sleepAndCalculate());
        sleep3.map(sleepAndCalculate());
        sleep4.map(sleepAndCalculate());

        Object result = Observable.combineLatest(sleep1, sleep2, sleep3, sleep4, new Func4<Integer, Integer, Integer, Integer, Object>() {
            @Override
            public Object call(Integer integer, Integer integer2, Integer integer3, Integer integer4) {
                return integer + " " + integer2 + " " + integer3 + " " + integer4;
            }
        }).toBlocking().last();

        System.out.println(result);

        Thread.sleep(10000);
    }

    private Func1<Integer, Integer> sleepAndCalculate() {
        return i -> {
            try {
                System.out.println("sleeping for " + i);
                Thread.sleep(i * 1000);
                System.out.println("woke up after " + i);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return i * 2;
        };
    }

    private Action1<Integer> sleep() {
        return integer -> {
            try {
                System.out.println("sleeping for " + integer);
                Thread.sleep(integer * 1000);
                System.out.println("woke up after " + integer);
            } catch (InterruptedException e) {
                System.out.println("could not sleep");
            }
        };
    }
}
