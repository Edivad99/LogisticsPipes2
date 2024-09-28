package logisticspipes.asm.te;

import logisticspipes.utils.DoubleCoordinates;
import net.minecraft.core.Direction;

public interface ITileEntityChangeListener {

  void pipeRemoved(DoubleCoordinates pos);

  void pipeAdded(DoubleCoordinates pos, Direction side);

}
