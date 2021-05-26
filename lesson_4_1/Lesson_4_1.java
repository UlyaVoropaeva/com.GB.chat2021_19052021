public class Lesson_4_1 {

    private static Object monitor = new Object();
    private static volatile char currentNum = 'A';
    private static final int number = 5;

    public static void main(String[] args) {
        LitterPrint A = new LitterPrint('A','B');
        LitterPrint B = new LitterPrint('B','C');
        LitterPrint C = new LitterPrint('C','A');

        A.start();
        B.start();
        C.start();

    }

    public static class LitterPrint  extends Thread{

        private char literprint;
        private char liternext;

        public LitterPrint (char literprint, char liternext){
            this.liternext=liternext;
            this.literprint=literprint;
        }

        @Override
        public void run() {
            try {
                for (int i = 0; i < number; i++) {
                    synchronized (monitor) {
                    while (currentNum != literprint) {
                        monitor.wait();
                    }
                    System.out.print(literprint);
                    currentNum = liternext;
                    monitor.notifyAll();
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        }
    }
}