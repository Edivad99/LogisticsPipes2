package logisticspipes.utils;

import logisticspipes.interfaces.IInventoryUtil;
import logisticspipes.interfaces.ISpecialInsertion;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;

public class InventoryUtil implements IInventoryUtil, ISpecialInsertion {

  protected final IItemHandler inventory;
  private final ProviderMode mode;

  public InventoryUtil(IItemHandler inventory, ProviderMode mode) {
    this.inventory = inventory;
    this.mode = mode;
  }

  @Override
  public int roomForItem(ItemStack stack) {
    // Special casing for "unlimited" storage items
    if (this.inventory.getSlots() == 1 && this.inventory.getSlotLimit(0) == Integer.MAX_VALUE) {
      ItemStack content = this.inventory.extractItem(0, Integer.MAX_VALUE, true);
      if (content.isEmpty()) {
        return Integer.MAX_VALUE;
      }
      return Integer.MAX_VALUE - content.getCount();
    }

    int totalRoom = 0;
    for (int i = 0; i < this.inventory.getSlots() && stack.getCount() > totalRoom; i++) {
      // stack.copy() because other BlockEntities might modify stack.
      // "This must not be modified by the item handler." lol
      ItemStack leftover = this.inventory.insertItem(i, stack.copy(), true);
      totalRoom += stack.getCount() - leftover.getCount();
    }
    return totalRoom;
  }

  @Override
  public int getSizeInventory() {
    return this.inventory.getSlots();
  }

  @Override
  public ItemStack getStackInSlot(int slot) {
    return this.inventory.getStackInSlot(slot);
  }

  @Override
  public ItemStack decrStackSize(int slot, int amount) {
    return this.inventory.extractItem(slot, amount, false);
  }

  @Override
  public int addToSlot(ItemStack stack, int slot) {
    int wanted = stack.getCount();
    ItemStack rest = inventory.insertItem(slot, stack.copy(), false);
    return wanted - rest.getCount();
  }
}
