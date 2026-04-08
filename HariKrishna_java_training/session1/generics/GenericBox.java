package session1.generics;

public class GenericBox<T> {

    private T value;

    public void setValue(T value) {
        this.value = value;
    }

    public T getValue() {
        return value;
    }

    public static void main(String[] args) {

        GenericBox<Integer> intBox = new GenericBox<>();
        intBox.setValue(10);
        System.out.println("Integer Value: " + intBox.getValue());

        GenericBox<String> strBox = new GenericBox<>();
        strBox.setValue("Hello");
        System.out.println("String Value: " + strBox.getValue());
    }
}