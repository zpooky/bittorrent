package spooky.scheduler

import scala.concurrent.duration.FiniteDuration
import java.util.concurrent.ScheduledExecutorService

class Scheduler(private val executors: ScheduledExecutorService) {
  def schedule(initialDelay: FiniteDuration, interval: FiniteDuration, runnable: Runnable): Context = {
    ???
  }
}
