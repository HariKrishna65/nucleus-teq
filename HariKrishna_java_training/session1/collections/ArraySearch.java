package session1.collections;

public class ArraySearch {

    public static void main(String[] args) {
        int[] arr = {10, 20, 30};
        int key = 20;

        int index = -1;

        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == key) index = i;
        }

        System.out.println("Index: " + index);
    }
}