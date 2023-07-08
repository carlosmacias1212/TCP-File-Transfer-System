package part2.nodes;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.InputMismatchException;
import java.util.Scanner;

import part2.exceptions.NotConnectedToRouterException;

/**
 * This represents a node which is connected to one of two possible routers.
 * 
 * The node is a part of a cluster which is a collection of all nodes connected
 * to one router. There are two clusters of nodes in this architecture.
 * 
 * After a node's connection is established with its router, it can connect to a
 * peer (node) in the other cluster. The node becomes disconnected from the
 * router once a connection to a peer is established. The node can then send
 * files to its peer. The peer is also able to send files to the node too.
 */
public class Node {
    // Static attributes
    public static int unusedPort = 5558; // port that is currently not in use
    public static int nodeCount = 0; // current amount of nodes in this cluster

    // Attributes for interacting with a router
    private int routerPort; // port number of the node's router
    private String routerAddress = null; // IP address of the node's router
    private Socket routerSocket = null; // Socket that connects this node to its router
    private PrintWriter routerOut = null; // output from this node to its router
    private BufferedReader routerIn = null; // input from the router to this node
    private String routerRepr = null; // String representation of the router. Format: IP ADDRESS:PORT #

    // Attributes for interacting with a peer
    private String peerAddress = null; // IP address of the peer this node is connected to
    private ServerSocket nodeSocket = null; // ServerSocket of this node that listens for incoming connections from
                                            // peers
    private Socket peerSocket = null; // Socket that connects this node to a peer
    private DataInputStream peerIn = null; // DataInputStream from the connected peer to this node
    private DataOutputStream peerOut = null; // DataOutputStream from this node to its connected peer

    // Attributes about the current node
    private String localAddress = null; // IP address of this node
    private int nodeID; // ID number of this node
    private boolean connectedToRouter = false; // Whether this node is currently connected to a router
    private boolean connectedToPeer = false; // Whether this node is currently connected to a peer

    /**
     * This creates a new node which then connects to a router.
     * 
     * @param routerAddress the string representation of the router's
     *                      IP address
     * @param routerPort    the port number that the router is using to listen for
     *                      connections from incoming nodes
     */
    public Node(final String routerAddress, final int routerPort) throws NotConnectedToRouterException {
        // Increment the amount of nodes in this cluster
        nodeCount++;

        // Connect this node to a router
        connectToRouter(routerAddress, routerPort);

    }

    // Public methods

    /**
     * This is a general function that is used as the primary way for a user to
     * control a node.
     */
    public void menu() {
        final Scanner scan = new Scanner(System.in);
        int choice = 0;

        // Loop to display the menu
        do {
            try {
                // Check if the socket from this node to its peer has been closed
                if (peerSocket != null && peerSocket.isClosed()) {
                    System.out.println("The other peer closed the session.");
                    System.out.println("Quitting...");
                }

                // Prompt the user
                System.out.print(
                        "\nWhat do you want to do?\n1) Connect to a peer\n2) Send a file to the currently connected peer\n3) Quit\nChoice: ");

                choice = scan.nextInt();
                scan.nextLine();
                switch (choice) {

                    // Connect to a peer
                    case 1:
                        if (isConnectedToPeer()) {
                            System.out.println("You can only connect to one peer at a time!");
                        } else {
                            System.out.print("What is the IP address of the peer you want to connect to?: ");
                            final String destination = scan.next();
                            connectToPeer(destination, 5558);
                        }
                        break;

                    // Send a file
                    case 2:
                        if (isConnectedToPeer()) {
                            // Send the file
                            System.out.print("What file do you want to send: ");
                            final String fileName = scan.nextLine();
                            sendFile(fileName);
                        } else {
                            System.out.println("You must connect to a peer first!");
                        }
                        break;
                    // Quit and disconnect from everything
                    case 3:
                        System.out.println("Quitting...");

                        if (isConnectedToRouter()) {
                            disconnectFromRouter();
                        }
                        disconnectFromPeer();
                        break;

                    // Invalid input
                    default:
                        System.out.println("Enter a valid number!");
                }

            } // Invalid input
            catch (final InputMismatchException e) {
                System.out.println("Enter a valid number!");
                scan.nextLine();
            }

        } // Keep repeating the menu until the decide to quit
        while (choice != 3);

        scan.close();

        // Ensures system gets shut down
        System.exit(0);
    }

    // Setters

    /**
     * sets the status of the node being connected to a router
     * 
     * @param status true if node is connected to a router, false otherwise
     */
    public void setRouterStatus(final boolean status) {
        connectedToRouter = status;
    }

    /**
     * 
     * @param routerOut
     */
    public void setRouterOut(final PrintWriter routerOut) {
        this.routerOut = routerOut;
    }

