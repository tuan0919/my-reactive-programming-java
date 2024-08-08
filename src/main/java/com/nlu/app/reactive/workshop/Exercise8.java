package com.nlu.app.reactive.workshop;

import reactor.core.publisher.Flux;

import java.io.IOException;

public class Exercise8 {


    public static void main(String[] args) throws IOException {

        // Use ReactiveSources.intNumbersFluxWithException()

        // Print values from intNumbersFluxWithException and print a message when error happens
//        ReactiveSources.intNumbersFluxWithException()
//                .subscribe(num -> {
//                    System.out.println(num);
//                }, (error) -> System.out.println(error.getMessage()));

        // Print values from intNumbersFluxWithException and continue on errors
//        ReactiveSources.intNumbersFluxWithException()
//                .onErrorContinue((error, item) -> {
//                    System.out.println("Error: " + error);
//                    System.out.println("On Item: " + item);
//                }).subscribe(num -> {
//                    System.out.println(num);
//                });

        // Print values from intNumbersFluxWithException and when errors
        // happen, replace with a fallback sequence of -1 and -2
        ReactiveSources.intNumbersFluxWithException()
                .onErrorResume(error -> Flux.just(-1, -2))
                .subscribe(System.out::println);

        System.out.println("Press a key to end");
        System.in.read();
    }

}
