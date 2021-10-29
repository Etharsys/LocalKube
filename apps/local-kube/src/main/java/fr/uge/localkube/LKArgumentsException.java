package fr.uge.localkube;

import java.util.Objects;

public class LKArgumentsException extends RuntimeException {
    /**
     * Class exception throwable
     * message: the error message to print
     */

    private final String message;

    /**
     * Constructor of the exception
     * @param message the message to print
     */
    public LKArgumentsException(String message){
        super(message);
        this.message = message;
    }

    @Override
    /**
     * Print on console the message end return it
     */
    public String toString() {
        Objects.requireNonNull(message);
        var err = "[ERROR] --- ";
        System.out.println(err + message);
        return err + message;
    }
}
