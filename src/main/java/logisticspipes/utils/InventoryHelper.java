package logisticspipes.utils;

import org.jetbrains.annotations.Nullable;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.utils.transactor.ITransactor;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;

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
    }

    /*if (object instanceof ICapabilityProvider && ((ICapabilityProvider) object).hasCapability
    (CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, dir)) {
      return new TransactorSimple(((ICapabilityProvider) object).getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, dir));
    }*/

    return null;

  }
}
