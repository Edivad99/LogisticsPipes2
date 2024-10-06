package logisticspipes.modules

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.time.withTimeout
import logisticspipes.LogisticsPipes
import logisticspipes.grow.Coroutines
import net.minecraft.client.Minecraft
import net.minecraft.world.level.block.entity.BlockEntity
import java.time.Duration

abstract class AsyncModule<S, C> : LogisticsModule() {
  private var currentJob: Deferred<C?>? = null

  /**
   * Represents the wait time in ticks until the next job is started.
   */
  open val everyNthTick: Int = 20

  /**
   * A debug helper for adding the connected [BlockEntity] to error
   * information. May return null, if the information is not
   * available.
   */
  private val connectedEntity: BlockEntity?
    get() = this.service?.availableAdjacent?.inventories()?.firstOrNull()?.blockEntity

  @ExperimentalCoroutinesApi
  override fun tick() {
    val currentJobTemp = currentJob
    if (currentJobTemp?.isActive == true) {
      runSyncWork()
    } else {
      if (currentJobTemp?.isCompleted == true) {
        try {
          runSyncWork()
          completeJob(currentJobTemp)
        } finally {
          currentJob = null
        }
      }

      if (this.service?.isNthTick(everyNthTick) == true) {
        val setup = jobSetup()
        currentJob = Coroutines.asynchronousScope.async {
          try {
            return@async withTimeout(Duration.ofSeconds(90)) {
              tickAsync(setup)
            }
          } catch (e: RuntimeException) {
            val isGamePaused = getLevel()?.isClientSide == false && Minecraft.getInstance().isPaused
            if (e !is TimeoutCancellationException && !isGamePaused) {
              val connected =
                connectedEntity?.let { " connected to $it at ${it.blockPos}" } ?: ""
              LogisticsPipes.LOG.error("Error in ticking async module $module$connected", e)
            }
          }
          return@async null
        }
      }
    }
  }


  /**
   * Setup function which is run on every new module tick.
   *
   * @return setup object S which is passed to [tickAsync].
   */
  abstract fun jobSetup(): S

  /**
   * Completion function that is run after every successful module
   * tick and all asynchronous work is done.
   *
   * @param deferred the [Deferred] of the asynchronous work.
   */
  abstract fun completeJob(deferred: Deferred<C?>)

  /**
   * Asynchronous tick function that is run with a timeout. Takes the
   * setup object and returns an object for [completeJob].
   *
   * @param setupObject the setup object from [jobSetup].
   * @return a completion object for [completeJob].
   */
  abstract suspend fun tickAsync(setupObject: S): C

  /**
   * Runs every tick while the [current async task][currentJob] is active.
   */
  abstract fun runSyncWork()
}
