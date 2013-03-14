//Copyright (C) 2013  SeongJae Park
//
//This program is free software: you can redistribute it and/or modify
//it under the terms of the GNU General Public License as published by
//the Free Software Foundation, either version 3 of the License, or
//(at your option) any later version.
//
//This program is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU General Public License for more details.
//
//You should have received a copy of the GNU General Public License
//along with this program.  If not, see <http://www.gnu.org/licenses/>

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
