package logisticspipes.ticks;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.Callable;
import logisticspipes.transport.LPTravelingItem;
import net.minecraft.util.Tuple;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

public class QueuedTasks {
  @SuppressWarnings("rawtypes")
  private static final LinkedList<Callable> QUEUE = new LinkedList<>();

  // called on server shutdown only.
  public static void clearAllTasks() {
    QueuedTasks.QUEUE.clear();
  }

  @SuppressWarnings("rawtypes")
  public static void queueTask(Callable task) {
    synchronized (QueuedTasks.QUEUE) {
      QueuedTasks.QUEUE.add(task);
    }
  }

  @SuppressWarnings({ "rawtypes" })
  @SubscribeEvent
  public void tickEnd(ServerTickEvent.Post event) {
    Callable call;
    while (!QueuedTasks.QUEUE.isEmpty()) {
      synchronized (QueuedTasks.QUEUE) {
        call = QueuedTasks.QUEUE.removeFirst();
      }
      if (call != null) {
        try {
          call.call();
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }
    //MainProxy.proxy.tick();
    synchronized (LPTravelingItem.FORCE_KEEP) {
      Iterator<Tuple<Integer, Object>> iter = LPTravelingItem.FORCE_KEEP.iterator();
      while (iter.hasNext()) {
        var pair = iter.next();
        pair.setA(pair.getA() - 1);
        if (pair.getA() < 0) {
          iter.remove();
        }
      }
    }
  }
}
