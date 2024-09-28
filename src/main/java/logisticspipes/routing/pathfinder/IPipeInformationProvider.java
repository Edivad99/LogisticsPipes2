package logisticspipes.routing.pathfinder;

import java.util.stream.Stream;
import org.jetbrains.annotations.Nullable;
import logisticspipes.interfaces.routing.IFilter;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.transport.LPTravelingItem;
import logisticspipes.utils.ConnectionType;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public interface IPipeInformationProvider {

  boolean isCorrect(ConnectionType connectionType);

  @Nullable
  Level getLevel();

  BlockEntity getBlockEntity();

  default int getX() {
    return getBlockEntity().getBlockPos().getX();
  }

  default int getY() {
    return getBlockEntity().getBlockPos().getY();
  }

  default int getZ() {
    return getBlockEntity().getBlockPos().getZ();
  }

  boolean isRouterInitialized();

  boolean isRoutingPipe();

  CoreRoutedPipe getRoutingPipe();

  @Nullable
  BlockEntity getNextConnectedBlockEntity(Direction direction);

  default boolean isFirewallPipe() {
    // TODO: Implement
    return false;
  }

  default IFilter getFirewallFilter() {
     /*if (pipe instanceof PipeItemsFirewall) {
      return ((PipeItemsFirewall) pipe).getFilter();
    }*/
    throw new RuntimeException("This is no firewall pipe");
  }

  default boolean divideNetwork() {
    return false;
  }

  default boolean powerOnly() {
    return false;
  }

  default boolean isOnewayPipe() {
    return false;
  }

  default boolean isOutputClosed(Direction direction) {
    return false;
  }

  boolean canConnect(BlockEntity to, Direction direction, boolean ignoreSystemDisconnection);

  @Deprecated
  default boolean canConnect(BlockEntity to, Direction direction) {
    return canConnect(to, direction, false);
  }

  double getDistance();

  double getDistanceWeight();

  //boolean isItemPipe();

  //boolean isFluidPipe();

  //boolean isPowerPipe();

  //double getDistanceTo(int destinationint, Direction ignore, Item ident, boolean isActive, double traveled, double max, List<DoubleCoordinates> visited);

  boolean acceptItem(LPTravelingItem item, BlockEntity from);

  //void refreshTileCacheOnSide(Direction side);

  default boolean isMultiBlock() {
    return false;
  }

  default Stream<BlockEntity> getPartsOfPipe() {
    return Stream.of();
  }
}
