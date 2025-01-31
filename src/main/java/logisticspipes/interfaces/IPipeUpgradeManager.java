package logisticspipes.interfaces;

import org.jetbrains.annotations.Nullable;
import net.minecraft.core.Direction;

public interface IPipeUpgradeManager {

  boolean hasPowerPassUpgrade();

  boolean hasRFPowerSupplierUpgrade();

  boolean hasBCPowerSupplierUpgrade();

  int getIC2PowerLevel();

  int getSpeedUpgradeCount();

  boolean isSideDisconnected(Direction side);

  boolean hasCCRemoteControlUpgrade();

  boolean hasCraftingMonitoringUpgrade();

  boolean isOpaque();

  boolean hasUpgradeModuleUpgrade();

  boolean hasCombinedSneakyUpgrade();

  @Nullable
  Direction[] getCombinedSneakyOrientation();
}
