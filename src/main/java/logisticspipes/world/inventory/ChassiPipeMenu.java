package logisticspipes.world.inventory;

import logisticspipes.world.inventory.slot.ModuleSlot;
import logisticspipes.world.level.block.entity.ChassiPipeBlockEntity;
import lombok.Getter;
import net.minecraft.world.entity.player.Inventory;

@Getter
public class ChassiPipeMenu extends LogisticsPipesMenu {

  private final ChassiPipeBlockEntity blockEntity;

  public ChassiPipeMenu(int id, Inventory inventory, ChassiPipeBlockEntity blockEntity) {
    super(LogisticsPipesMenuTypes.CHASSI_PIPE.get(), id, inventory.player, blockEntity::isStillValid);
    this.blockEntity = blockEntity;

    this.addSlot(new ModuleSlot(this.blockEntity.getContainer(), 0, 9, 9));
    this.addSlot(new ModuleSlot(this.blockEntity.getContainer(), 1, 9, 29));

    this.addInventorySlots(inventory, 187);
  }
}
