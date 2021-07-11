package ru.geekbrains.home.chat.task1ABC;

public class mainThread {
    private static final Object o = new Object();
    private static char lastLetter = 'C';

    public static void main(String[] args) {
        Thread thread1 = new LetterThread('C', 'A');
        Thread thread2 = new LetterThread('A', 'B');
        Thread thread3 = new LetterThread('B', 'C');

        thread1.start();
        thread2.start();
        thread3.start();
    }
    private static class LetterThread extends Thread {
        private final char first;
        private final char last;

        public LetterThread(char first, char last) {
            this.first = first;
            this.last = last;
        }

        @Override
        public void run() {
            try {
                for (int i = 0; i < 5; i++) {
                    synchronized (o) {
                        while (lastLetter != first) {
                            o.wait();
                        }
                        System.out.print(last);
                        lastLetter = last;
                        o.notifyAll();
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
