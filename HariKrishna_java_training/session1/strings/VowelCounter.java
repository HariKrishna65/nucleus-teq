package session1.strings;

public class VowelCounter {

    public static void main(String[] args) {
        String str = "education";
        int count = 0;

        for (char c : str.toCharArray()) {
            if ("aeiou".indexOf(c) != -1) count++;
        }

        System.out.println("Vowels: " + count);
    }
}