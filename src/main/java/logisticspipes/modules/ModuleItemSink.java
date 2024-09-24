package logisticspipes.modules;

import java.util.Arrays;
import org.jetbrains.annotations.Nullable;
import logisticspipes.utils.SinkReply;
import logisticspipes.world.level.block.entity.ChassiPipeBlockEntity;
import lombok.Getter;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.SimpleContainer;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;

public class ModuleItemSink extends LogisticsModule {

  @Getter
  private final SimpleContainer requestedItemsInventory = new SimpleContainer(9);
  @Getter
  private boolean defaultRoute;

  private SinkReply sinkReply;
  private SinkReply sinkReplyDefault;

  public ModuleItemSink() {
    this.requestedItemsInventory.addListener(__ -> this.service.setChanged());
  }

  @Override
  public void registerPosition(ModulePositionType slot, int positionInt) {
    super.registerPosition(slot, positionInt);
    this.sinkReply = new SinkReply(SinkReply.FixedPriority.ITEM_SINK, 0, true, false, 1, 0,
        new ChassiPipeBlockEntity.ChassiTargetInformation(this.getPositionInt()));
    this.sinkReplyDefault = new SinkReply(SinkReply.FixedPriority.DEFAULT_ROUTE, 0, true, true, 1, 0,
        new ChassiPipeBlockEntity.ChassiTargetInformation(this.getPositionInt()));
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

  public void setDefaultRoute(boolean defaultRoute) {
    this.defaultRoute = defaultRoute;
    this.service.setChanged();
  }

  @Nullable
  private IItemHandler getInventory(Direction direction) {
    var level = this.getLevel();
    if (level == null) {
      return null;
    }
    return level.getCapability(Capabilities.ItemHandler.BLOCK, this.getPos().relative(direction), null);
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
