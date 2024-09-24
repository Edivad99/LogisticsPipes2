package logisticspipes.interfaces;

import org.jetbrains.annotations.Nullable;
import net.minecraft.world.level.Level;

public interface ILevelProvider {

  @Nullable
  Level getLevel();
}