    /**
     * sets the status of the node being connected to a peer
     * 
     * @param status true if node is connected to a peer, false otherwise
     */
    public void setPeerStatus(final boolean status) {
        connectedToPeer = status;
    }

    /**
     * Sets the ServerSocket that the current node uses to listen for incoming
     * connections
     * 
     * @param nodeSocket ServerSocket object that the node will use to listen for
     *                   incoming connections
     */
    public void setNodeSocket(final ServerSocket nodeSocket) {
        this.nodeSocket = nodeSocket;
    }

    /**
     * Sets the Socket that the node will use to communicate with a peer
     * 
     * @param peerSocket Socket object that the node uses to communicate with a peer
     */
    public void setPeerSocket(final Socket peerSocket) {
        this.peerSocket = peerSocket;
    }

    /**
     * Sets the DataInputStream that a node uses to receive data from a connected
     * peer
     * 
     * @param peerIn A DataInputStream that is from the peerSocket
     */
    public void setPeerIn(final DataInputStream peerIn) {
        this.peerIn = peerIn;
    }

    /**
     * Sets the DataOutputStream that a node uses to send data to a connected
     * peer
     * 
     * @param peerOut A DataOutputStream that is from the peerSocket
     */
    public void setPeerOut(final DataOutputStream peerOut) {
        this.peerOut = peerOut;
    }

    // Getters

    /**
     * returns the status of the node being connected to a router
     * 
     * @return true if node is connected to a router, false otherwise
     */
    public boolean isConnectedToRouter() {
        return connectedToRouter;
    }

    /**
     * returns the status of the node being connected to a peer
     * 
     * @return true if node is connected to a peer, false otherwise
     */
    public boolean isConnectedToPeer() {
        return connectedToPeer;
    }

    /**
     * gets the ID number of the current node. Each node in its router's cluster has
     * a distinct ID number.
     * 
     * @return ID number of the current node
     */
    public int getNodeID() {
        return nodeID;
    }

    /**
     * Gets the local IP address of the current node.
     * 
     * @return string representation of the node's IP address
     */
    public String getLocalAddress() {
        return localAddress;
    }

    /**
     * Gets the Socket that connects the current node to its router
     * 
     * @return Socket object that connects the node to its router
     */
    public Socket getRouterSocket() {
        return routerSocket;
    }

    /**
     * Gets the Socket that connects the current node to its peer
     * 
     * @return Socket object that connects the node to its peer
     */
    public Socket getPeerSocket() {
        return peerSocket;
    }

    /**
     * Gets the PrintWriter object that is used by the current node to communicate
     * with its router
     * 
     * @return PrintWriter object the node uses to communicate with its router
     */
    public PrintWriter getRouterOut() {
        return this.routerOut;
    }

    /**
     * Gets the port that the node's router is listening on
     * 
     * @return port number the router uses to listen
     */
    public int getRouterPort() {
        return routerPort;
    }

    // Private methods
    /**
     * Connects the current node to its router
     * 
     * @param routerAddress String representation of the router's IP address
     * @param routerPort    Integer port the router uses to accept connections
     * @throws NotConnectedToRouterException if the node cannot connect to this
     *                                       specific router
     */
    private void connectToRouter(final String routerAddress, final int routerPort)
            throws NotConnectedToRouterException {
        this.routerAddress = routerAddress;
        this.routerPort = routerPort;
        routerRepr = repr(routerAddress, routerPort);

        nodeID = nodeCount;

        // Try to connect to the router
        try {
            routerSocket = new Socket(routerAddress, routerPort);
            routerOut = new PrintWriter(routerSocket.getOutputStream(), true);
            routerIn = new BufferedReader(new InputStreamReader(routerSocket.getInputStream()));
            localAddress = routerSocket.getLocalAddress().getHostAddress();

            System.out
                    .println("Node " + nodeID + " at address " + localAddress + " is connected to the router at "
                            + routerRepr);

            setRouterStatus(true);

            // Start listening for incoming connections
            listenForPeers();

        } catch (final UnknownHostException e) {
            System.err.println("Couldn't find the router: " + routerRepr);
        } catch (final IOException e) {
            System.err.println("I/O exception occured with the connection to: " + routerRepr);
        }

        if (!isConnectedToRouter()) {
            throw new NotConnectedToRouterException("You could not connect to the desired router!");
        }

    }

    /**
     * This listens for peers to connect to this node. A PeerThread gets started
     * which accepts connections.
     */
    private void listenForPeers() {
        // Check if currently connected to a peer
        if (isConnectedToPeer()) {
            System.out.println(
                    "You are already connected to a peer! Disconnect from the current peer before you connect to another peer.");
            return;
        }

        // Start listening for peers
        final PeerThread pThread = new PeerThread(this);
        pThread.start();

    }

