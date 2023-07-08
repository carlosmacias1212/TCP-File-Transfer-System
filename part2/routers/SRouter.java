package part2.routers;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import part2.exceptions.NotConnectedToRouterException;
import part2.exceptions.RouterTypeException;

/**
 * This represents a router which connects to another router. Each router will
 * manage a cluster of nodes and facilitate initial connections between nodes
 * across clusters. There are two types of routers: "server" and "client".
 * "server" type: listens for connection from another router
 * "client" type: requests a connection to the other router
 * Other than initial connection between routers, they are identical in the way
 * the handle clusters of nodes.
 */
public class SRouter {

    // Static attributes
    public static int unusedPort = 5555; // port that is currently not in use

    // General attributes for this router
    private final Object[][] routingTable = new Object[100][3]; // Routing table that consists of hosts that are
                                                                // connected to this router
    private int indext = 0; // a connected host's position in the routing table
    private String address; // router's IP address
    private String routerType = "server"; // type of the router: ("server" or "client")
    private String routerName = "S-ROUTER 1"; // name of the router
    private boolean listeningForNodes = false; // status of the router listening for nodes

    // Attributes for communication with another router
    private boolean connectedToRouter = false; // status on if the router is connected to another router
    private int port; // port used for communication with other routers
    private ServerSocket routerServerSocket; // ServerSocket used to listen for connections from another router
    private Socket routerClientSocket; // Socket used to communicate with another router

    // Attrributes for communication with a node
    private final int nodePort = 5557; // port number used to communicate with a node
    private ServerSocket routerSocket; // ServerSocket used to listen for connections from a node
    private Socket nodeSocket; // Socket used to communicate with a node

    /*
     * This creates a router of type "server" which listens for incoming connection
     * from another router.
     */
    public SRouter() {
        try {
            // Only used when routerType = "server"
            assert (routerType.equals("server"));

            // Set the port of the current router to listen to
            port = unusedPort;
            unusedPort++;

            // Create a ServerSocket for this router to connect to another router
            routerServerSocket = new ServerSocket();
            routerServerSocket.setReuseAddress(true);

            // Set the address of tjis router
            final Socket socket = new Socket();
            socket.connect(new InetSocketAddress("google.com", 80));
            address = socket.getLocalAddress().getHostAddress();
            socket.close();

            // Bind the ServerSocket to this router's address
            routerServerSocket.bind(new InetSocketAddress(address, port));

        } catch (final IOException e) {
            System.err.println("Could not listen on port: " + port + ".");
            System.exit(1);
        } catch (final AssertionError e) {
            System.err.println("This router must be of type \"server\"!");
            System.exit(1);
        }
    }

    /**
     * create a router of a given type.
     * 
     * @param routerType a string which is the type of router that gets created.
     *                   This can be of type "server" or "client". A router of type
     *                   "server" should exist before a router of type "client" is
     *                   created.
     * @throws RouterTypeException if routerType is not "server" or "client"
     */
    public SRouter(String routerType) throws RouterTypeException {
        routerType = routerType.toLowerCase();

        // S-Router 1 (server)
        if (routerType.equals("server")) {
            try {
                // Only used when routerType = "server"
                assert (routerType.equals("server"));

                // Set the port of the current router to listen to
                port = unusedPort;
                unusedPort++;

                // Create a ServerSocket for this router to connect to another router
                routerServerSocket = new ServerSocket();
                routerServerSocket.setReuseAddress(true);

                // Set the address of the current router
                final Socket socket = new Socket();
                socket.connect(new InetSocketAddress("google.com", 80));
                address = socket.getLocalAddress().getHostAddress();
                socket.close();

                routerServerSocket.bind(new InetSocketAddress(address, port));

            } catch (final IOException e) {
                System.err.println("Could not listen on port: " + port + ".");
                System.exit(1);
            } catch (final AssertionError e) {
                System.err.println("This router must be of type \"server\"!");
                System.exit(1);
            }
        }
        // S-Router 2 (client)
        else if (routerType.equals("client")) {
            this.routerType = routerType;
            this.routerName = "S-ROUTER 2";

        } else {
            throw new RouterTypeException("A routerType can only be equal to 'server' or 'client'.");
        }

    }

