package logisticspipes.world.inventory.slot;

import lombok.Getter;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class LogisticsPipesSlot extends Slot {

  @Getter
  private boolean phantom;
  protected boolean canAdjustPhantom = true;
  private int stackLimit = -1;

  public LogisticsPipesSlot(Container container, int slot, int x, int y) {
    super(container, slot, x, y);
  }

  @Override
  public boolean mayPlace(ItemStack stack) {
    return this.container.canPlaceItem(this.getSlotIndex(), stack);
  }

  protected LogisticsPipesSlot setPhantom() {
    this.phantom = true;
    return this;
  }

  public LogisticsPipesSlot setCanAdjustPhantom(boolean canAdjust) {
    this.canAdjustPhantom = canAdjust;
    return this;
  }

  protected LogisticsPipesSlot setStackLimit(int limit) {
    this.stackLimit = limit;
    return this;
  }

  @Override
  public final int getMaxStackSize() {
    int max = super.getMaxStackSize();
    return this.stackLimit < 0 ? max : Math.min(max, this.stackLimit); // issue #1347
  }

  public boolean canAdjustPhantom() {
    return this.canAdjustPhantom;
  }

  @Override
  public boolean mayPickup(Player player) {
    return !this.isPhantom();
  }
}
