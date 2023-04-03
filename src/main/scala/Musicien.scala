/**
   @MLS
*/
package upmc.akka.leader

import akka.actor.{Props,  Actor,  ActorRef,  ActorSystem, ActorSelection}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext
import ExecutionContext.Implicits.global

case class Start()
case class PlayConductor()
case class Maestro(id : Int)
case class Vivant(id : Int)
case class Mort(n : Int)
case class Suicide()
case class Election()
case class Elu()

class Musicien (val id : Int) extends Actor {
    import DataBaseActor._

    val provider = context.actorOf(Props(new ProviderActor(self)), "providerActor")
    val player = context.actorOf(Props[PlayerActor], name = "playerActor")
    val display = context.actorOf(Props[DisplayActor], "displayActor")

    val rand = new scala.util.Random
    var musiciens = new Array[ActorSelection](4)
    var vivants = new Array[Boolean](4)

    var nbVivants = 0
    var idMaestro = -1

    var solo = true;
    

    vivants(id) = true
    
    for(i <- 0 to 3) {
        if (i != id){
            musiciens(i) = context.actorSelection("akka.tcp://MozartSystem" + i + "@127.0.0.1:600" + i + "/user/Node" + i + "/Musicien" + i)
            vivants(i) = false
        }
    }

    def receive = {

        case Start => display ! Message ("Le node " + this.id + " est dans la zone.")

        case PlayConductor => if(idMaestro == id) provider ! GetMeasure (rand.nextInt(6) + rand.nextInt(6))

        case Maestro(n) => if (n != id && vivants(n)) idMaestro = n

        case Vivant(n) => {
            solo = false;

            if(!vivants(n)) {
                if(nbVivants == 0 && idMaestro == id) self ! PlayConductor
                
                vivants(n) = true
                nbVivants += 1
                if (nbVivants == 1)
                    display ! Message ("On a un node en vie ! MA CHEEEEEEEE !")
                else
                    display ! Message ("On a " + nbVivants + " nodes en vie ! MA CHEEEEEEEE !")
            }

            if(idMaestro != -1) musiciens(n) ! Maestro (idMaestro)
        }

        case Mort (n) => {
            display ! Message ("Le node " + n + " est mort RIP x_x :sad:")
            if(vivants(n)) {
                vivants(n) = false
                nbVivants -= 1

                if (nbVivants == 1)
                    display ! Message ("On a un node en vie ! MA CHEEEEEEEE !")
                else
                    display ! Message ("On a " + nbVivants + " nodes en vie ! MA CHEEEEEEEE !")    
            }
            
            if(idMaestro == n || idMaestro == -1) self ! Election   
        }

        case Suicide =>
            if(solo) {
                display ! Message ("J'en ai marre d'être seul... (même si vaut mieux être seul que mal accompagné hein) ")
                context.system.terminate()
                System.exit(0)
            }

        case Election => {
            var elu = false
            for(i <- 0 to 3)
                if(!elu && vivants(i)) {
                    elu = true;

                    if(i == id) 
                        self ! Elu
                    else 
                        idMaestro = i
                }
        }

        case Elu =>
            if(idMaestro != id) {
                idMaestro = id
                display ! Message ("Le Maestro est dans la place ! SKURT SKUUUURT !!!")
                self ! PlayConductor
            }

        case Measure (chordlist) => {
            if(idMaestro == id) {
                if(nbVivants > 0) {
                    var musicien = -1
                    var randMusicien = rand.nextInt(nbVivants) + 1
                    for(i <- 0 to 3){    
                        if(i != id && vivants(i)){
                            randMusicien -= 1
                            if(randMusicien == 0) musicien = i
                        }
                    }
                    display ! Message ("Fais nous rêver numéro " + musicien + " ! (Nous ne sommes tous qu'un simple numéro au final)")
                    musiciens(musicien) ! Measure (chordlist)
                    context.system.scheduler.scheduleOnce(1800 milliseconds, self, PlayConductor)
                } else {
                    solo = true;
                    display ! Message ("C'est quand qu'ils arrivent les autres ? ils me manquent :(")
                    context.system.scheduler.scheduleOnce(20000 milliseconds, self, Suicide)
                }
            } else {
                display ! Message ("Admirez ma note... DOOOOOO RÉÉÉÉ MIIIIIIIIIIII")
                player ! Measure (chordlist)
            }
        }
    }
}