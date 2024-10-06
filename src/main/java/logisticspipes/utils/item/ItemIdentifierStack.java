package logisticspipes.utils.item;

import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

@Getter
public class ItemIdentifierStack implements Comparable<ItemIdentifierStack> {

  public static final StreamCodec<RegistryFriendlyByteBuf, ItemIdentifierStack> STREAM_CODEC =
      StreamCodec.composite(
          ItemStack.OPTIONAL_STREAM_CODEC, ItemIdentifierStack::makeNormalStack,
          ItemIdentifierStack::getFromStack);

  private final ItemIdentifier item;
  @Setter
  private int stackSize;

  public ItemIdentifierStack(ItemIdentifier item, int stackSize) {
    this.item = item;
    this.stackSize = stackSize;
  }

  public ItemIdentifierStack(ItemIdentifierStack item) {
    this(item.item, item.stackSize);
  }

  public static ItemIdentifierStack getFromStack(ItemStack stack) {
    return new ItemIdentifierStack(ItemIdentifier.get(stack), stack.getCount());
  }

  public static ItemIdentifierStack parse(HolderLookup.Provider lookupProvider, CompoundTag tag) {
    return ItemIdentifierStack.getFromStack(ItemStack.parseOptional(lookupProvider, tag));
  }

  public Tag save(HolderLookup.Provider lookupProvider) {
    return this.makeNormalStack().saveOptional(lookupProvider);
  }

  public boolean isEmpty() {
    return this.getStackSize() <= 0;
  }

  public void lowerStackSize(int count) {
    this.stackSize -= count;
  }

  public ItemStack makeNormalStack() {
    return this.item.makeNormalStack(stackSize);
  }

  @Override
  public int compareTo(ItemIdentifierStack o) {
    int c = this.item.compareTo(o.item);
    if (c == 0) {
      return this.getStackSize() - o.getStackSize();
    }
    return c;
  }

  public ItemEntity makeItemEntity(Level level, double xCoord, double yCoord, double zCoord) {
    return this.item.makeItemEntity(this.stackSize, level, xCoord, yCoord, zCoord);
  }

  public String getFriendlyName() {
    return getStackSize() + " " + this.item.getFriendlyName(makeNormalStack());
  }

  @Override
  public String toString() {
    return String.format("%dx %s", getStackSize(), this.item);
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof ItemIdentifierStack stack) {
      return stack.item.equals(item) && stack.getStackSize() == getStackSize();
    }
    if ((o instanceof ItemIdentifier)) {
      throw new IllegalStateException("Comparison between ItemIdentifierStack and ItemIdentifier -- did you forget a .getItem() in your code?");
    }
    return false;
  }

  @Override
  public int hashCode() {
    return Objects.hash(item, stackSize);
  }
}
