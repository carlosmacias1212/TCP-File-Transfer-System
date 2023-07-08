package part2.routers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * This represents a thread for the router to accept incoming connections.
 */
public class ServerThread extends Thread {
	private final Object[][] RTable; // routing table
	private PrintWriter out; // writers (for writing back to the machine and to destination)
	private final BufferedReader in; // reader (for reading from the machine connected to)
	private String addr; // communication strings
	private Socket outSocket; // socket for communicating with a destination

	/**
	 * this creates a new thread for a router which listens for incoming
	 * connections. These connections can be from another router or a node.
	 * 
	 * @param Table  2D object array which is the routing table for this
	 *               router. This routing table consists of IP address, sockets,
	 *               and names.
	 * @param toHost Socket object that the router is using for connection to
	 *               another host
	 * @param index  integer index of the host's position in the routing table
	 * @param name   String name that describes the socket that is used in this
	 *               thread
	 * @throws IOException
	 */
	ServerThread(final Object[][] Table, final Socket toHost, final int index, final String name) throws IOException {
		out = new PrintWriter(toHost.getOutputStream(), true); // socket's output
		in = new BufferedReader(new InputStreamReader(toHost.getInputStream())); // socket's input
		RTable = Table; // routing table
		addr = toHost.getInetAddress().getHostAddress(); // connected host's address
		RTable[index][0] = addr; // IP address of the connected host
		RTable[index][1] = toHost; // socket for communication with the host
		RTable[index][2] = name; // name of the socket
	}

	/**
	 * a thread that handles communication between a host and a router. This runs
	 * for each machine that connects to the router. This will listen for messages
	 * that get sent to the router via the thread's socket.
	 * Different actions will be taken depending on the messsage received.
	 */
	public void run() {
		try {

			// Keep listening for messages
			while (true) {

				final String input = in.readLine();

				// Shutdown the router if the stream has closed
				if (input == null) {
					System.out.println("The router has been shutdown.");
					System.exit(0);
				}

				System.out.println("Router received the message: " + input);

				/*
				 * Split the message seperated by :
				 * message[0] - senderType (node, router)
				 * message[1] - messageType (request, response, disconnect)
				 * message[2] - address of the node who initiated the request
				 * message[3] - address of the peer that the node wants to connect to
				 * 
				 * message[4] - response that states whether or not a node can connect to a
				 * peer. NOTE: only used when senderType is 'router' and messageType is
				 * 'response'
				 */

				final String[] message = input.split(":");

				// senderType can be "node" or "router"
				final String senderType = message[0];

				// messageType can be "request", "response", or disconnect.
				// "request":
				// if a destination IP is being requested for a connection.
				// "response":
				// if a router is sending a response on if a node's peer can be connected to
				// "disconnect":
				// a type of request, where a host asks to be disconnected from its router and
				// removed from the routing table
				final String messageType = message[1];

				// The address of the node who initiated the request
				final String origin = message[2];

				// The address of the peer the node wants to connect to
				final String destination = message[3];

				// a router receives destination from a node
				if (senderType.equals("node") && messageType.equals("request")) {
					System.out.println("A router received a destination from a node!");
					// Send the destination to the other router

					final String add = (String) RTable[0][0];
					final String name = (String) RTable[0][2];
					final Socket sockToRouter = (Socket) RTable[0][1];

					final PrintWriter outWriter = new PrintWriter(sockToRouter.getOutputStream(), true);
					outWriter.println("router:request:" + origin + ":" + destination);

					System.out.println("Sent: router:request:" + origin + ":" + destination + "\nFrom: " + name);
				}
				// a router receives a destination from another router
				else if (senderType.equals("router") && messageType.equals("request")) {
					System.out.println("A router received a destination from another router!");

					// Check if the destination is in this router's routing table
					// loops through the routing table to find the destination
					String canConnect = "NO";

					// Don't need to check index 0 because that is a router not a node
					for (int i = 1; i < 10; i++) {
						addr = (String) RTable[i][0];

						if (destination.equals(addr)) {
							System.out.println("Found destination: " + destination);
							canConnect = "YES";
							break;
						}
					}

					// Send the response to the router
					outSocket = (Socket) RTable[0][1];
					out = new PrintWriter(outSocket.getOutputStream(), true);
					out.println("router:response:" + origin + ":" + destination + ":" + canConnect);

				}
				// a router receives a response from another router
				else if (senderType.equals("router") && messageType.equals("response")) {
					// send the response to the original node
					System.out.println("a router received a response from a router!");
					// Send to node
					// Don't need to check index 0 because that is a router not a node
					for (int i = 1; i < 10; i++) {
						addr = (String) RTable[i][0];
						final String name = (String) RTable[i][2];
						System.out.println("Addr: " + addr);
						System.out.println("Socket: " + name);
						if (origin.equals(addr)) {
							System.out.println("Found origin: " + origin);
							outSocket = (Socket) RTable[i][1];
							break;
						}
					}
					out = new PrintWriter(outSocket.getOutputStream(), true);
					final String canConnect = message[4];
					out.println(canConnect);

				}

				// a router receives a request to disconnect a node
				else if (senderType.equals("node") && (messageType.equals("disconnect"))) {

					System.out.println("Disconnecting a node from this router...");
					final String[] receivedSocket = socketToArr(destination);

					// Find the received socket in the routing table
					for (int i = 0; i < RTable.length; i++) {
						if (RTable[i][0] != null) {
							// Represent the current socket as a string array
							final String[] currentSock = socketToArr(RTable[i][1].toString());

							// If the sockets are identical remove them from the routing table
							if (socketsAreIdentical(receivedSocket, currentSock)) {
								RTable[i][0] = null;

								((Socket) RTable[i][1]).close();
								RTable[i][1] = null;
								RTable[i][2] = null;
								System.out.println(
										"The node was succesfully disconnected from the router and removed from the routing table!");
								break;
							}
						} else {
							break;
						}

					}
					break;
				}

				else {
					System.err.println("The message received was an incorrect format!");
					System.exit(1);
				}

				// waits 10 seconds to let the routing table fill with all machines' information
				try {
					Thread.currentThread().sleep(10000);
				} catch (final InterruptedException ie) {
					System.out.println("Thread interrupted");
				}
			}

		} catch (final IOException e) {
			System.out.println("\nThe router was shutdown.");
			System.exit(0);
		}
	}

