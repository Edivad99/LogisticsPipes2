package logisticspipes.utils.item;


import java.util.Objects;
import lombok.Getter;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * @author Krapht I have no bloody clue what different mods use to differate
 * between items except for itemID, there is metadata, damage, and
 * whatnot. so..... to avoid having to change all my bloody code every
 * time I need to support a new item targeted that would make it a
 * "different" item, I made this cache here A ItemIdentifier is
 * immutable, singleton and most importantly UNIQUE!
 */

@Getter
public final class ItemIdentifier implements Comparable<ItemIdentifier> {

  private final Item item;

  public ItemIdentifier(Item item) {
    this.item = item;
  }

  @Override
  public int compareTo(ItemIdentifier o) {
    return Integer.compare(Item.getId(this.item), Item.getId(o.item));
  }

  public static ItemIdentifier get(ItemStack itemStack) {
    return new ItemIdentifier(itemStack.getItem());
  }

  public ItemStack makeNormalStack(int stackSize) {
    //TODO: Add components
    return new ItemStack(this.item, stackSize);
  }

  public ItemEntity makeItemEntity(int stackSize, Level level, double x, double y, double z) {
    return new ItemEntity(level, x, y, z, makeNormalStack(stackSize));
  }

  public String getFriendlyName(ItemStack stack) {
    return this.item.getName(stack).getString();
  }

  public String getFriendlyName() {
    return this.item.getName(this.makeNormalStack(1)).getString();
  }

  public boolean equalsWithNBT(ItemStack stack) {
    return ItemStack.isSameItemSameComponents(stack, makeNormalStack(stack.getCount()));
  }

  @Override
  public String toString() {
    return this.getFriendlyName();
  }

  @Override
  public boolean equals(Object o) {
    if ((o instanceof ItemIdentifierStack)) {
      throw new IllegalStateException("Comparison between ItemIdentifierStack and ItemIdentifier -- did you forget a .getItem() in your code?");
    }
    if (o instanceof ItemIdentifier other) {
      return other.item.equals(item);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return Objects.hash(item);
  }
}
