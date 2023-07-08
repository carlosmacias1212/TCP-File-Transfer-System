import java.io.*;
import java.net.*;

public class TCPServer {
   public static void main(String[] args) throws IOException {

      /* Variables for setting up connection and communication */
      // socket to connect with the router
      Socket socket = null;

      // for writing to the router
      DataOutputStream dataOut = null;

      // for reading raw data from the router
      DataInputStream dataIn = null;

      // router host name
      String routerName = "192.168.1.9";

      // port number
      int SockNum = 5555;

      // FileOutputStream for writing recieved data to a file
      FileOutputStream fileOutput = new FileOutputStream("out.mp4");

      /* Tries to connect to the router */
      try {

         System.out.println("\n==========================================");
         // Connect to the router's socket
         socket = new Socket(routerName, SockNum);

         // Print that the socket is connected
         if (socket.isConnected()) {
            System.out.println("SERVER is connected to the router!");
         } else {
            System.out.println("SERVER is NOT connected to the router!");
         }
         System.out.println("==========================================");
         System.out.println("SERVER");
         System.out.println("==========================================\n");

         // DataOutputStream used for writing string data to the router's socket
         dataOut = new DataOutputStream(socket.getOutputStream());

         // DataInputStream used for reading raw data from the router's socket
         dataIn = new DataInputStream(socket.getInputStream());

      } catch (UnknownHostException e) {
         System.err.println("Don't know about router: " + routerName);
         System.exit(1);
      } catch (IOException e) {
         System.err.println("Couldn't get I/O for the connection to: " + routerName);
         System.exit(1);
      }

      /* Inital sends/recieves */
      // Send (initial): send the destination ip to the router
      // destination IP (Client)
      String address = "192.168.1.8";
      System.out.println("Output - address");

      // Write the address as a string to the dataOut
      dataOut.writeUTF(address);

      // Flush the dataOut to ensure that all of the buffered bytes are written
      dataOut.flush();

      System.out.println("Sent! - " + address + "\n");

      // Receive (initial): get confirmation that data was received from the router
      String initial;
      System.out.println("Input - initial");
      initial = dataIn.readUTF();
      System.out.println("Received! - " + initial);

      System.out.println("Waiting for file data from the client...");
      System.out.println("==========================================");

      // Start time of communication
      float startTime = System.nanoTime();

      // Counts how many times the while loop needs to write data
      int amountOfTransfers = 0;

      // Loop until the end of the DataInputStream is reached
      int data;
      while ((data = dataIn.read()) != -1) {
         // Send: send the current byte of data to the file
         fileOutput.write(data);
         amountOfTransfers++;
      }
      System.out.println("Total time for communcation is-" + (System.nanoTime() - startTime));
      System.out.println("Number of Transfers/Received File Size-" + amountOfTransfers);
      System.out.println("Avg time needed to send message-" + ((System.nanoTime() - startTime) / amountOfTransfers));

      System.out.println("Finished receiving the file!");

      // closing connections
      fileOutput.close();
      dataOut.close();
      dataIn.close();
      socket.close();

      System.exit(0);

   }
}
