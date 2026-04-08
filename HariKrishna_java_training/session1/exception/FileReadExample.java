package session1.exception;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class FileReadExample {

    public static void main(String[] args) {

        try {
            BufferedReader br = new BufferedReader(new FileReader("test.txt"));

            String line = br.readLine();
            System.out.println("File Content: " + line);

            br.close();
        } catch (IOException e) {
            System.out.println("File Error: " + e.getMessage());
        }
    }
}