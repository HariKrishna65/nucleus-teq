package session1.advanced;

abstract class Bird {
    abstract void fly();
}

class Sparrow extends Bird {
    void fly() {
        System.out.println("Sparrow is flying");
    }
}

public class AbstractClassExample {

    public static void main(String[] args) {
        Bird bird = new Sparrow();
        bird.fly();
    }
}