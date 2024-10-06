package logisticspipes.utils.transactor;

import net.neoforged.neoforge.items.IItemHandler;

public final class InventoryIterator {

  private InventoryIterator() {}

  /**
   * Returns an Iterable object for the specified side of the inventory.
   */
  public static Iterable<IInvSlot> getIterable(IItemHandler itemHandler) {
    return new InventoryIteratorSimple(itemHandler);
  }
}
