package logisticspipes.ticks;

import java.util.concurrent.PriorityBlockingQueue;
import logisticspipes.Configs;

public class RoutingTableUpdateThread extends Thread {

  private static final PriorityBlockingQueue<Runnable> UPDATE_CALLS = new PriorityBlockingQueue<>();

  private static Long AVERAGE = 0L;

  public RoutingTableUpdateThread(int i) {
    super("LogisticsPipes RoutingTableUpdateThread #" + i);
    setDaemon(true);
    setPriority(Configs.MULTI_THREAD_PRIORITY);
    start();
  }

  public static void add(Runnable run) {
    UPDATE_CALLS.add(run);
  }

  public static boolean remove(Runnable run) {
    return UPDATE_CALLS.remove(run);
  }

  public static int size() {
    return UPDATE_CALLS.size();
  }

  public static long getAverage() {
    synchronized (AVERAGE) {
      return AVERAGE;
    }
  }

  @Override
  public void run() {
    Runnable item;
    // take blocks until things are available, no need to check
    try {
      while ((item = UPDATE_CALLS.take()) != null) {
        long starttime = System.nanoTime();
        item.run();
        long took = System.nanoTime() - starttime;
        synchronized (AVERAGE) {
          if (AVERAGE == 0) {
            AVERAGE = took;
          } else {
            AVERAGE = ((AVERAGE * 999L) + took) / 1000L;
          }
        }
      }
    } catch (InterruptedException ignored) {

    }
  }

}
