package logisticspipes.routing.pathfinder;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import org.jetbrains.annotations.Nullable;
import logisticspipes.utils.ConnectionType;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;

public class PipeInformationManager {

  private Map<Class<?> /*TileEntity*/, Class<? extends IPipeInformationProvider>> infoProvider = new HashMap<>();

  public boolean isItemPipe(BlockEntity blockEntity) {
    return this.isPipe(blockEntity, true, ConnectionType.ITEM);
  }

  public boolean isPipe(@Nullable BlockEntity blockEntity) {
    return this.isPipe(blockEntity, true, ConnectionType.UNDEFINED);
  }

  public boolean isPipe(@Nullable BlockEntity blockEntity, boolean check, ConnectionType connectionType) {
    switch (blockEntity) {
      case null -> {
        return false;
      }
      case IPipeInformationProvider informationProvider -> {
        return true;
      }
      case ISubMultiBlockPipeInformationProvider subMultiBlockPipeInformationProvider -> {
        return connectionType == ConnectionType.MULTIBLOCK;
      }
      default -> {
        for (Class<?> type : infoProvider.keySet()) {
          if (type.isAssignableFrom(blockEntity.getClass())) {
            try {
              IPipeInformationProvider provider = infoProvider.get(type)
                  .getDeclaredConstructor(type)
                  .newInstance(type.cast(blockEntity));
              if (!check || provider.isCorrect(connectionType)) {
                return true;
              }
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                     IllegalArgumentException | NoSuchMethodException | SecurityException e) {
              e.printStackTrace();
            }
          }
        }
      }
    }
    return false;
  }

  @Nullable
  public IPipeInformationProvider getInformationProviderFor(@Nullable BlockEntity blockEntity) {
    switch (blockEntity) {
      case null -> {
        return null;
      }
      case IPipeInformationProvider provider -> {
        return provider;
      }
      case ISubMultiBlockPipeInformationProvider provider -> {
        return provider.getMainTile();
      }
      default -> {
        for (Class<?> type : infoProvider.keySet()) {
          if (type.isAssignableFrom(blockEntity.getClass())) {
            try {
              IPipeInformationProvider provider = infoProvider.get(type)
                  .getDeclaredConstructor(type).newInstance(type.cast(blockEntity));
              if (provider.isCorrect(ConnectionType.UNDEFINED)) {
                return provider;
              }
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                     IllegalArgumentException | SecurityException | NoSuchMethodException e) {
              e.printStackTrace();
            }
          }
        }
      }
    }
    return null;
  }

  public boolean canConnect(IPipeInformationProvider startPipe, IPipeInformationProvider provider,
      Direction direction, boolean flag) {
    return startPipe.canConnect(provider.getBlockEntity(), direction, flag) &&
        provider.canConnect(startPipe.getBlockEntity(), direction.getOpposite(), flag);
  }

  public boolean isNotAPipe(BlockEntity blockEntity) {
    if (blockEntity instanceof IPipeInformationProvider) {
      return false;
    } else if (blockEntity instanceof ISubMultiBlockPipeInformationProvider) {
      return false;
    } else {
      for (Class<?> type : infoProvider.keySet()) {
        if (type.isAssignableFrom(blockEntity.getClass())) {
          return false;
        }
      }
    }
    return true;
  }
}
