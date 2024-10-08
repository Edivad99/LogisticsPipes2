package logisticspipes.routing;

import java.util.List;
import java.util.UUID;
import org.jetbrains.annotations.Nullable;
import logisticspipes.api.ILogisticsPowerProvider;
import logisticspipes.interfaces.ISubSystemPowerProvider;
import logisticspipes.interfaces.routing.IFilter;
import logisticspipes.modules.LogisticsModule;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.utils.DoubleCoordinates;
import logisticspipes.utils.item.ItemIdentifier;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.Level;

public interface IRouter {

  void destroy();

  void update(boolean doFullRefresh, CoreRoutedPipe pipe);

  // TODO: Check if all usages (non-texture related) are OK.
  boolean isRoutedExit(Direction connection);

  boolean isSubPoweredExit(Direction connection);

  int getDistanceToNextPowerPipe(Direction dir);

  boolean hasRoute(int id, boolean active, ItemIdentifier item);

  @Nullable
  ExitRoute getExitFor(int id, boolean active, ItemIdentifier type);

  List<List<ExitRoute>> getRouteTable();

  List<ExitRoute> getIRoutersByCost();

  @Nullable
  CoreRoutedPipe getPipe();

  @Nullable
  CoreRoutedPipe getCachedPipe();

  boolean isInDim(ResourceKey<Level> dimension);

  boolean isAt(ResourceKey<Level> dimension, int x, int y, int z);

  UUID getId();

  @Nullable
  default LogisticsModule getLogisticsModule() {
    CoreRoutedPipe pipe = getPipe();
    if (pipe == null) {
      return null;
    }
    return pipe.getLogisticsModule();
  }

  void clearPipeCache();

  int getSimpleID();

  DoubleCoordinates getLPPosition();

  /* Automated Disconnection */
  boolean isSideDisconnected(@Nullable Direction dir);

  @Nullable
  List<ExitRoute> getDistanceTo(IRouter r);

  void clearInterests();

  @Nullable
  List<Tuple<ILogisticsPowerProvider, List<IFilter>>> getPowerProvider();

  @Nullable
  List<Tuple<ISubSystemPowerProvider, List<IFilter>>> getSubSystemPowerProvider();

  boolean isCacheInvalid();

  //force-update LSA version in the network
  void forceLsaUpdate();

  @Nullable
  List<ExitRoute> getRoutersOnSide(Direction direction);

  //void queueTask(int i, IRouterQueuedTask callable);
}
