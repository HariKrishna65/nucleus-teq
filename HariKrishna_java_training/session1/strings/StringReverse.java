package session1.strings;

public class StringReverse {

    public static void main(String[] args) {
        String str = "hello";

        String reversed = new StringBuilder(str).reverse().toString();
        System.out.println(reversed);
    }
}