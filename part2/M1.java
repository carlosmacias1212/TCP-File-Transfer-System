package part2;

import part2.exceptions.NotConnectedToRouterException;
import part2.nodes.Node;

// This is for running a node that connects to RouterTwo
public class M1 {

    public static void main(final String[] args) {

        // Connect this node to RouterTwo
        Node node;
        try {
            node = new Node("192.168.1.5", 5557);

            // Get user input
            node.menu();
        } catch (final NotConnectedToRouterException e) {
            System.err.println(e.getMessage());
        }
    }

}
