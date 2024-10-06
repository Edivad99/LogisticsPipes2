package logisticspipes.routing;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.jetbrains.annotations.Nullable;
import logisticspipes.interfaces.routing.IAdditionalTargetInformation;
import logisticspipes.logisticspipes.IRoutedItem;
import logisticspipes.proxy.MainProxy;
import logisticspipes.routing.order.IDistanceTracker;
import logisticspipes.utils.item.ItemIdentifierStack;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

public class ItemRoutingInformation {

  public static class DelayComparator implements Comparator<ItemRoutingInformation> {

    @Override
    public int compare(ItemRoutingInformation o1, ItemRoutingInformation o2) {
      return (int) (o2.getTimeOut() - o1.getTimeOut()); // cast will never overflow because the delta is in 1/20ths of a second.
    }
  }

  private static final Map<UUID, ItemRoutingInformation> STORE_MAP = new HashMap<>();

  public int destinationint = -1;
  @Nullable
  public UUID destinationUUID;
  public boolean arrived;
  public int bufferCounter = 0;
  public boolean _doNotBuffer;
  public IRoutedItem.TransportMode _transportMode = IRoutedItem.TransportMode.UNKNOWN;
  public List<Integer> jamlist = new ArrayList<>();
  @Nullable
  public IDistanceTracker tracker = null;
  public IAdditionalTargetInformation targetInfo;

  private long delay = 640 + MainProxy.getGlobalTick();

  @Getter
  @Setter
  @Nullable
  private ItemIdentifierStack item;

  public void readFromNBT(HolderLookup.Provider provider, CompoundTag tag) {
    if (tag.contains("destinationUUID")) {
      destinationUUID = UUID.fromString(tag.getString("destinationUUID"));
    }
    arrived = tag.getBoolean("arrived");
    bufferCounter = tag.getInt("bufferCounter");
    _transportMode = IRoutedItem.TransportMode.values()[tag.getInt("transportMode")];
    this.item = ItemIdentifierStack.parse(provider, tag.getCompound("Item"));
  }

  public void writeToNBT(HolderLookup.Provider provider, CompoundTag tag) {
    if (destinationUUID != null) {
      tag.putString("destinationUUID", destinationUUID.toString());
    }
    tag.putBoolean("arrived", arrived);
    tag.putInt("bufferCounter", bufferCounter);
    tag.putInt("transportMode", _transportMode.ordinal());
    tag.put("Item", this.item.save(provider));
  }

  // the global LP tick in which getTickToTimeOut returns 0.
  public long getTimeOut() {
    return delay;
  }

  // how many ticks until this times out
  public long getTickToTimeOut() {
    return delay - MainProxy.getGlobalTick();
  }

  public void resetDelay() {
    delay = 640 + MainProxy.getGlobalTick();
    if (tracker != null) {
      tracker.setDelay(delay);
    }
  }

  public void setItemTimeout() {
    delay = MainProxy.getGlobalTick() - 1;
    if (tracker != null) {
      tracker.setDelay(delay);
    }
  }

  @Override
  public String toString() {
    return String.format("(%s, %d, %s, %s, %s, %d, %s)", item, destinationint, destinationUUID, _transportMode, jamlist, delay, tracker);
  }

  @Override
  public ItemRoutingInformation clone() {
    ItemRoutingInformation that = new ItemRoutingInformation();
    that.destinationint = destinationint;
    that.destinationUUID = destinationUUID;
    that.arrived = arrived;
    that.bufferCounter = bufferCounter;
    that._doNotBuffer = _doNotBuffer;
    that._transportMode = _transportMode;
    that.jamlist = new ArrayList<>(jamlist);
    that.tracker = tracker;
    that.targetInfo = targetInfo;
    that.item = new ItemIdentifierStack(getItem());
    return that;
  }

  public void storeToNBT(HolderLookup.Provider provider, CompoundTag tag) {
    UUID uuid = UUID.randomUUID();
    tag.putString("StoreUUID", uuid.toString());
    this.writeToNBT(provider, tag);
    STORE_MAP.put(uuid, this);
  }

  public static ItemRoutingInformation restoreFromNBT(HolderLookup.Provider provider,
      CompoundTag tag) {
    if (tag.contains("StoreUUID")) {
      UUID uuid = UUID.fromString(tag.getString("StoreUUID"));
      if (STORE_MAP.containsKey(uuid)) {
        ItemRoutingInformation result = STORE_MAP.get(uuid);
        STORE_MAP.remove(uuid);
        return result;
      }
    }
    ItemRoutingInformation info = new ItemRoutingInformation();
    info.readFromNBT(provider, tag);
    return info;
  }
}
