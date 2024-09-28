/*
package logisticspipes.utils.item;


//REPLACE WITH ITEMSTACK
public final class ItemIdentifierStack implements Comparable<ItemIdentifierStack> {

  private final ItemIdentifier _item;
  @Getter
  @Setter
  private int stackSize;

  public static ItemIdentifierStack getFromStack(@Nonnull ItemStack stack) {
    return new ItemIdentifierStack(ItemIdentifier.get(stack), stack.getCount());
  }

  public ItemIdentifierStack(ItemIdentifier item, int stackSize) {
    _item = item;
    setStackSize(stackSize);
  }

  public ItemIdentifierStack(ItemIdentifierStack copy) {
    this(copy._item, copy.stackSize);
  }

  public ItemIdentifier getItem() {
    return _item;
  }

  public void lowerStackSize(int stackSize) {
    this.stackSize -= stackSize;
  }

  @Nonnull
  public ItemStack unsafeMakeNormalStack() {
    return _item.unsafeMakeNormalStack(stackSize);
  }

  @Nonnull
  public ItemStack makeNormalStack() {
    return _item.makeNormalStack(stackSize);
  }

  @Nonnull
  public ItemEntity makeEntityItem(Level world, double x, double y, double z) {
    return _item.makeEntityItem(stackSize, world, x, y, z);
  }

  @Override
  public boolean equals(Object that) {
    if (that instanceof ItemIdentifierStack stack) {
      return stack._item.equals(_item) && stack.getStackSize() == getStackSize();
    }
    if ((that instanceof ItemIdentifier)) {
      throw new IllegalStateException("Comparison between ItemIdentifierStack and ItemIdentifier -- did you forget a .getItem() in your code?");
    }

    return false;
  }

  @Override
  public int hashCode() {
    return _item.hashCode() ^ (1023 * getStackSize());
  }

  @Override
  public String toString() {
    return String.format("%dx %s", getStackSize(), _item.toString());
  }

  public String getFriendlyName() {
    return getStackSize() + " " + _item.getFriendlyName();
  }

  public static LinkedList<ItemIdentifierStack> getListFromInventory(Container inv) {
    return ItemIdentifierStack.getListFromInventory(inv, false);
  }

  public static LinkedList<ItemIdentifierStack> getListFromInventory(Container inv, boolean removeNull) {
    LinkedList<ItemIdentifierStack> list = new LinkedList<>();
    for (int i = 0; i < inv.getContainerSize(); i++) {
      if (inv.getItem(i).isEmpty()) {
        if (!removeNull) {
          list.add(null);
        }
      } else {
        list.add(ItemIdentifierStack.getFromStack(inv.getItem(i)));
      }
    }
    return list;
  }

  public static LinkedList<ItemIdentifierStack> getListSendQueue(LinkedList<Triplet<IRoutedItem, Direction, ItemSendMode>> _sendQueue) {
    LinkedList<ItemIdentifierStack> list = new LinkedList<>();
    for (Triplet<IRoutedItem, Direction, ItemSendMode> part : _sendQueue) {
      if (part == null) {
        list.add(null);
      } else {
        boolean added = false;
        for (ItemIdentifierStack stack : list) {
          if (stack.getItem().equals(part.getA().getItemIdentifierStack().getItem())) {
            stack.setStackSize(stack.getStackSize() + part.getA().getItemIdentifierStack().getCount());
            added = true;
            break;
          }
        }
        if (!added) {
          list.add(new ItemIdentifierStack(part.getA().getItemIdentifierStack()));
        }
      }
    }
    return list;
  }

  @Override
  public int compareTo(ItemIdentifierStack o) {
    int c = _item.compareTo(o._item);
    if (c == 0) {
      return getStackSize() - o.getStackSize();
    }
    return c;
  }
}
*/