    /**
     * connect the node to a peer
     * 
     * @param address the string representation of the peer's IP address
     * @param port    the port number the peer is using to listen for connections
     *                from incoming nodes
     */
    private void connectToPeer(final String address, final int port) {
        // Check if you can connect
        if (canConnectToPeer(address)) {
            System.out.println("You are able to connect to " + address);
        } else {
            System.err.println("You cannot connect to " + address);
            return;
        }

        // Connect to the peer
        final String nodeRepr = repr(address, port);
        try {
            peerSocket = new Socket(address, port);
            peerAddress = address;
            if (peerSocket.isConnected()) {
                System.out
                        .println("The node at address: " + localAddress + " is connected to the node at "
                                + nodeRepr);
                peerIn = new DataInputStream(peerSocket.getInputStream());
                peerOut = new DataOutputStream(peerSocket.getOutputStream());
                setPeerStatus(true);

                // Listen for messages from the connected peer
                final FileThread thread = new FileThread(this);
                thread.start();

                disconnectFromRouter();

            } else {
                System.err
                        .println("The node at address: " + localAddress + " is NOT connected to the node at "
                                + nodeRepr);
                throw new IOException();
            }

        } catch (final IOException e) {
            System.out
                    .println("The node at address: " + localAddress + " could NOT connect to the node at "
                            + nodeRepr);
            e.printStackTrace();
            System.exit(1);
        }

    }

    /**
     * send a file from the node to its connected peer
     * 
     * @param fileName the string representation of the file that will be sent.
     */
    private void sendFile(final String fileName) {

        try {
            if (peerSocket == null) {
                throw new IOException("You cannot send a file over a null socket!");
            } else if (peerSocket.isClosed() || peerSocket.isOutputShutdown()) {
                System.out.println("Error occurred when writing out the socket!");
            }
            FileInputStream fileIn;
            peerOut.flush();

            // Read in the file and send it to its peer
            fileIn = new FileInputStream(fileName);
            int input;

            // Send the filename to the peer
            peerOut.writeUTF(fileName);
            peerOut.flush();

            // Get the size of the file
            final File file = new File(fileName);
            final long size = file.length();

            // Send the amount of bytes the peer should read
            peerOut.writeLong(size);
            peerOut.flush();

            // Send the file itself to the peer
            while ((input = fileIn.read()) != -1) {
                peerOut.write(input);
                peerOut.flush();
            }

            fileIn.close();
            System.out.println("Sent the " + size + " bytes sized file " + fileName + " to the connected peer!");

        } catch (final FileNotFoundException e) {
            System.out.println("This file cannot be found!");
        } catch (final IOException e) {
            System.err.println("An IOException occurred!");
            e.printStackTrace();
        }
    }

    /**
     * check to see if a node can connect to a peer
     * 
     * @param address string representation of the peer's IP address
     * @return true if the node can connect to the peer, false otherwise
     */
    private boolean canConnectToPeer(final String address) {
        if (isConnectedToPeer()) {
            System.out.println(
                    "You are already connected to a peer! Disconnect from the current peer before you connect to another peer.");
            return false;
        }
        // Check if the peer is in the other router's routing table
        // Send the address to the router
        final String senderType = "node";
        final String messageType = "request";

        routerOut.println(senderType + ":" + messageType + ":" + localAddress + ":" + address);

        System.out
                .println("Node " + nodeID + " sent the address " + address + " to " + repr(routerAddress, routerPort));

        String response;
        try {
            response = routerIn.readLine();
            if (response.equals("YES")) {
                return true;
            } else {
                return false;
            }

        } catch (final IOException e) {
            e.printStackTrace();
            System.err.println("An IO error occurred. Make sure you are connected to the router!");
            System.exit(1);
        }

        return false;

    }

    /**
     * if the node is connected to a router, disconnect them
     * 
     */
    private void disconnectFromRouter() {
        if (!isConnectedToRouter()) {
            System.out.println("You are not connected to a router.");
        } else {
            // Remove the node from the routing table
            routerOut.println("node:disconnect:" + localAddress + ":" + routerSocket);
            routerOut.flush();
            try {
                routerSocket.close();
                System.out.println("You are not connected to a router.");
            } catch (final IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }

    /**
     * if the node is connected to a peer, disconnect them
     * 
     */
    private void disconnectFromPeer() {
        if (!isConnectedToPeer()) {
            System.out.println("You are not connected to a peer.");
        } else {

            try {
                // Close the socket
                peerSocket.close();
                nodeSocket.close();
                routerSocket.close();
                setPeerStatus(false);
                System.out.println("You are not connected to a peer.");

            } catch (final IOException e) {
                e.printStackTrace();
                System.err.println("Unable to close the socket.");
            }

        }
    }

    /**
     * concatenates an address and port into a single string that is in a format
     * used by multiple methods
     * 
     * @param address the string representation of an IP address
     * @param port    the port number
     * @return the concatenated string
     */
    private String repr(final String address, final int port) {
        return address + ":" + port;
    }

}
