package part2.nodes;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * This represents a thread for a node which listens for incoming connections
 * from peers
 */
public class PeerThread extends Thread {

    private final Node[] nodeArr = new Node[1];

    // Create a new thread
    PeerThread(final Node currentNode) {
        nodeArr[0] = currentNode;

        try {
            final PrintWriter routerOut = new PrintWriter(nodeArr[0].getRouterSocket().getOutputStream(), true);
            nodeArr[0].setRouterOut(routerOut);

        } catch (final IOException e) {
            System.err.println("An IOException occurred!");
            e.printStackTrace();
        }
    }

    /**
     * Runs the thread for a node to listen for and accept connections from peers
     */
    public void run() {
        try {
            // Listen for incoming connections
            final ServerSocket nodeSocket = new ServerSocket();
            nodeSocket.setReuseAddress(true);
            nodeSocket.bind(new InetSocketAddress(nodeArr[0].getLocalAddress(), 5558));
            nodeArr[0].setNodeSocket(nodeSocket);
            System.out.println(
                    "Node " + nodeArr[0].getNodeID() + " is listening for peers on port " + 5558 + " at the IP address "
                            + nodeArr[0].getLocalAddress() + "!");

            // Accept the connection
            final Socket peerSocket = nodeSocket.accept();
            nodeArr[0].setPeerSocket(peerSocket);
            System.out.println("A peer connected to this node!");

            // Create a DataInputStream for the peerSocket
            final DataInputStream peerIn = new DataInputStream(peerSocket.getInputStream());
            nodeArr[0].setPeerIn(peerIn);

            // Create a DataOutputStream for the peerSocket
            final DataOutputStream peerOut = new DataOutputStream(peerSocket.getOutputStream());
            nodeArr[0].setPeerOut(peerOut);

            // Set peerStatus of the parent node to true
            nodeArr[0].setPeerStatus(true);

            // Remove the node from the routing table
            nodeArr[0].getRouterOut()
                    .println("node:disconnect:" + nodeArr[0].getLocalAddress() + ":" + nodeArr[0].getRouterSocket());

            // Set connectedToRouter of the parent node to false
            nodeArr[0].setRouterStatus(false);

            // Create a thread to accept files from the peer
            final FileThread thread = new FileThread(nodeArr[0]);
            thread.start();

        } catch (final IOException e) {
            System.err.println("An IOException occurred!");
            e.printStackTrace();
            System.err.println(e.getMessage());
        }
    }
}