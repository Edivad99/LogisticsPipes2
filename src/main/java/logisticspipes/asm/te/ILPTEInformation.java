package logisticspipes.asm.te;

import org.jetbrains.annotations.Nullable;

public interface ILPTEInformation {

  @Nullable
  LPTileEntityObject getLPTileEntityObject();

  void setLPTileEntityObject(LPTileEntityObject object);
}
