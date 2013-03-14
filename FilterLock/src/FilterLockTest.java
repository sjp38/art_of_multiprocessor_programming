public class FilterLockTest {

    public static class FilterLock {
        static int mDepth = 0;
        static volatile int[] mLevel;
        static volatile int[] mVictim;

        public static void setDepth(int depth) {
            mDepth = depth;
            mLevel = new int[mDepth];
            mVictim = new int[mDepth];
        }

        public static void lock(int myId) {
            for (int level = 1; level < mDepth; level++) {
                mLevel[myId] = level;
                mVictim[level] = myId;
                while (true) {
                    boolean upperExist = false;
                    for (int other = 0; other < mDepth; other++) {
                        if (other == myId) {
                            continue;
                        }
                        if (mLevel[other] >= level) {
                            upperExist = true;
                            break;
                        }
                    }
                    if (!upperExist || mVictim[level] != myId) {
                        break;
                    }
                }
            }

        }

        public static void unlock(int myId) {
            mLevel[myId] = 0;
        }
    }

    static class SimpleWork implements Runnable {
        int mId = 0;

        public SimpleWork(int id) {
            mId = id;
        }

        public void run() {
            try {
                Thread.currentThread();
                Thread.sleep(10);
            } catch (InterruptedException e) {
                System.out.println("Failed to sleep");
            }
            FilterLock.lock(mId);
            System.out.print("[" + mId + "]");
            for (int i = mId; i < mId + 10; i++) {
                System.out.print(i + " ");
            }
            System.out.println("\n");
            FilterLock.unlock(mId);
        }
    }

    private static int N = 64;

    public static final void main(String[] args) {
        System.out.println("start!");
        FilterLock.setDepth(N);
        Thread workers[] = new Thread[N];
        for (int i = 0; i < N; i++) {
            workers[i] = new Thread(new SimpleWork(i));
            workers[i].start();
        }
        for (int i = 0; i < N; i++) {
            try {
                workers[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Done!");
    }
}
