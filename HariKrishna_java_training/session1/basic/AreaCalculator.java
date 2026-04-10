package session1.basic;

import java.util.Scanner;

public class AreaCalculator {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.println("1.Circle 2.Rectangle 3.Triangle");
        int choice = sc.nextInt();

        Shape shape = null;

        switch (choice) {
            case 1:
                System.out.print("Enter radius: ");
                shape = new Circle(sc.nextDouble());
                break;
            case 2:
                System.out.print("Enter length and width: ");
                shape = new Rectangle(sc.nextDouble(), sc.nextDouble());
                break;
            case 3:
                System.out.print("Enter base and height: ");
                shape = new Triangle(sc.nextDouble(), sc.nextDouble());
                break;
            default:
                System.out.println("Invalid choice");
                return;
                
        }

        System.out.println("Area: " + shape.calculateArea());
        sc.close();
    }
}