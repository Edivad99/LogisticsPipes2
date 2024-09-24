package logisticspipes.routing.pathfinder;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import org.jetbrains.annotations.Nullable;
import logisticspipes.utils.ConnectionType;
import net.minecraft.world.level.block.entity.BlockEntity;

public class PipeInformationManager {

  private Map<Class<?> /*TileEntity*/, Class<? extends IPipeInformationProvider>> infoProvider = new HashMap<>();

  public boolean isItemPipe(BlockEntity blockEntity) {
    return this.isPipe(blockEntity, true, ConnectionType.ITEM);
  }

  private boolean isPipe(@Nullable BlockEntity blockEntity, boolean check, ConnectionType connectionType) {
    if (blockEntity == null) {
      return false;
    }
    if (blockEntity instanceof IPipeInformationProvider) {
      return true;
    } else if (blockEntity instanceof ISubMultiBlockPipeInformationProvider) {
      return connectionType == ConnectionType.MULTIBLOCK;
    } else {
      for (Class<?> type : infoProvider.keySet()) {
        if (type.isAssignableFrom(blockEntity.getClass())) {
          try {
            IPipeInformationProvider provider = infoProvider.get(type)
                .getDeclaredConstructor(type)
                .newInstance(type.cast(blockEntity));
            if (!check || provider.isCorrect(connectionType)) {
              return true;
            }
          } catch (InstantiationException | IllegalAccessException | InvocationTargetException | IllegalArgumentException | NoSuchMethodException | SecurityException e) {
            e.printStackTrace();
          }
        }
      }
    }
    return false;
  }
}
