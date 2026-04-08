package session1.basic;

public class FactorialCalculator {

    public static void main(String[] args) {
        int n = 5;
        long result = factorial(n);
        System.out.println("Factorial: " + result);
    }

    static long factorial(int n) {
        if (n <= 1) return 1;
        return n * factorial(n - 1);
    }
}