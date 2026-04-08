package session1.exception;

public class ExceptionHandlingDemo {

    public static void main(String[] args) {

        try {
            int result = 10 / 0; // error
            System.out.println(result);
        } catch (ArithmeticException e) {
            System.out.println("Exception caught: " + e.getMessage());
        } finally {
            System.out.println("Program finished");
        }
    }
}