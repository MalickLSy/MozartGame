/**
   @MLS
*/
package upmc.akka.leader

import math._
import javax.sound.midi._
import javax.sound.midi.ShortMessage._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext
import ExecutionContext.Implicits.global
import akka.actor.{Props, Actor, ActorRef, ActorSystem}

object PlayerActor {
  case class MidiNote (pitch:Int, vel:Int, dur:Int, at:Int) 
  val info = MidiSystem.getMidiDeviceInfo().filter(_.getName == "Gervill").headOption
  
  // or "SimpleSynth virtual input" or "Gervill"
  val device = info.map(MidiSystem.getMidiDevice).getOrElse {
    println("[ERROR] Could not find Gervill synthesizer.")
    sys.exit(1)
  }

  val rcvr = device.getReceiver()

  /////////////////////////////////////////////////
  def note_on (pitch:Int, vel:Int, chan:Int): Unit = {
      val msg = new ShortMessage
      msg.setMessage(NOTE_ON, chan, pitch, vel)
      rcvr.send(msg, -1)
  }

  def note_off (pitch:Int, chan:Int): Unit = {
      val msg = new ShortMessage
      msg.setMessage(NOTE_ON, chan, pitch, 0)
      rcvr.send(msg, -1)
  }
}

//////////////////////////////////////////////////

class PlayerActor () extends Actor{
  import DataBaseActor._
  import PlayerActor._
  device.open()
  
  def play (objetMusical:ObjetMusical): Unit = {
    objetMusical match {
      case Measure (l) => l.foreach(c=>play(c))
      case Chord(at, notes) => notes.foreach(note => play_midi(at,note))
    }
  }

  def play_midi(at : Int, objetMusical: ObjetMusical) : Unit = { 
    objetMusical match {
      case Note(p, d, v) => self ! MidiNote(p, v, d, at)
    }
  }

  def receive = {
    case Measure (l) => play(Measure(l))
    case MidiNote(p,v, d, at) => {
      context.system.scheduler.scheduleOnce ((at) milliseconds) (note_on (p,v,10))
      context.system.scheduler.scheduleOnce ((at+d) milliseconds) (note_off (p,10))
    }
  }
}