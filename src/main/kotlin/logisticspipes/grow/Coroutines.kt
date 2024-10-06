package logisticspipes.grow

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.neoforged.neoforge.server.ServerLifecycleHooks

object Coroutines {
  val io = Dispatchers.IO
  val ioScope
    get() = CoroutineScope(io)
  val default = Dispatchers.Default
  val asynchronousScope
    get() = CoroutineScope(default)
  val server = ServerTickDispatcher
  val serverScope
    get() = CoroutineScope(server)

  fun scheduleServerTask(inTicks: Int, task: Runnable) {
    val runTick = ServerLifecycleHooks.getCurrentServer()?.tickCount!! + inTicks

    fun waitForTick() {
      if (ServerLifecycleHooks.getCurrentServer()?.tickCount!! >= runTick) {
        task.run()
      } else {
        ServerTickDispatcher.scheduleNextTick(::waitForTick)
      }
    }

    serverScope.launch {
      waitForTick()
    }
  }
}
