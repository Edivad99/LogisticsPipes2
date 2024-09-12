package logisticspipes.world.inventory.slot;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;

public class PhantomSlot extends LogisticsPipesSlot {

  public PhantomSlot(Container container, int index, int x, int y) {
    super(container, index, x, y);
    this.setPhantom();
    this.setStackLimit(1);
  }
}
