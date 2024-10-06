package logisticspipes.pipes.basic;

import logisticspipes.interfaces.IPipeUpgradeManager;
import net.minecraft.core.Direction;

public class UnroutedPipeUpgradeManager implements IPipeUpgradeManager {

  @Override
  public boolean hasPowerPassUpgrade() {
    return false;
  }

  @Override
  public boolean hasRFPowerSupplierUpgrade() {
    return false;
  }

  @Override
  public boolean hasBCPowerSupplierUpgrade() {
    return false;
  }

  @Override
  public int getIC2PowerLevel() {
    return 0;
  }

  @Override
  public int getSpeedUpgradeCount() {
    return 0;
  }

  @Override
  public boolean isSideDisconnected(Direction side) {
    return false;
  }

  @Override
  public boolean hasCCRemoteControlUpgrade() {
    return false;
  }

  @Override
  public boolean hasCraftingMonitoringUpgrade() {
    return false;
  }

  @Override
  public boolean isOpaque() {
    return false;
  }

  @Override
  public boolean hasUpgradeModuleUpgrade() {
    return false;
  }

  @Override
  public boolean hasCombinedSneakyUpgrade() {
    return false;
  }

  @Override
  public Direction[] getCombinedSneakyOrientation() {
    return null;
  }
}
