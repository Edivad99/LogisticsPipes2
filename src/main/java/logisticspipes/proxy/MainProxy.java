package logisticspipes.proxy;

import org.jetbrains.annotations.Nullable;
import logisticspipes.routing.pathfinder.IPipeInformationProvider;
import lombok.Getter;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;

public class MainProxy {

  @Getter
  private static int globalTick;

  public static void addTick() {
    globalTick++;
  }

  public static boolean checkPipesConnections(@Nullable BlockEntity from, @Nullable BlockEntity to,
      Direction way) {
    return MainProxy.checkPipesConnections(from, to, way, false);
  }

  public static boolean checkPipesConnections(@Nullable BlockEntity from, @Nullable BlockEntity to,
      Direction way, boolean ignoreSystemDisconnection) {
    if (from == null || to == null) {
      return false;
    }
    IPipeInformationProvider fromInfo = SimpleServiceLocator.pipeInformationManager.getInformationProviderFor(from);
    IPipeInformationProvider toInfo = SimpleServiceLocator.pipeInformationManager.getInformationProviderFor(to);
    if (fromInfo == null && toInfo == null) {
      return false;
    }
    if (fromInfo != null) {
      if (!fromInfo.canConnect(to, way, ignoreSystemDisconnection)) {
        return false;
      }
    }
    if (toInfo != null) {
      return toInfo.canConnect(from, way.getOpposite(), ignoreSystemDisconnection);
    }
    return true;
  }
}
