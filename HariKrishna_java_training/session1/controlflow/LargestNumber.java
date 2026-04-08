package session1.controlflow;

public class LargestNumber {

    public static void main(String[] args) {
        int a = 10, b = 25, c = 15;

        int largest = Math.max(a, Math.max(b, c));
        System.out.println("Largest: " + largest);
    }
}