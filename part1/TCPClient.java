import java.io.*;
import java.net.*;

public class TCPClient {
   public static void main(String[] args) throws IOException {

      /* Variables for setting up connection and communication */
      // socket to connect with the router
      Socket socket = null;

      // for writing to the router
      DataOutputStream dataOut = null;

      // for reading from the router
      DataInputStream dataIn = null;

      // router host name
      String routerName = "192.168.1.9";

      // port number
      int SockNum = 5555;

      // Stream to read in the bytes from a file on the client
      FileInputStream fileInput = null;

      /* Tries to connect to the router */
      try {
         System.out.println("\n==========================================");
         // Connect to the router's socket
         socket = new Socket(routerName, SockNum);
         if (socket.isConnected()) {
            System.out.println("CLIENT is connected to the router!");
         } else {
            System.out.println("CLIENT is NOT connected to the router!");
         }

         // DataOutputStream used for writing data to the router's socket
         dataOut = new DataOutputStream(socket.getOutputStream());

         // DataInputStream used for reading data from the router's socket
         dataIn = new DataInputStream(socket.getInputStream());

      } catch (UnknownHostException e) {
         System.err.println("Don't know about router: " + routerName);
         System.exit(1);
      } catch (IOException e) {
         System.err.println("Couldn't get I/O for the connection to: " + routerName);
         System.exit(1);
      }

      /* Variables for message passing */
      // String messages received from the router
      String fromServer;

      // Destination IP (Server)
      String address = "192.168.1.9";

      /* Communication process (initial sends/receives) */
      // send (initial): send the server's address to the router
      System.out.println("Output - address");
      dataOut.writeUTF(address);

      // Flush the dataOut to ensure that all of the buffered bytes are written
      dataOut.flush();
      System.out.println("Sent! - " + address + "\n");

      // receive (initial): verification of connection from the router
      System.out.println("Input - initial");
      fromServer = dataIn.readUTF();
      System.out.println("Received! - " + fromServer);

      // Take in a stream of data from a file
      fileInput = new FileInputStream("short.mp4");

      // Communication while loop
      System.out.println("Sending file data to the client...");
      System.out.println("==========================================\n");

      // Start time of commucantion
      float startTime = System.nanoTime();
      // Counts how many times the while loop need to write data
      int amountOfTransfers = 0;

      System.out.println("File size-" + fileInput.getChannel().size());

      // Iterate through all of the data from the given file
      int contents;
      while ((contents = fileInput.read()) != -1) {
         // Send: send the current byte of data to the router
         dataOut.write(contents);
         amountOfTransfers++;
      }
      System.out.println("Total time for communcation is-" + (System.nanoTime() - startTime));
      System.out.println("Number of Transfers-" + amountOfTransfers);
      System.out.println("Avg time needed to send message-" + ((System.nanoTime() - startTime) / amountOfTransfers));

      // Flush the dataOut to ensure that all of the buffered bytes are written
      dataOut.flush();
      System.out.println("Sent the file's data!");

      // closing connections
      dataIn.close();
      fileInput.close();
      dataOut.close();
      socket.close();
   }
}
