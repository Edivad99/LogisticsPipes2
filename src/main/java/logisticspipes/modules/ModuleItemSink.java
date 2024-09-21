package logisticspipes.modules;

import java.util.Arrays;
import java.util.Objects;
import org.jetbrains.annotations.Nullable;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;

@Getter
public class ModuleItemSink extends ModuleLogistics {

  public final SimpleContainer requestedItemsInventory = new SimpleContainer(9);
  public boolean defaultRoute;

  @Override
  public void registerModule(Level level, BlockPos pos) {
    this.level = level;
    this.pos = pos;
  }

  @Override
  public CompoundTag serializeNBT(HolderLookup.Provider provider) {
    var tag = new CompoundTag();
    tag.put("requestedItemsInventory", this.requestedItemsInventory.createTag(provider));
    tag.putBoolean("defaultRoute", this.defaultRoute);
    return tag;
  }

  @Override
  public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
    this.requestedItemsInventory.fromTag(tag.getList("requestedItemsInventory", 10), provider);
    this.defaultRoute = tag.getBoolean("defaultRoute");
  }

  @Nullable
  private IItemHandler getInventory(Direction direction) {
    Objects.requireNonNull(this.level, "Module is not registered");
    Objects.requireNonNull(this.pos, "Module is not registered");
    return this.level.getCapability(Capabilities.ItemHandler.BLOCK, this.pos.relative(direction), null);
  }

  public boolean isNearInventory() {
    return Arrays.stream(Direction.values())
        .anyMatch(direction -> this.getInventory(direction) != null);
  }

  public void importFromAdjacentInventory() {
    for (var direction : Direction.values()) {
      var cap = this.getInventory(direction);
      if (cap == null) {
        continue;
      }
      this.requestedItemsInventory.clearContent();
      int requestedItemsIndex = 0;
      for (int i = 0; i < cap.getSlots() && requestedItemsIndex < this.requestedItemsInventory.getContainerSize(); i++) {
        var stack = cap.getStackInSlot(i);
        if (!stack.isEmpty()) {
          this.requestedItemsInventory.setItem(requestedItemsIndex++, stack.copyWithCount(1));
        }
      }
      break;
    }
  }
}
