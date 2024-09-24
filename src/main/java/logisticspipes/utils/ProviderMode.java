package logisticspipes.utils;

import lombok.Getter;

@Getter
public enum ProviderMode {

  DEFAULT("Normal", false, false, 0, 0),
  LEAVE_FIRST("LeaveFirst", false, false, 1, 0),
  LEAVE_LAST("LeaveLast", false, false, 0, 1),
  LEAVE_FIRST_AND_LAST("LeaveFirstAndLast", false, false, 1, 1),
  LEAVE_ONE_PER_STACK("Leave1PerStack", true, false, 0, 0),
  LEAVE_ONE_PER_TYPE("Leave1PerType", false, true, 0, 0);

  private final String translationName;
  private final boolean hideOnePerStack;
  private final boolean hideOnePerType;
  private final int cropStart;
  private final int cropEnd;

  ProviderMode(String translationName, boolean hideOnePerStack, boolean hideOnePerType, int cropStart, int cropEnd) {
    this.translationName = translationName;
    this.hideOnePerStack = hideOnePerStack;
    this.hideOnePerType = hideOnePerType;
    this.cropStart = cropStart;
    this.cropEnd = cropEnd;
  }

  public String getTranslationKey() {
    return "misc.extractionmode." + translationName;
  }
}
