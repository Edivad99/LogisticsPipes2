package logisticspipes.logisticspipes;

import org.jetbrains.annotations.Nullable;
import net.minecraft.core.Direction;

/**
 * This class is responsible for handling items arriving at its destination
 *
 * @author Krapht
 */
public abstract class TransportLayer {

  public abstract boolean stillWantItem(IRoutedItem item);

  @Nullable
  public abstract Direction itemArrived(IRoutedItem item, @Nullable Direction denied);

  public void handleItem(IRoutedItem item) {}
}
