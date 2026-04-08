package session1.advanced;

interface Animal {
    void sound();
}

class Dog implements Animal {
    public void sound() {
        System.out.println("Dog barks");
    }
}

public class InterfaceExample {

    public static void main(String[] args) {
        Animal obj = new Dog();
        obj.sound();
    }
}