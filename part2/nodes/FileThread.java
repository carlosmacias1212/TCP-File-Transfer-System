package part2.nodes;

import static org.junit.Assert.assertEquals;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * A thread for a node which receives incoming data from the peer that is
 * connected to it. This thread first recieves a file name which is used to
 * create a new local file. Afterwards, a stream of bytes is recieved which gets
 * written to the new file.
 */

public class FileThread extends Thread {
    // Attributes for interacting with another node
    private final DataInputStream in; // DataInputStream that the current node uses to receive data from its connected
                                      // peer
    private final Node[] nodeArr = new Node[1]; // Array that stores a reference to the current node. This allows for
                                                // the current thread to modify the node's attributes as needed.

    /**
     * create a new thread for a node which receives incoming data from the peer it
     * is connected to
     * 
     * @param peer the socket that is reponsible for the connection from a node to a
     *             peer
     * @throws IOException if an error occurs
     */
    FileThread(final Node node) throws IOException {
        // Store the node into the node array to allow for its attributes to be changed
        // in the run method
        nodeArr[0] = node;

        // Get the nodes input stream for receiving
        in = new DataInputStream(nodeArr[0].getPeerSocket().getInputStream());
    }

    /**
     * starts the thread for a node which listens for incoming data.
     * This thread first recieves a file name which is used to create a new local
     * file. It then receives the size of the file that will be received.
     * Afterwards, a stream of bytes is received which gets written to the local
     * file.
     */
    public void run() {

        String message;
        final boolean running = true;

        // Listen for data over the input stream
        while (running) {
            try {

                // Receive a message
                message = in.readUTF();

                // Connection to peer has been shutdown
                if (message == null) {
                    System.out.println("The node has been shutdown");
                }

                System.out.println("Node received the message: " + message);

                // Create the file locally
                final FileOutputStream fileOut = new FileOutputStream(message);

                // Receive the size of the file
                final long size = in.readLong();

                System.out.println("Node received the filesize: " + size);

                // Variable used to store incoming bytes of data
                int data;

                // Read size bytes of data and write them to the file
                long count = 0;
                while (count < size) {
                    data = in.read();
                    fileOut.write(data);
                    count++;
                }

                fileOut.close();

                // Verify the size of the file received matches what was written to the file
                final File file = new File(message);
                final long writtenSize = file.length();
                System.out.println("The file written is " + writtenSize + " bytes");
                System.out.println("Saved " + message + " from a peer!");
                assertEquals(size, writtenSize);

            } catch (final IOException e) {
                nodeArr[0].setPeerStatus(false);
                try {
                    nodeArr[0].getPeerSocket().close();
                    nodeArr[0].getRouterSocket().close();
                } catch (final IOException e1) {
                    System.out.println("Socket is already closed");
                }
                System.out.println("The socket from this node to the peer has been closed!");
                System.out.println("Quitting...");
                System.exit(0);
            }
            break;
        }
    }
}
