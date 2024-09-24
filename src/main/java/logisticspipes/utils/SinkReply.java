package logisticspipes.utils;

import logisticspipes.interfaces.routing.IAdditionalTargetInformation;

public class SinkReply {

  public enum FixedPriority {
    DEFAULT_ROUTE,
    MOD_BASED_ITEM_SINK,
    ORE_DICT_ITEM_SINK,
    ENCHANTMENT_ITEM_SINK,
    TERMINUS,
    ITEM_SINK,
    PASSIVE_SUPPLIER,
  }

  public enum BufferMode {
    NONE,
    BUFFERED,
    DESTINATION_BUFFERED,
  }

  public final FixedPriority fixedPriority;
  public final int customPriority;
  public final boolean isPassive;
  public final boolean isDefault;
  public final int energyUse;
  public final int maxNumberOfItems;
  public final BufferMode bufferMode;
  public final IAdditionalTargetInformation addInfo;

  public SinkReply(FixedPriority fixedPriority, int customPriority, boolean isPassive, boolean isDefault, int energyUse, int maxNumberOfItems, IAdditionalTargetInformation addInfo) {
    this.fixedPriority = fixedPriority;
    this.customPriority = customPriority;
    this.isPassive = isPassive;
    this.isDefault = isDefault;
    this.energyUse = energyUse;
    this.maxNumberOfItems = maxNumberOfItems;
    bufferMode = BufferMode.NONE;
    this.addInfo = addInfo;
  }

  public SinkReply(SinkReply base, int maxNumberOfItems) {
    this.fixedPriority = base.fixedPriority;
    this.customPriority = base.customPriority;
    this.isPassive = base.isPassive;
    this.isDefault = base.isDefault;
    this.energyUse = base.energyUse;
    this.maxNumberOfItems = maxNumberOfItems;
    this.bufferMode = BufferMode.NONE;
    this.addInfo = base.addInfo;
  }

  public SinkReply(SinkReply base, int maxNumberOfItems, BufferMode bufferMode) {
    this.fixedPriority = base.fixedPriority;
    this.customPriority = base.customPriority;
    this.isPassive = base.isPassive;
    this.isDefault = base.isDefault;
    this.energyUse = base.energyUse;
    this.maxNumberOfItems = maxNumberOfItems;
    this.bufferMode = bufferMode;
    this.addInfo = base.addInfo;
  }
}
