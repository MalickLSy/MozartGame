/**
   @MLS
*/
package upmc.akka.leader

import akka.actor.Actor

case class Check ()

class HeartActor (val n : Int) extends Actor {
    def receive = {
        case Check => sender ! Vivant(n)
    }
}