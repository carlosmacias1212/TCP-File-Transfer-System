import java.io.*;

public class JavaFileWriting {
    public static void main(String[] args) {

        String inputFilePath = "short.mp4";
        String outputFilePath = "out.mp4";

        // Read in the contents from the input file to the output file
        FileInputStream inputBytes;
        FileOutputStream outputBytes;
        try {
            inputBytes = new FileInputStream(inputFilePath);

            outputBytes = new FileOutputStream(outputFilePath);

            // Write the contents of the output stream to a file

            int contents;
            while ((contents = inputBytes.read()) != -1) {
                outputBytes.write(contents);
            }

            inputBytes.close();
            outputBytes.close();

        } catch (FileNotFoundException e) {
            System.err.println("A FileNotFoundException occurred!");
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("An IOException occurred!");
            e.printStackTrace();
        }

    }

}
