package edu.upc.fib.prop.exceptions;

public class InvalidDetailsException extends Exception {

    public InvalidDetailsException() {
        super("Invalid email or password");
    }

}
