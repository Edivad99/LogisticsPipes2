package logisticspipes.logisticspipes;

import java.util.LinkedList;
import javax.annotation.Nullable;
import logisticspipes.fromkotlin.WorldCoordinatesWrapper;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.routing.IRouter;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;

public class PipeTransportLayer extends TransportLayer {

  private final CoreRoutedPipe routedPipe;
  private final ITrackStatistics trackStatistics;
  private final IRouter router;

  public PipeTransportLayer(CoreRoutedPipe routedPipe, ITrackStatistics _trackStatistics, IRouter router) {
    this.routedPipe = routedPipe;
    this.trackStatistics = _trackStatistics;
    this.router = router;
  }

  @Override
  public boolean stillWantItem(IRoutedItem item) {
    // pipes are dumb and always want the item
    return true;
  }

  @Override
  public Direction itemArrived(IRoutedItem item, @Nullable Direction denied) {
    if (item.getItemIdentifierStack() != null) {
      this.trackStatistics.receivedItem(item.getItemIdentifierStack().getCount());
    }

    // 1st priority, deliver to adjacent inventories
    LinkedList<Direction> possibleEnumFacing = new LinkedList<>();
    for (NeighborBlockEntity<BlockEntity> adjacent : routedPipe.getAvailableAdjacent().inventories()) {
      if (this.router.isRoutedExit(adjacent.getDirection())) {
        continue;
      }
      if (denied != null && denied.equals(adjacent.getDirection())) {
        continue;
      }

      CoreRoutedPipe pipe = this.router.getPipe();
      if (pipe != null) {
        if (pipe.isLockedExit(adjacent.getDirection())) {
          continue;
        }
      }

      possibleEnumFacing.add(adjacent.getDirection());
    }
    if (!possibleEnumFacing.isEmpty()) {
      return possibleEnumFacing.get(routedPipe.getLevel().random.nextInt(possibleEnumFacing.size()));
    }

    // 2nd priority, deliver to non-routed exit
    new WorldCoordinatesWrapper(routedPipe.container)
        .connectedTileEntities()
        .stream()
        .filter(neighbor -> {
          if (this.router.isRoutedExit(neighbor.getDirection())) return false;
          final CoreRoutedPipe routerPipe = this.router.getPipe();
          return routerPipe == null || !routerPipe.isLockedExit(neighbor.getDirection());
        })
        .forEach(neighbor -> possibleEnumFacing.add(neighbor.getDirection()));

    if (possibleEnumFacing.isEmpty()) {
      // last resort, drop item
      return null;
    } else {
      return possibleEnumFacing.get(routedPipe.getLevel().random.nextInt(possibleEnumFacing.size()));
    }
  }
}
