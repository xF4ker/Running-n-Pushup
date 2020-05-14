package com.example.toosie.Data.Work;

public class WorkWithDataException extends Exception {

    WorkWithDataException(final String errorMessage) {
        super(errorMessage);
    }

    WorkWithDataException() {
        super();
    }
}
