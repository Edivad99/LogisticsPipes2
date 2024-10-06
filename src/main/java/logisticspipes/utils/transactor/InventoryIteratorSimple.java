package logisticspipes.utils.transactor;

import java.util.Iterator;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;

public class InventoryIteratorSimple implements Iterable<IInvSlot> {

  private final IItemHandler itemHandler;

  public InventoryIteratorSimple(IItemHandler itemHandler) {
    this.itemHandler = itemHandler;
  }

  @Override
  public Iterator<IInvSlot> iterator() {
    return new Iterator<>() {

      int slot = 0;

      @Override
      public boolean hasNext() {
        return this.slot < itemHandler.getSlots();
      }

      @Override
      public IInvSlot next() {
        return new InvSlot(this.slot++);
      }
    };
  }

  private class InvSlot implements IInvSlot {

    private final int slot;

    public InvSlot(int slot) {
      this.slot = slot;
    }

    @Override
    public ItemStack getStackInSlot() {
      return itemHandler.getStackInSlot(this.slot);
    }

    @Override
    public ItemStack insertItem(ItemStack stack, boolean simulate) {
      return itemHandler.insertItem(this.slot, stack.copy(), simulate);
    }

    @Override
    public ItemStack extractItem(int amount, boolean simulate) {
      return itemHandler.extractItem(this.slot, amount, simulate);
    }

    @Override
    public int getSlotLimit() {
      return itemHandler.getSlotLimit(this.slot);
    }

    @Override
    public boolean canPutStackInSlot(ItemStack stack) {
      var toTest = stack.copyWithCount(1);
      return itemHandler.insertItem(this.slot, toTest, true).isEmpty();
    }
  }
}
