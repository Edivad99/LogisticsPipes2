package logisticspipes.interfaces;

import org.jetbrains.annotations.Nullable;
import logisticspipes.api.IRoutedPowerProvider;
import logisticspipes.fromkotlin.Adjacent;
import logisticspipes.logisticspipes.IRoutedItem;
import logisticspipes.modules.LogisticsModule;
import logisticspipes.pipes.basic.debug.DebugLogController;
import logisticspipes.utils.CacheHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;

public interface IPipeServiceProvider extends IRoutedPowerProvider,
    ISpawnParticles, ISendRoutedItem {

  boolean isNthTick(int n);

  DebugLogController getDebug();

  CacheHolder getCacheHolder();

  BlockPos getPos();

  void setChanged();

  /**
   * @return the available adjacent cache.
   */
  Adjacent getAvailableAdjacent();

  /**
   * Only makes sense to use this on the chassis pipe.
   */
  @Nullable
  default Direction getPointedOrientation() {
    return null;
  }

  /**
   * to interact and send items you need to know about orders, upgrades, and have the ability to send
   */
  //LogisticsItemOrderManager getItemOrderManager();

  void queueRoutedItem(IRoutedItem routedItem, @Nullable Direction from);

  ISlotUpgradeManager getUpgradeManager(@Nullable LogisticsModule.ModulePositionType slot, int positionInt);

  int countOnRoute(Item item);
}
