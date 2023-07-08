import java.io.*;
import java.net.*;

public class SThread extends Thread {
	// Routing table
	private Object[][] RTable;
	// Two DataOutputStreams (for writing back to the machine and to destination)
	private DataOutputStream out, outTwo;

	// DataInputStreams
	private DataInputStream in; // reader (for reading from the machine connected to)

	// Variables for storing communication
	private String outputLine, destination, addr;

	// Integer variable to store bytes that are passed into the router
	private int dataIn;

	// Socket for communicating with a destination
	private Socket outSocket;

	// Constructor
	SThread(Object[][] Table, Socket toClient, int index) throws IOException {

		/*
		 * Pass the socket's streams into data streams for more portabiltity
		 */
		// Pass the socket's output stream into a DataOutputStream
		out = new DataOutputStream(toClient.getOutputStream());

		// Pass the socket's output stream into a DataInputStream
		in = new DataInputStream(toClient.getInputStream());

		// Routing table
		RTable = Table;

		addr = toClient.getInetAddress().getHostAddress();
		RTable[index][0] = addr; // IP addresses
		RTable[index][1] = toClient; // sockets for communication
	}

	// Run method (will run for each machine that connects to the ServerRouter)
	public void run() {
		try {

			/* Initial sends/receives */
			// Recieve (initial): Gets the destination
			System.out.println("Input - destination");
			destination = in.readUTF();
			System.out.println("Received! - " + destination + "\n");

			// Send (Initial): confirmation of connection message
			outputLine = "Connected to the router.";
			System.out.println("Output - outputLine");
			out.writeUTF(outputLine); // confirmation of connection

			// Flush the dataOut to ensure that all of the buffered bytes are written
			out.flush();
			System.out.println("Sent! - " + outputLine + "\n\n");

			// waits 10 seconds to let the routing table fill with all machines' information
			try {
				Thread.currentThread().sleep(10000);
			} catch (InterruptedException ie) {
				System.out.println("Thread interrupted");
			}

			//Start time before routing table is used
			float startTime = System.nanoTime();
			// loops through the routing table to find the destination
			for (int i = 0; i < 10; i++) {
				if (destination.equals((String) RTable[i][0])) {
					outSocket = (Socket) RTable[i][1]; // gets the socket for communication from the table
					System.out.println("Found destination in the routing table: " + destination);
					outTwo = new DataOutputStream(outSocket.getOutputStream());
				}
			}
			System.out.println("Total routing time-" + (System.nanoTime() - startTime));

			/* Communication loop that runs until the end of the stream is reached */
			while ((dataIn = in.read()) != -1) {
				if (outSocket != null) {
					// Send: send the current byte of data to the destination
					outTwo.write(dataIn);
				}

				// Flush the stream to force the buffered output bytes to be written
				outTwo.flush();
			} // end while

			System.out.println("Finished sending the file!");
			System.exit(0);

		} // end try
		catch (IOException e) {
			System.err.println("Could not listen to socket.");
			System.exit(1);
		}
	}

}