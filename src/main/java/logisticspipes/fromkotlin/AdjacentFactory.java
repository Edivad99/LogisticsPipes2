package logisticspipes.fromkotlin;

import java.util.Objects;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.utils.ConnectionType;
import net.minecraft.core.Direction;

public class AdjacentFactory {

  public static AdjacentFactory INSTANCE = new AdjacentFactory();

  public Adjacent createAdjacentCache(CoreRoutedPipe parent) {
    var connectedTileEntities =
        new WorldCoordinatesWrapper(Objects.requireNonNull(parent.container))
            .connectedTileEntities()
            .stream()
            .filter(x -> SimpleServiceLocator.pipeInformationManager.isNotAPipe(x.getBlockEntity()) && !parent.isSideBlocked(x.getDirection(), false))
            .toList();

    // FIXME: container.canPipeConnect(neighbor.getTileEntity(), neighbor.getDirection()))
    // the above will check InventoryUtil.getSizeInventory() > 0 (which is wrong)
    // it is similar to SimpleServiceLocator.pipeInformationManager.isNotAPipe

    if (connectedTileEntities.isEmpty()) {
      return NoAdjacent.INSTANCE;
    } else if (connectedTileEntities.size() == 1) {
      return new SingleAdjacent(parent, connectedTileEntities.getFirst().getDirection(), ConnectionType.UNDEFINED);
    } else {
      var arr = new ConnectionType[Direction.values().length];
      for (var neighbor : connectedTileEntities) {
        arr[neighbor.getDirection().ordinal()] = ConnectionType.UNDEFINED;
      }
      return new DynamicAdjacent(parent, arr);
    }
  }
}
