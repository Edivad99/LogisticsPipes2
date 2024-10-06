package logisticspipes.grow

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import logisticspipes.LogisticsPipes
import java.util.concurrent.CancellationException
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.coroutines.CoroutineContext

object ServerTickDispatcher : CoroutineDispatcher() {
  private val coroutineQueue = ConcurrentLinkedQueue<Runnable>()
  private val toSchedule = mutableListOf<Runnable>()

  fun serverStart() {
    val startupJob = Coroutines.serverScope.launch {
      LogisticsPipes.LOG.info("Hello from the server tick")
    }
    Coroutines.asynchronousScope.async {
      LogisticsPipes.LOG.info("Waiting for server tick")
      startupJob.join()
      LogisticsPipes.LOG.info("Server tick complete! Hello from the async scope")
    }.invokeOnCompletion { throwable ->
      throwable?.stackTraceToString()?.also { stacktrace ->
        LogisticsPipes.LOG.error("Error when greeting server tick scope:\n$stacktrace")
      }
    }
  }

  fun cleanup() =
    cancelChildren(CancellationException("cleanup was called on ServerTickContext"))

  fun tick() {
    val start = System.nanoTime()
    // failsafe to exit after 1 second
    while (coroutineQueue.isNotEmpty() && (System.nanoTime() - start) < 1_000_000_000) {
      coroutineQueue.poll().run()
    }
    if (System.nanoTime() - start >= 1_000_000_000) {
      LogisticsPipes.LOG
        .debug("Logistics Pipes ServerTickContext hang for a second. Dumping coroutines:")
      coroutineQueue.forEach {
        LogisticsPipes.LOG.debug(it.toString())
      }
    }
    synchronized(toSchedule) {
      coroutineQueue.addAll(toSchedule)
      toSchedule.clear()
    }
  }

  override fun dispatch(context: CoroutineContext, block: Runnable) {
    coroutineQueue.add(block)
  }

  fun scheduleNextTick(block: Runnable) {
    synchronized(toSchedule) {
      toSchedule.add(block)
    }
  }
}

