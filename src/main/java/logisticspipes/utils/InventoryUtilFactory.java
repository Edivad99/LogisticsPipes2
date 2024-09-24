package logisticspipes.utils;

import java.util.ArrayList;
import java.util.Objects;
import javax.annotation.Nonnull;
import org.jetbrains.annotations.Nullable;
import logisticspipes.LogisticsPipes;
import logisticspipes.interfaces.IInventoryUtil;
import logisticspipes.proxy.specialinventoryhandler.SpecialInventoryHandler;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;

public class InventoryUtilFactory {

  private final ArrayList<SpecialInventoryHandler.Factory> handlerFactories = new ArrayList<>();

  public void registerHandler(@Nonnull SpecialInventoryHandler.Factory handlerFactory) {
    if (handlerFactory.init()) {
      handlerFactories.add(handlerFactory);
      LogisticsPipes.LOG.info("Loaded SpecialInventoryHandler.Factory: {}", handlerFactory.getClass().getCanonicalName());
    } else {
      LogisticsPipes.LOG.warn("Could not load SpecialInventoryHandler.Factory: {}", handlerFactory.getClass().getCanonicalName());
    }
  }

  @Nullable
  public IInventoryUtil getInventoryUtil(BlockEntity blockEntity, Direction direction) {
    return getHidingInventoryUtil(blockEntity, direction, ProviderMode.DEFAULT);
  }

  @Nullable
  public IInventoryUtil getHidingInventoryUtil(@Nullable BlockEntity blockEntity, @Nullable Direction direction, ProviderMode mode) {
    if (blockEntity != null) {
      IInventoryUtil util = getSpecialHandlerFor(blockEntity, direction, mode);
      if (util != null) {
        return util;
      } else {
        var level = blockEntity.getLevel();
        var cap = level.getCapability(Capabilities.ItemHandler.BLOCK, blockEntity.getBlockPos(), direction);
        if (cap != null) {
          return new InventoryUtil(cap, mode);
        }
      }
    }
    return null;
  }

  @Nullable
  private IInventoryUtil getSpecialHandlerFor(BlockEntity blockEntity, @Nullable Direction direction, ProviderMode mode) {
    return this.handlerFactories.stream()
        .filter(factory -> factory.isType(blockEntity, direction))
        .map(factory -> factory.getUtilForTile(blockEntity, direction, mode))
        .filter(Objects::nonNull)
        .findAny()
        .orElse(null);
  }
}
