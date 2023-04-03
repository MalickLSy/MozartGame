/**
   @MLS
*/
package upmc.akka.leader

import akka.actor._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext
import ExecutionContext.Implicits.global

class NodeActor (val id : Int) extends Actor {
    val musicien = context.actorOf(Props(new Musicien(id)), "Musicien"+id)
    val checker = context.actorOf(Props(new CheckerActor(self, id)), "Checker"+id)
    val heart = context.actorOf(Props(new HeartActor(id)), "Heart"+id)

    context.system.scheduler.schedule(0 milliseconds, 200 milliseconds, checker, CheckVivant)
    context.system.scheduler.schedule(0 milliseconds, 200 milliseconds, checker, CheckMort)

    def receive = { 
        case Start => musicien ! Start()
        case Vivant(n) => musicien ! Vivant(n)
        case Mort(n) => musicien ! Mort(n)
    }
}