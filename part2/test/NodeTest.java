package part2.test;

import org.junit.Test;

import part2.exceptions.NotConnectedToRouterException;
import part2.nodes.Node;

public class NodeTest {

    @Test
    // Test if case 3 properly closes the sockets
    public void testMenuCloseSocket() {
        // Connect this node to RouterOne
        try {
            Node node = new Node("192.168.1.13", 5557);

        } catch (final NotConnectedToRouterException e) {
            System.err.println(e.getMessage());
        }
    }

}
