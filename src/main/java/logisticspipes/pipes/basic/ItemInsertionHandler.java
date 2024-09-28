package logisticspipes.pipes.basic;

import java.util.ArrayList;
import java.util.List;
import logisticspipes.interfaces.ILPItemAcceptor;
import logisticspipes.world.level.block.entity.LogisticsGenericPipeBlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;

public class ItemInsertionHandler<T extends CoreUnroutedPipe> implements IItemHandler {

  public static final List<ILPItemAcceptor> ACCEPTORS = new ArrayList<>();

  private final LogisticsGenericPipeBlockEntity<T> pipe;
  private final Direction dir;

  public ItemInsertionHandler(LogisticsGenericPipeBlockEntity<T> pipe, Direction dir) {
    this.pipe = pipe;
    this.dir = dir;
  }

  @Override
  public int getSlots() {
    return 2;
  }

  @Override
  public ItemStack getStackInSlot(int slot) {
    return ItemStack.EMPTY;
  }

  @Override
  public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
    if (!simulate) {
      return handleItemInsertion(pipe, dir, stack);
    }
    return ItemStack.EMPTY;
  }

  @Override
  public ItemStack extractItem(int slot, int amount, boolean simulate) {
    return ItemStack.EMPTY;
  }

  @Override
  public int getSlotLimit(int slot) {
    return 64;
  }

  @Override
  public boolean isItemValid(int slot, ItemStack stack) {
    return false;
  }

  public static <T extends CoreUnroutedPipe> ItemStack handleItemInsertion(
      LogisticsGenericPipeBlockEntity<T> pipe, Direction from, ItemStack stack) {
    for (ILPItemAcceptor acceptor : ACCEPTORS) {
      if (acceptor.accept(pipe, from, stack)) {
        return ItemStack.EMPTY;
      }
    }
    return pipe.insertItem(from, stack);
  }
}
