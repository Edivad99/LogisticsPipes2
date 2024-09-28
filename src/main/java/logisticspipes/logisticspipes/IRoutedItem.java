package logisticspipes.logisticspipes;

import java.util.List;
import java.util.UUID;
import org.jetbrains.annotations.Nullable;
import logisticspipes.interfaces.routing.IAdditionalTargetInformation;
import logisticspipes.routing.IRouter;
import logisticspipes.routing.ItemRoutingInformation;
import logisticspipes.routing.order.IDistanceTracker;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public interface IRoutedItem {

  enum TransportMode {
    UNKNOWN,
    DEFAULT,
    PASSIVE,
    ACTIVE
  }

  int getDestination();

  UUID getDestinationUUID();

  void setDestination(int destination);

  void clearDestination();

  void setTransportMode(TransportMode transportMode);

  TransportMode getTransportMode();

  void setAdditionalTargetInformation(IAdditionalTargetInformation info);

  IAdditionalTargetInformation getAdditionalTargetInformation();

  void setDoNotBuffer(boolean doNotBuffer);

  boolean getDoNotBuffer();

  int getBufferCounter();

  void setBufferCounter(int counter);

  void setArrived(boolean flag);

  boolean getArrived();

  void addToJamList(IRouter router);

  List<Integer> getJamList();

  void checkIDFromUUID();

  @Nullable
  ItemStack getItemIdentifierStack();

  void readFromNBT(HolderLookup.Provider provider, CompoundTag tag);

  void writeToNBT(HolderLookup.Provider provider, CompoundTag tag);

  void setDistanceTracker(IDistanceTracker tracker);

  @Nullable
  IDistanceTracker getDistanceTracker();

  ItemRoutingInformation getInfo();

  void split(int itemsToTake, Direction orientation);
}
