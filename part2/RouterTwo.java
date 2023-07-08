package part2;

import part2.exceptions.NotConnectedToRouterException;
import part2.exceptions.RouterTypeException;
import part2.routers.SRouter;

public class RouterTwo {

    static SRouter router;

    public static void main(final String[] args) {

        try {
            // Create a router of routerType "client"
            router = new SRouter("client");

            // Connect to the router of routerType "server"
            router.connectToRouter("192.168.1.13");

            // Listen for nodes to connect to this router
            router.listenForNodes();

        } catch (final RouterTypeException e) {
            System.err.println(e.getMessage());
        } catch (final NotConnectedToRouterException e) {
            System.err.println(e.getMessage());

        }
    }

}
