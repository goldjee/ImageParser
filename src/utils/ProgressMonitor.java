package utils;

/**
 * Created by Ins on 25.03.2019.
 */
public class ProgressMonitor {

    private volatile int cntDone;

    private volatile int cntAll;

    public ProgressMonitor() {
        reset();
    }

    public synchronized void reset() {
        cntDone = 0;
        cntAll = 0;
    }

    public synchronized void increment() {
        cntDone++;
        printProgress();
    }

    private synchronized void printProgress() {
        System.out.print("\rProgress: " + getProgress() + "%");
        System.out.flush();

        if (isFinished())
            System.out.println();
    }

    private synchronized double getProgress() {
        return (double) (Math.round((double) cntDone / cntAll * 10000)) / 100;
    }

    public synchronized void setCntAll(int cntAll) {
        reset();
        this.cntAll = cntAll;
    }

    public synchronized boolean isFinished() {
        return cntDone >= cntAll;
    }
}
