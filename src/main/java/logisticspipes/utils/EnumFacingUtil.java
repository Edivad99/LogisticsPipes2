package logisticspipes.utils;

import org.jetbrains.annotations.Nullable;
import net.minecraft.core.Direction;

public class EnumFacingUtil {

  @Nullable
  public static Direction getOrientation(int input) {
    if (input < 0 || Direction.values().length <= input) {
      return null;
    }
    return Direction.values()[input];
  }
}
