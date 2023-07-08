package part2.exceptions;

public class NotConnectedToRouterException extends Exception {

    public NotConnectedToRouterException(String errorMessage) {
        super(errorMessage);
    }

}
