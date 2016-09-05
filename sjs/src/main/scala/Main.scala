package akka.remote

import eu.unicredit._

import scala.scalajs.js

import akka.actor._
import com.typesafe.config.ConfigFactory

import scala.concurrent.duration._

//case class MyString(str: String) extends Serializable {
//  override def toString() = "My string -> "+str
//}

object Main extends js.JSApp {

  def main() = {
    println("JS env")

    /*

    Step to keep everything working...

    1 -> check serialization
    2 -> check channel communication in JS --> postponed first implement a dummy channel for local remoting
    3 -> start write a proper tranport for akka-remote
    4 -> put everything togheter

    */

    //serialization()

    //channelCommunication()

    transport()
  }

  def serialization() = {
    //this is due to the shocon bug...
    akka.actor.JSDynamicAccess.injectClass{
      "StringLiteral(akka.remote.StaticUPickleSerializer)" -> classOf[akka.remote.StaticUPickleSerializer]
    }
    //This will be merged in akka.js
    akka.actor.JSDynamicAccess.injectClass{
      "java.io.Serializable" -> classOf[java.io.Serializable]
    }

    import eu.unicredit.Messages.Test

    def ppActor(matcher: Test, answer: Test) = Props(
        new Actor {
          def receive = {
            case matcher =>
              sender ! answer
              println(s"received $matcher sending answer $answer")
          }
        }
      )

    val system = ActorSystem("serializationTest", ConfigFactory.parseString(Config.serializationConfig))
    import system.dispatcher

    val ponger = system.actorOf(ppActor(Test("ping",1), Test("pong",2)))
    val pinger = system.actorOf(ppActor(Test("pong",3), Test("ping",4)))


    system.scheduler.scheduleOnce(1 second)(
      pinger.!(Test("pong",0))(ponger)
    )

    system.scheduler.scheduleOnce(2 seconds){
      pinger ! PoisonPill
      ponger ! PoisonPill
      system.terminate()
    }

  }

  def transport() = {
    println("have to write a dummy transport first")
    //this is due to the shocon bug...
    akka.actor.JSDynamicAccess.injectClass{
      "StringLiteral(akka.remote.StaticUPickleSerializer)" -> classOf[akka.remote.StaticUPickleSerializer]
    }
    //This will be merged in akka.js
    akka.actor.JSDynamicAccess.injectClass{
      "java.io.Serializable" -> classOf[java.io.Serializable]
    }
    akka.actor.JSDynamicAccess.injectClass{
      "eu.unicredit.DummyTransport" -> classOf[eu.unicredit.DummyTransport]
    }

    akka.actor.JSDynamicAccess.injectClass{
      "akka.remote.RemoteActorRefProvider" -> classOf[akka.remote.RemoteActorRefProvider]
    }

    val system1 = ActorSystem("transportTest1", ConfigFactory.parseString(Config.transportConfig))
    val system2 = ActorSystem("transportTest2", ConfigFactory.parseString(Config.transportConfig))
    import system1.dispatcher

    system1.scheduler.scheduleOnce(0 millis){
      system2.scheduler.scheduleOnce(0 millis){

        println("Systems started..")
        /*system2.actorOf(Props(new Actor {
          def receive = {
            case any =>
              println("pippo received any...")
          }
        }), "pippo")

        system1.actorSelection("akka.ws://transportTest2/pippo") ! "prova"
        */
      }
    }

    system1.scheduler.scheduleOnce(3 seconds){
      system1.terminate()
      system2.terminate()
    }

  }

}
