package logisticspipes.interfaces;

public interface ICoordinates {
  double getXDouble();

  double getYDouble();

  double getZDouble();

  default int getXInt() {
    return (int) getXDouble();
  }

  default int getYInt() {
    return (int) getYDouble();
  }

  default int getZInt() {
    return (int) getZDouble();
  }
}
