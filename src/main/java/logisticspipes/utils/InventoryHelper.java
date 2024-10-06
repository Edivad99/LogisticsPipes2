package logisticspipes.utils;

import org.jetbrains.annotations.Nullable;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.utils.transactor.ITransactor;
import logisticspipes.utils.transactor.TransactorSimple;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;

public class InventoryHelper {

  //BC getTransactorFor using our getInventory
  @Nullable
  public static ITransactor getTransactorFor(Object object, Direction dir) {
    if (object instanceof BlockEntity blockEntity) {
      ITransactor t = SimpleServiceLocator.inventoryUtilFactory
          .getSpecialHandlerFor(blockEntity, dir, ProviderMode.DEFAULT);
      if (t != null) {
        return t;
      }

      var level = blockEntity.getLevel();
      var pos = blockEntity.getBlockPos();
      var cap = level.getCapability(Capabilities.ItemHandler.BLOCK, pos, dir);
      if (cap != null) {
        return new TransactorSimple(cap);
      }
    }
    return null;
  }
}
