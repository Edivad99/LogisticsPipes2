package logisticspipes.world.inventory;

import logisticspipes.util.item.ModuleInventory;
import logisticspipes.world.inventory.slot.PhantomSlot;
import lombok.Getter;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

@Getter
public class ItemSinkModuleMenu extends LogisticsPipesMenu {

  private final ModuleInventory moduleInventory;
  private final ItemStack itemstack;

  public ItemSinkModuleMenu(int id, Inventory inventory, ModuleInventory moduleInventory) {
    super(LogisticsPipesMenuTypes.ITEM_SINK.get(), id, inventory.player, __ -> true);
    this.moduleInventory = moduleInventory;
    this.moduleInventory.startOpen(inventory.player);
    this.itemstack = this.moduleInventory.getItemStack();

    for (int i = 0; i < 9; i++) {
      this.addSlot(new PhantomSlot(this.moduleInventory, i, 8 + i * 18, 18));
    }

    this.addInventorySlots(inventory, 142);
  }

  @Override
  public void removed(Player player) {
    super.removed(player);
    this.moduleInventory.stopOpen(player);
  }
}
