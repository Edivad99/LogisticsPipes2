package logisticspipes.world.inventory.slot;

import logisticspipes.world.item.LogisticsPipesItems;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

public class ModuleSlot extends LogisticsPipesSlot {

  public ModuleSlot(Container container, int slot, int x, int y) {
    super(container, slot, x, y);
    this.setStackLimit(1);
  }

  @Override
  public boolean mayPlace(ItemStack stack) {
    return stack.is(LogisticsPipesItems.MODULE_ITEM_SINK.get());
  }
}
