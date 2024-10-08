package logisticspipes.particle;

public enum Particles {

  /**
   * General color arrangement:<br>
   * SinkReply: blue<br>
   * Extract: orange<br>
   * Provide/request: violet<br>
   * Use power: gold<br>
   * Render update: green<br>
   * Power status change: red<br>
   * Special cases: white<br>
   */

  BLUE_SPARKLE(0, 0, 1),
  GOLD_SPARKLE(0.93F, 0.80F, 0.36F),
  GREEN_SPARKLE(0, 1, 0),
  LIGHT_GREEN_SPARKLE(0.81F, 0.94F, 0.31F),
  LIGHT_RED_SPARKLE(0.94F, 0.32F, 0.31F),
  ORANGE_SPARKLE(0.97F, 0.46F, 0.19F),
  RED_SPARKLE(1, 0, 0),
  VIOLET_SPARKLE(0.51F, 0.04F, 0.73F),
  WHITE_SPARKLE(1, 1, 1);

  private final float red, green, blue;

  Particles(float red, float green, float blue) {
    this.red = red;
    this.green = green;
    this.blue = blue;
  }

  public SparkleParticleOptions getSparkleFXParticleOptions(int amount) {
    //LogisticsPipes.LOG.info(this.name());
    return new SparkleParticleOptions(red, green, blue, amount);
  }
}