    /**
     * listen for and accept an incoming connection from another router
     * 
     * @throws RouterTypeException if routerType does not equal "server"
     */
    public void listenForRouter() throws RouterTypeException {
        // used when routerType = "server"
        if (!routerType.equals("server")) {
            throw new RouterTypeException("This method requires routerType to be 'server'.");
        }

        System.out.println(
                "\n==========================================\n" +
                        "S-ROUTER 1 is listening for S-ROUTER 2 on "
                        + port + "." + "\n==========================================");

        try {
            routerClientSocket = routerServerSocket.accept();
            final ServerThread thread = new ServerThread(routingTable, routerClientSocket, indext,
                    "Server router to client router");
            thread.start();
            indext++;
            System.out.println(
                    "\n==========================================\nROUTER 1: The routers are connected!\n==========================================\n");
            connectedToRouter = true;

        } catch (final IOException e) {
            System.err.println("ROUTER 1: Routers couldn't connect to each other!");
            System.exit(1);
        } catch (final AssertionError e) {
            System.err.println("ROUTER 1: This router must be of type \"server\"!");
            System.exit(1);
        }
    }

    /**
     * connect a router to another router that is listening for incoming connections
     * from routers
     * 
     * @param remoteIP a string representation of the remote router's IP address
     * @throws RouterTypeException if routerType does not equal "client"
     */
    public void connectToRouter(final String remoteIP) throws RouterTypeException {
        if (!routerType.equals("client")) {
            throw new RouterTypeException(
                    "This method can only be called on a router with a routerType 'client'");
        }

        final int remotePort = 5555;

        try {
            routerClientSocket = new Socket(remoteIP, remotePort);
            if (routerClientSocket.isConnected()) {

                address = routerClientSocket.getLocalAddress().getHostAddress();

                final ServerThread thread = new ServerThread(routingTable, routerClientSocket, indext,
                        "Client router to server router");
                thread.start();
                indext++;

                System.out.println("ROUTER 2: The routers are connected!\n This router at " + address
                        + " connected to the router at " + remoteIP + ":" + remotePort);
                connectedToRouter = true;

            } else {
                System.out.println("ROUTER 2: The routers are not connected!");
            }
        } catch (final UnknownHostException e) {
            System.err.println("ROUTER 2: Unknown host exception occurred!");
            System.exit(1);
        } catch (final IOException e) {
            System.err.println("ROUTER 2: Client/Server failed to connect!");
            System.exit(1);
        } catch (final AssertionError e) {
            System.err.println("ROUTER 2: This router must be of type \"client\"!");
            System.exit(1);
        }
    }

    /**
     * listen for and accept connections from nodes to connect to this router
     * 
     * @throws NotConnectedToRouterException if this router hasn't connected to
     *                                       another router
     */
    public void listenForNodes() throws NotConnectedToRouterException {
        if (this.connectedToRouter == false) {
            throw new NotConnectedToRouterException(
                    "The router must be connected to another router before it can listen for nodes.");
        }
        listeningForNodes = true;
        // Create the socket for nodes to connect to
        try {
            routerSocket = new ServerSocket(nodePort);
            while (listeningForNodes) {
                System.out.println("\n" + routerName + " is listening for nodes on port " + nodePort + "!");
                nodeSocket = routerSocket.accept();
                final ServerThread thread = new ServerThread(routingTable, nodeSocket, indext, "Node to " + routerName);
                thread.start();
                indext++;
                System.out.println("\n\n" + routerName + ": A node connected to this router!");

            }

        } catch (final IOException e) {
            System.err.println("An IOException occurred!");
            e.printStackTrace();
        }
    }

    /**
     * shutdown the routers
     * 
     * @throws RouterTypeException if routerType does not equal "server"
     */
    public void close() throws RouterTypeException {
        if (!routerType.equals("server")) {
            throw new RouterTypeException(
                    "This method can only be called on a router with a routerType 'server'");
        }

        try {

            // Close all sockets in the routing table
            for (int i = 0; i < routingTable.length; i++) {
                if (routingTable[i][0] != null) {
                    ((Socket) routingTable[i][1]).close();
                }
            }

        } catch (final IOException e) {
            System.err.println("An error occurred when closing the socket.");
            e.printStackTrace();
        }
    }
}
