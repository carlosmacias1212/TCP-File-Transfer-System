package part2;

import java.util.Scanner;

import part2.exceptions.NotConnectedToRouterException;
import part2.exceptions.RouterTypeException;
import part2.routers.SRouter;

public class RouterOne implements Runnable {

    static SRouter router;

    public static void main(final String[] args) {
        // Create a router of type "server"
        router = new SRouter();

        // Listen for the client router to connect to this router
        try {
            router.listenForRouter();
        } catch (final RouterTypeException e) {
            System.err.println("listenForRouter() can only be used on a router of type 'server'.");
        }

        // Listen for nodes to connect to this router
        final RouterOne routerOne = new RouterOne();
        final Thread thread = new Thread(routerOne);
        thread.start();

        // Get user input
        final Scanner scan = new Scanner(System.in);
        String input = null;
        do {
            System.out.print("Enter quit at anytime to shutdown the routers: ");
            input = scan.nextLine();

        } while (!input.equals("quit"));

        scan.close();
        try {
            router.close();
        } catch (final RouterTypeException e) {
            System.err.println(e.getMessage());
        }
        System.exit(0);

    }

    @Override
    public void run() {
        // Listen for nodes to connect to this router
        try {
            router.listenForNodes();
        } catch (final NotConnectedToRouterException e) {
            System.out.println("listenForNodes() requires a connection to another router.");
        }
    }

}
