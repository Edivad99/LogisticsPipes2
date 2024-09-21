package logisticspipes.util.item;

import org.jetbrains.annotations.Nullable;
import lombok.Getter;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;

public class ModuleInventory implements WorldlyContainer {

  @Getter
  private final ItemStack itemStack;
  @Getter
  private final InteractionHand hand;
  private final NonNullList<ItemStack> items;

  public ModuleInventory(ItemStack itemStack, InteractionHand hand) {
    this.itemStack = itemStack;
    this.hand = hand;
    this.items = NonNullList.withSize(9, ItemStack.EMPTY);
    var contents = this.itemStack.get(DataComponents.CONTAINER);
    if (contents != null) {
      contents.copyInto(this.items);
    }
  }

  @Override
  public int[] getSlotsForFace(Direction side) {
    int size = this.items.size();
    int[] result = new int[size];

    for(int i = 0; i < result.length; i++) {
      result[i] = i;
    }

    return result;
  }

  @Override
  public boolean canPlaceItemThroughFace(int index, ItemStack itemStack,
      @Nullable Direction direction) {
    return true;
  }

  @Override
  public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
    return true;
  }

  @Override
  public int getContainerSize() {
    return this.items.size();
  }

  @Override
  public boolean isEmpty() {
    return this.items.stream().allMatch(ItemStack::isEmpty);
  }

  @Override
  public ItemStack getItem(int slot) {
    return this.items.get(slot);
  }

  @Override
  public ItemStack removeItem(int slot, int amount) {
    ItemStack result = ContainerHelper.removeItem(this.items, slot, amount);
    if (!result.isEmpty()) {
      this.setChanged();
    }
    return result;
  }

  @Override
  public ItemStack removeItemNoUpdate(int slot) {
    return ContainerHelper.takeItem(this.items, slot);
  }

  @Override
  public void setItem(int slot, ItemStack stack) {
    this.items.set(slot, stack);
    if (stack.getCount() > this.getMaxStackSize()) {
      stack.setCount(this.getMaxStackSize());
    }
  }

  @Override
  public void setChanged() {
    this.itemStack.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(this.items));
  }

  @Override
  public boolean stillValid(Player player) {
    return true;
  }

  @Override
  public void clearContent() {
    this.items.clear();
  }
}
