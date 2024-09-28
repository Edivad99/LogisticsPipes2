package logisticspipes.interfaces.routing;

import logisticspipes.request.resources.IResource;
import logisticspipes.utils.DoubleCoordinates;
import net.minecraft.world.item.Item;

public interface IFilter {

  boolean isBlocked();

  boolean isFilteredItem(Item item);

  boolean isFilteredItem(IResource resultItem);

  boolean blockProvider();

  boolean blockCrafting();

  boolean blockRouting();

  boolean blockPower();

  DoubleCoordinates getLPPosition();
}
