/**
   @MLS
*/
package upmc.akka.leader

import akka.actor.{Props,  Actor,  ActorRef,  ActorSystem, ActorSelection}

case class CheckVivant ()
case class CheckMort ()

class CheckerActor(node : ActorRef, val id : Int) extends Actor {
    var vivants = new Array[Boolean](4)
    var hearts = new Array[ActorSelection](4)
    var cptNoSignal = new Array[Int](4)
    
    for(i <- 0 to 3) {
            try {
                hearts(i) = context.actorSelection("akka.tcp://MozartSystem" + i + "@127.0.0.1:600" + i + "/user/Node" + i + "/Heart" + i)
            } catch {
                case _ : Throwable => println("Erreur heart " + i)
            }

        if (i == id) vivants(i) = true
        else vivants(i) = false
        
        cptNoSignal(i) = 0
    }

    def receive = {
        case CheckMort => {
            for(i <- 0 to 3) {
                if (i != id){
                    cptNoSignal(i) = cptNoSignal(i) + 1
                    if(cptNoSignal(i) == 5) {
                        vivants(i) = false
                        node ! Mort(i)
                    }
                }
            }
        }

        case CheckVivant => {
            for(i <- 0 to 3)
                if (i != id) hearts(i) ! Check
        }

        case Vivant (n) => {
            cptNoSignal(n) = 0
            vivants(n) = true
            node ! Vivant(n)
        }
    }
}