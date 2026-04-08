package session1.oop;

public class PolymorphismDemo {

    public static void main(String[] args) {
        PolymorphismDemo obj = new PolymorphismDemo();

        System.out.println(obj.add(2, 3));
        System.out.println(obj.add(2.5, 3.5));
    }

    int add(int a, int b) {
        return a + b;
    }

    double add(double a, double b) {
        return a + b;
    }
}