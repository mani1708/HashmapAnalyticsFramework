package com.hashmap.haf.scheduler.actors

import akka.actor.{Actor, Props}
import com.hashmap.haf.scheduler.impl.QuartzScheduler

object WorkflowEventListenerActor {
  def props =
    Props[WorkflowEventListenerActor]

  final case class AddJob(workflowId: Long, cronExpression: String)
  final case class StopJob(workflowId: Long)
  final case class DropJob(workflowId: Long)
  final object RemoveAllJobs

}

class WorkflowEventListenerActor extends Actor {
  import SchedulerActor._
  import WorkflowEventListenerActor._
  implicit val system = context.system
  val schedulerActor = system.actorOf(SchedulerActor.props(new QuartzScheduler(system)))

  override def receive = {
    case AddJob(id, expr) => schedulerActor ! StartJob(id.toString, expr)
    case StopJob(id) => schedulerActor ! SuspendJob(id.toString)
    case DropJob(id) => schedulerActor ! RemoveJob(id.toString)
  }
}
