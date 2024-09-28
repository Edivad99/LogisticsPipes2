package logisticspipes.client.renderer.blockentity.state;

import org.jetbrains.annotations.Nullable;
import logisticspipes.Configs;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.basic.CoreUnroutedPipe;
import lombok.Getter;
import net.minecraft.core.Direction;

public class TextureMatrix {

  //Old Pipe Renderer
  private final int[] iconIndexes = new int[7];

  //New Pipe Renderer
  @Getter
  private int textureIndex;
  @Getter
  private boolean isRouted;
  private boolean[] isRoutedInDir = new boolean[6];
  private boolean[] isSubPowerInDir = new boolean[6];
  @Getter
  private boolean hasPowerUpgrade;
  @Getter
  private boolean hasPower;
  @Getter
  private boolean isFluid;
  @Getter
  @Nullable
  private Direction pointedOrientation;

  private boolean dirty = true;

  public int getTextureIndex(Direction direction) {
    return iconIndexes[direction.ordinal()];
  }

  public void setIconIndex(Direction direction, int value) {
    if (iconIndexes[direction.ordinal()] != value) {
      iconIndexes[direction.ordinal()] = value;
      dirty = true;
    }
  }

  public void refreshStates(CoreUnroutedPipe pipe) {
    //TODO: FIXME
    /*if (textureIndex != pipe.getTextureIndex()) {
      dirty = true;
    }
    textureIndex = pipe.getTextureIndex();*/
    if (isRouted != pipe.isRoutedPipe()) {
      dirty = true;
    }
    isRouted = pipe.isRoutedPipe();
    if (isRouted) {
      CoreRoutedPipe cPipe = (CoreRoutedPipe) pipe;
      for (int i = 0; i < 6; i++) {
        if (isRoutedInDir[i] != cPipe.getRouter().isRoutedExit(Direction.from3DDataValue(i))) {
          dirty = true;
        }
        isRoutedInDir[i] = cPipe.getRouter().isRoutedExit(Direction.from3DDataValue(i));
      }
      for (int i = 0; i < 6; i++) {
        if (isSubPowerInDir[i] != cPipe.getRouter().isSubPoweredExit(Direction.from3DDataValue(i))) {
          dirty = true;
        }
        isSubPowerInDir[i] = cPipe.getRouter().isSubPoweredExit(Direction.from3DDataValue(i));
      }
      if (hasPowerUpgrade != (cPipe.getUpgradeManager().hasRFPowerSupplierUpgrade() || cPipe.getUpgradeManager().getIC2PowerLevel() > 0)) {
        dirty = true;
      }
      hasPowerUpgrade = cPipe.getUpgradeManager().hasRFPowerSupplierUpgrade() || cPipe.getUpgradeManager().getIC2PowerLevel() > 0;
      if (hasPower != (cPipe.textureBufferPowered || Configs.LOGISTICS_POWER_USAGE_DISABLED)) {
        dirty = true;
      }
      hasPower = cPipe.textureBufferPowered || Configs.LOGISTICS_POWER_USAGE_DISABLED;
      if (isFluid != cPipe.isFluidPipe()) {
        dirty = true;
      }
      isFluid = cPipe.isFluidPipe();
      if (pointedOrientation != cPipe.getPointedOrientation()) {
        dirty = true;
      }
      pointedOrientation = cPipe.getPointedOrientation();
    } else {
      isRoutedInDir = new boolean[6];
    }
  }

  public boolean isRoutedInDir(Direction dir) {
    if (dir == null) {
      return false;
    }
    return isRoutedInDir[dir.ordinal()];
  }

  public boolean isSubPowerInDir(Direction dir) {
    if (dir == null) {
      return false;
    }
    return isSubPowerInDir[dir.ordinal()];
  }

  public boolean isDirty() {
    return dirty;
  }

  public void clean() {
    dirty = false;
  }
}
