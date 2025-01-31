package logisticspipes.asm.te;

import java.util.ArrayList;
import java.util.List;
import logisticspipes.utils.CacheHolder;

public class LPTileEntityObject {

  public List<ITileEntityChangeListener> changeListeners = new ArrayList<>();
  public long initialised = 0;

  private CacheHolder cacheHolder;

  public CacheHolder getCacheHolder() {
    if (cacheHolder == null) {
      cacheHolder = new CacheHolder();
    }
    return cacheHolder;
  }

  public void trigger(CacheHolder.CacheTypes type) {
    if (cacheHolder != null) {
      getCacheHolder().trigger(type);
    }
  }
}
