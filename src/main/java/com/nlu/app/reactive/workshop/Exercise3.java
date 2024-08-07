package com.nlu.app.reactive.workshop;

import java.io.IOException;

public class Exercise3 {

    public static void main(String[] args) throws IOException {

        // Use ReactiveSources.intNumbersFlux()

        // Get all numbers in the ReactiveSources.intNumbersFlux stream
        // into a List and print the list and its size
        var list = ReactiveSources.intNumbersFlux()
                .toStream().toList();
        System.out.println(list);
        System.out.println(list.size());
    }

}
