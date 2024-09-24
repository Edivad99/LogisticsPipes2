package logisticspipes.interfaces;

import net.minecraft.world.item.ItemStack;

public interface IInventoryUtil {

  /**
   * Inventory space count which terminates when space for max items are
   * found.
   *
   * @return spaces found. If this is less than max, then there are only
   * spaces for that amount.
   */
  int roomForItem(ItemStack stack);

  // IInventory adapter
  int getSizeInventory();

  ItemStack getStackInSlot(int slot);

  ItemStack decrStackSize(int slot, int amount);
}