	/**
	 * convert a string representation of a socket to an array. This function is
	 * used when comparing sockets.
	 * 
	 * @param socket string representation of a socket. Needs to be of the format:
	 *               Socket[addr=/IP_ADDRESS,port=PORT_#,localport=LOCAL_PORT_#]
	 * @return an array of strings that represents that socket. Format:
	 *         [IP_ADDRESS, port=PORT_#, localport=LOCAL_PORT_#]
	 */
	private String[] socketToArr(final String socket) {

		// Split by commas
		final String[] arr = socket.toString().split(",");

		// IP Address
		arr[0] = arr[0].split("/")[1];

		// Local Port #
		arr[2] = arr[2].split("]")[0];
		return arr;
	}

	//
	/**
	 * check if two string array representations of sockets are identical.
	 * Format of a socket's string representation:
	 * [IP_ADDRESS, port=PORT_#, localport=LOCAL_PORT_#]
	 * 
	 * @param sockOne a string array representation of the socket
	 * @param sockTwo a string array representation of the socket
	 * @return true if they refer to the same socket, false otherwise
	 */
	private boolean socketsAreIdentical(final String[] sockOne, final String[] sockTwo) {
		final String socketOneAddress = sockOne[0];
		final String socketTwoAddress = sockTwo[0];

		// Check if the addresses are not the same
		if (!(socketOneAddress.equals(socketTwoAddress))) {
			return false;
		}

		final int socketOnePort = Integer.parseInt(sockOne[1].split("=")[1]);
		final int socketTwoPort = Integer.parseInt(sockTwo[1].split("=")[1]);

		final int socketOneLocalPort = Integer.parseInt(sockOne[2].split("=")[1]);
		final int socketTwoLocalPort = Integer.parseInt(sockTwo[2].split("=")[1]);

		// The sockets are identical except their ports and localPorts are switched
		if (socketOnePort == socketTwoLocalPort && socketTwoPort == socketOneLocalPort) {
			return true;
		}
		// Check if the ports are not the same
		else if (socketOnePort != socketTwoPort) {
			return false;
		}
		// Check if the localPorts are not the same
		else if (socketOneLocalPort != socketTwoLocalPort) {
			return false;
		}
		// The sockets are completely identical
		else {
			return true;
		}

	}
}