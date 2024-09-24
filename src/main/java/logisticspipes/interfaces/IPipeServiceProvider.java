package logisticspipes.interfaces;

import org.jetbrains.annotations.Nullable;
import logisticspipes.api.IRoutedPowerProvider;
import net.minecraft.core.BlockPos;

public interface IPipeServiceProvider {

  @Nullable
  BlockPos getPos();

  void setChanged();
}
