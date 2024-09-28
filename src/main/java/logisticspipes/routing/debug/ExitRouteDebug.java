package logisticspipes.routing.debug;

import java.util.List;
import org.jetbrains.annotations.Nullable;
import logisticspipes.utils.DoubleCoordinates;

public class ExitRouteDebug {

  @Nullable
  public List<DoubleCoordinates> filterPosition = null;
  @Nullable
  public String toStringNetwork = null;
  public boolean isNewlyAddedCandidate = true;
  public boolean isTraced = true;
  public int index = -1;
}
