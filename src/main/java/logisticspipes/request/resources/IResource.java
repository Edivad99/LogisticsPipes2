package logisticspipes.request.resources;

import logisticspipes.routing.IRouter;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * With Destination and amount
 */
public interface IResource {

  Item getAsItem();

  int getRequestedAmount();

  IRouter getRouter();

  boolean matches(IResource resource, MatchSettings settings);

  boolean matches(Item itemType, MatchSettings settings);

  IResource clone(int multiplier);

  boolean mergeForDisplay(IResource resource, int withAmount); //Amount overrides existing amount inside the resource

  IResource copyForDisplayWith(int amount);

  String getDisplayText(ColorCode missing);

  ItemStack getDisplayItem();

  /**
   * Settings only apply for the normal Item Implementation.
   */
  enum MatchSettings {
    NORMAL,
    WITHOUT_NBT
  }

  enum ColorCode {
    NONE,
    MISSING,
    SUCCESS
  }
}
