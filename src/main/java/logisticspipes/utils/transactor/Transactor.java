package logisticspipes.utils.transactor;

import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;

public abstract class Transactor implements ITransactor {

  @Override
  public ItemStack add(ItemStack stack, Direction orientation, boolean doAdd) {
    var added = stack.copy();
    added.setCount(this.inject(stack, orientation, doAdd));
    return added;
  }

  public abstract int inject(ItemStack stack, Direction orientation, boolean doAdd);
}
