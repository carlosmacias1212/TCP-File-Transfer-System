package part2;

import part2.exceptions.NotConnectedToRouterException;
import part2.nodes.Node;

// This is for running a node that connects to RouterOne
public class N1 {

    public static void main(String[] args) {

        System.out.println("Starting connection to the router...");

        // Connect this node to RouterOne
        Node node;
        try {
            node = new Node("192.168.1.13", 5557);

            // Get user input
            node.menu();

        } catch (NotConnectedToRouterException e) {
            System.err.println(e.getMessage());
        }

    }

}
