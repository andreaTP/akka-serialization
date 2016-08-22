package unicredit

import akka.actor._

trait Utils {
  def terminate(implicit system: ActorSystem) = {
    import system.dispatcher
    import scala.concurrent.duration._
    system.scheduler.scheduleOnce(3 seconds){
      system.terminate
    }
  }
}

object SampleMultiJvmNode1 extends Utils {
  def main(args: Array[String]) {

    implicit val system = ActorSystem("system1")

    system.actorOf(Props(new Actor{
      def receive = {
        case any => println(s"RECEIVED $any")
      }
    }), "actor1")

    println("Hello from node 1")

    terminate
  }
}

object SampleMultiJvmNode2 extends Utils {
  def main(args: Array[String]) {

    implicit val system = ActorSystem("system2")

    system.actorOf(Props(new Actor{

      override def preStart() = {
        import context.dispatcher
        import scala.concurrent.duration._

        def getActor1 = system.actorSelection("akka.tcp://system1@127.0.0.1:9991//user/actor1")

        context.system.scheduler.scheduleOnce(1 seconds){
          getActor1 ! "one"
        }
        context.system.scheduler.scheduleOnce(2 seconds){
          getActor1 ! GenMessage.generate
        }
      }

      def receive = {
        case any => println(s"RECEIVED: $any")
      }
    }))

    println("Hello from node 2")

    terminate
  }
}
