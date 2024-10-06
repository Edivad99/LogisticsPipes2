package logisticspipes.utils.transactor;

import net.minecraft.world.item.ItemStack;

public interface IInvSlot {

  boolean canPutStackInSlot(ItemStack stack);

  ItemStack getStackInSlot();

  ItemStack insertItem(ItemStack stack, boolean simulate);

  ItemStack extractItem(int amount, boolean simulate);

  int getSlotLimit();
}
