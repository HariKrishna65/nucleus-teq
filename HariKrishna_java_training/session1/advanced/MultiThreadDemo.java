package session1.advanced;

class Task extends Thread {

    private String name;

    public Task(String name) {
        this.name = name;
    }

    public void run() {
        System.out.println(name + " is running");
    }
}

public class MultiThreadDemo {

    public static void main(String[] args) {

        Task t1 = new Task("Thread-1");
        Task t2 = new Task("Thread-2");

        t1.start();
        t2.start();
    }
}