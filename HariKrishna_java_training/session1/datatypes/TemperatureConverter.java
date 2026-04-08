package session1.datatypes;

public class TemperatureConverter {

    public static void main(String[] args) {

        double celsius = 25;
        double fahrenheit = cToF(celsius);

        System.out.println("Celsius to Fahrenheit: " + fahrenheit);

        double f = 77;
        double c = fToC(f);

        System.out.println("Fahrenheit to Celsius: " + c);
    }

    public static double cToF(double c) {
        return (c * 9 / 5) + 32;
    }

    public static double fToC(double f) {
        return (f - 32) * 5 / 9;
    }
}