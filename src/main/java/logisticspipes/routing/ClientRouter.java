package logisticspipes.routing;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.jetbrains.annotations.Nullable;
import logisticspipes.LogisticsPipes;
import logisticspipes.api.ILogisticsPowerProvider;
import logisticspipes.interfaces.ISubSystemPowerProvider;
import logisticspipes.interfaces.routing.IFilter;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.utils.DoubleCoordinates;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.world.level.block.entity.LogisticsGenericPipeBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.Level;

public class ClientRouter implements IRouter {

  private final Level level;
  private final int xCoord, yCoord, zCoord;

  public ClientRouter(@Nullable UUID identifier, Level level,
      int x, int y, int z) {
    this.level = level;
    this.xCoord = x;
    this.yCoord = y;
    this.zCoord = z;
  }

  @Override
  @Nullable
  public CoreRoutedPipe getPipe() {
    var pos = new BlockPos(this.xCoord, this.yCoord, this.zCoord);
    var blockEntity = this.level.getBlockEntity(pos);
    if (!(blockEntity instanceof LogisticsGenericPipeBlockEntity<?> pipeBlockEntity)) {
      return null;
    }
    if (!(pipeBlockEntity.pipe instanceof CoreRoutedPipe)) {
      return null;
    }
    return (CoreRoutedPipe) pipeBlockEntity.pipe;
  }

  @Override
  @Nullable
  public CoreRoutedPipe getCachedPipe() {
    return this.getPipe();
  }

  @Override
  public UUID getId() {
    return UUID.randomUUID();
  }

  @Override
  public void clearPipeCache() {
  }

  @Override
  public int getSimpleID() {
    return -420;
  }

  @Override
  public boolean isInDim(ResourceKey<Level> dimension) {
    return true;
  }

  @Override
  public boolean isAt(ResourceKey<Level> dimension, int x, int y, int z) {
    return this.xCoord == x && this.yCoord == y && this.zCoord == z;
  }

  @Override
  public DoubleCoordinates getLPPosition() {
    return new DoubleCoordinates(this.xCoord, this.yCoord, this.zCoord);
  }

  @Override
  public boolean isRoutedExit(Direction connection) {
    if (LogisticsPipes.isDebug()) {
      throw new UnsupportedOperationException("noClientRouting");
    }
    return false;
  }

  @Override
  public boolean hasRoute(int id, boolean active, ItemIdentifier item) {
    if (LogisticsPipes.isDebug()) {
      throw new UnsupportedOperationException("noClientRouting");
    }
    return false;
  }

  @Override
  public ExitRoute getExitFor(int id, boolean active, ItemIdentifier item) {
    if (LogisticsPipes.isDebug()) {
      throw new UnsupportedOperationException("noClientRouting");
    }
    return null;
  }

  @Override
  public List<List<ExitRoute>> getRouteTable() {
    if (LogisticsPipes.isDebug()) {
      throw new UnsupportedOperationException("noClientRouting");
    }
    return new ArrayList<>();
  }

  @Override
  public void clearInterests() {
  }

  @Override
  public List<Tuple<ILogisticsPowerProvider, List<IFilter>>> getPowerProvider() {
    return null;
  }

  @Override
  public List<Tuple<ISubSystemPowerProvider, List<IFilter>>> getSubSystemPowerProvider() {
    return null;
  }

  @Override
  public List<ExitRoute> getDistanceTo(IRouter r) {
    return null;
  }

  @Override
  public boolean isSideDisconnected(@Nullable Direction dir) {
    return false;
  }

  @Override
  public void update(boolean doFullRefresh, CoreRoutedPipe pipe) {
  }

  @Override
  public boolean isSubPoweredExit(Direction connection) {
    return false;
  }
}
