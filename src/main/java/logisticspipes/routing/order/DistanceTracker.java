package logisticspipes.routing.order;

import logisticspipes.proxy.MainProxy;

public class DistanceTracker implements IDistanceTracker {

  private int currentDistance = 0;
  private int initialDistance = 0;
  private boolean endReached = false;
  private long delay = 0;

  @Override
  public void setCurrentDistanceToTarget(int value) {
    if (this.initialDistance == 0) {
      this.initialDistance = value;
    }
    this.currentDistance = value;
  }

  @Override
  public int getCurrentDistanceToTarget() {
    return this.currentDistance;
  }

  @Override
  public int getInitialDistanceToTarget() {
    return this.initialDistance;
  }

  @Override
  public void setDestinationReached() {
    this.endReached = true;
  }

  @Override
  public boolean hasReachedDestination() {
    return this.endReached;
  }

  @Override
  public void setDelay(long delay) {
    this.delay = delay;
  }

  @Override
  public boolean isTimeout() {
    return this.delay != 0 && this.delay <= MainProxy.getGlobalTick();
  }
}
