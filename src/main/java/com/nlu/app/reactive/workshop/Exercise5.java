package com.nlu.app.reactive.workshop;

import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;

import java.io.IOException;

public class Exercise5 {

    public static void main(String[] args) throws IOException {

        // Use ReactiveSources.intNumberMono() and ReactiveSources.userMono()

        // Subscribe to a flux using the error and completion hooks
        ReactiveSources.userFlux()
                .subscribe(System.out::println,
                        System.err::println, () -> System.out.println("Done"));

        // Subscribe to a flux using an implementation of BaseSubscriber
        ReactiveSources.userFlux()
                .subscribe(new BaseSubscriber<>() {
                    @Override
                    protected void hookOnSubscribe(Subscription subscription) {
                        request(1);
                    }

                    @Override
                    protected void hookOnNext(User value) {
                        System.out.println(value.toString() + " received");
                        request(1);
                    }
                });

        System.out.println("Press a key to end");
        System.in.read();
    }

}