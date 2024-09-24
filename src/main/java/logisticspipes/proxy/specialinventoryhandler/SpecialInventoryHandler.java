package logisticspipes.proxy.specialinventoryhandler;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import logisticspipes.interfaces.IInventoryUtil;
import logisticspipes.utils.ProviderMode;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;

public abstract class SpecialInventoryHandler implements IInventoryUtil {

  public interface Factory {

    boolean init();

    boolean isType(@NotNull BlockEntity blockEntity, @Nullable Direction dir);

    @Nullable
    SpecialInventoryHandler getUtilForTile(@NotNull BlockEntity blockEntity, @Nullable Direction dir, @NotNull ProviderMode mode);
  }
}
