package logisticspipes.pipes.upgrades;

import java.util.UUID;
import org.jetbrains.annotations.Nullable;
import logisticspipes.interfaces.IPipeUpgradeManager;
import logisticspipes.interfaces.ISlotUpgradeManager;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.utils.DoubleCoordinates;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;

public class UpgradeManager implements IPipeUpgradeManager, ISlotUpgradeManager {

  private final CoreRoutedPipe pipe;

  /* cached attributes */
  @Nullable
  private UUID uuid = null;

  public UpgradeManager(CoreRoutedPipe pipe) {
    this.pipe = pipe;
  }

  @Nullable
  public UUID getSecurityID() {
    return this.uuid;
  }

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
    return new Direction[0];
  }

  @Override
  public boolean hasPatternUpgrade() {
    return false;
  }

  @Override
  public boolean isAdvancedSatelliteCrafter() {
    return false;
  }

  @Override
  public boolean hasByproductExtractor() {
    return false;
  }

  @Override
  public int getFluidCrafter() {
    return 0;
  }

  @Override
  public boolean isFuzzyUpgrade() {
    return false;
  }

  @Override
  public int getCrafterCleanup() {
    return 0;
  }

  @Override
  public boolean hasSneakyUpgrade() {
    return false;
  }

  @Override
  public Direction getSneakyOrientation() {
    return null;
  }

  @Override
  public boolean hasOwnSneakyUpgrade() {
    return false;
  }

  @Override
  public Container getInv() {
    return null;
  }

  @Override
  public IPipeUpgrade getUpgrade(int slot) {
    return null;
  }

  @Override
  public DoubleCoordinates getPipePosition() {
    return null;
  }

  @Override
  public int getActionSpeedUpgrade() {
    return 0;
  }

  @Override
  public int getItemExtractionUpgrade() {
    return 0;
  }

  @Override
  public int getItemStackExtractionUpgrade() {
    return 0;
  }

  public void securityTick() {
    /*if ((getSecurityID()) != null) {
      if (!SimpleServiceLocator.securityStationManager.isAuthorized(uuidS)) {
        securityDelay++;
      } else {
        securityDelay = 0;
      }
      if (securityDelay > 20) {
        secInv.clearInventorySlotContents(0);
        InventoryChanged(secInv);
      }
    }*/
  }

  public void dropUpgrades() {

  }
}
