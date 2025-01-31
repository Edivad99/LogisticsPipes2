package logisticspipes.logisticspipes;

/**
 * This interface tracks statistics
 *
 * @author Krapht
 */
public interface ITrackStatistics {

  void receivedItem(int count);

  void relayedItem(int count);
}
