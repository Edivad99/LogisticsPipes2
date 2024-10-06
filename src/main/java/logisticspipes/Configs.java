package logisticspipes;

import net.minecraft.SharedConstants;

public class Configs {

  // Configurable
  public static int LOGISTICS_DETECTION_LENGTH = 50;
  public static int LOGISTICS_DETECTION_COUNT = 100;
  public static int LOGISTICS_DETECTION_FREQUENCY = SharedConstants.TICKS_PER_SECOND * 30;
  public static boolean LOGISTICS_ORDERER_COUNT_INVERTWHEEL = false;
  public static boolean LOGISTICS_ORDERER_PAGE_INVERTWHEEL = false;
  public static final float LOGISTICS_ROUTED_SPEED_MULTIPLIER = 20F;
  public static final float LOGISTICS_DEFAULTROUTED_SPEED_MULTIPLIER = 10F;
  public static int MAX_UNROUTED_CONNECTIONS = 32;

  public static int LOGISTICS_HUD_RENDER_DISTANCE = 15;

  public static float	pipeDurability = 0.25F; //TODO

  public static boolean LOGISTICS_POWER_USAGE_DISABLED = true;
  public static double POWER_USAGE_MULTIPLIER = 1;
  public static double COMPILER_SPEED = 1.0;
  public static boolean ENABLE_RESEARCH_SYSTEM = false;

  public static int LOGISTICS_CRAFTING_TABLE_POWER_USAGE = 250;

  public static boolean TOOLTIP_INFO = LogisticsPipes.isDebug();
  public static boolean ENABLE_PARTICLE_FX = true;

  public static int[] CHASSIS_SLOTS_ARRAY = {1,2,3,4,8};

  // GuiOrderer Popup setting
  public static boolean DISPLAY_POPUP = true;

  // MultiThread
  public static int MULTI_THREAD_NUMBER = 4;
  public static int MULTI_THREAD_PRIORITY = Thread.NORM_PRIORITY;

  // Performance
  public static boolean DISABLE_ASYNC_WORK = false;
  public static int MINIMUM_INVENTORY_SLOT_ACCESS_PER_TICK = 10;
  public static int MAXIMUM_INVENTORY_SLOT_ACCESS_PER_TICK = 0;
  public static int MINIMUM_JOB_TICK_LENGTH = 1;

  public static boolean CHECK_FOR_UPDATES = true;

  public static boolean EASTER_EGGS = true;

  public static boolean OPAQUE = false;

  public static int MAX_ROBOT_DISTANCE = 64;
}
