package logisticspipes.proxy.specialconnection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import logisticspipes.interfaces.routing.ISpecialTileConnection;
import logisticspipes.logisticspipes.IRoutedItem;
import net.minecraft.world.level.block.entity.BlockEntity;

public class SpecialTileConnection {

  private final List<ISpecialTileConnection> handler = new ArrayList<>();

  public void registerHandler(ISpecialTileConnection connectionHandler) {
    if (connectionHandler.init()) {
      handler.add(connectionHandler);
    }
  }

  public Collection<BlockEntity> getConnectedPipes(BlockEntity blockEntity) {
    for (ISpecialTileConnection connectionHandler : handler) {
      if (connectionHandler.isType(blockEntity)) {
        return connectionHandler.getConnections(blockEntity);
      }
    }
    return new ArrayList<>();
  }

  public boolean needsInformationTransition(BlockEntity blockEntity) {
    for (ISpecialTileConnection connectionHandler : handler) {
      if (connectionHandler.isType(blockEntity)) {
        return connectionHandler.needsInformationTransition();
      }
    }
    return false;
  }

  public void transmit(BlockEntity blockEntity, IRoutedItem arrivingItem) {
    for (ISpecialTileConnection connectionHandler : handler) {
      if (connectionHandler.isType(blockEntity)) {
        connectionHandler.transmit(blockEntity, arrivingItem);
        break;
      }
    }
  }

  public boolean isType(BlockEntity blockEntity) {
    for (ISpecialTileConnection connectionHandler : handler) {
      if (connectionHandler.isType(blockEntity)) {
        return true;
      }
    }
    return false;
  }
}

