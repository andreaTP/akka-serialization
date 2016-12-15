package akka.remote

import akka.serialization._
import upickle.default._

@scala.scalajs.js.annotation.JSExport
class StaticUPickleSerializer(val system: akka.actor.ExtendedActorSystem) extends Serializer {

  def includeManifest: Boolean = true

  def identifier = 567890

  val companions =//Map[Class[_], (((Any) => Array[Byte]),(Array[Byte] => Any))]()
    Picklables.all

  //Special serializations
  import upickle.Js
  implicit val actorRefWriter = Writer[akka.actor.ActorRef]{
    case ar =>
      Js.Str(Serialization.serializedActorPath(ar))
  }
  implicit val actorRefReader = Reader[akka.actor.ActorRef]{
    case Js.Str(str) =>
      system.provider.resolveActorRef(str)
  }

  implicit val terminatedWriter = Writer[akka.actor.Terminated]{
    case ar =>
      Js.Str(
        Serialization.serializedActorPath(ar.actor)+" "+
        ar.existenceConfirmed+" "+
        ar.addressTerminated
      )
  }
  implicit val terminatedReader = Reader[akka.actor.Terminated]{
    case Js.Str(str) =>
      val vals = str.split(" ")
      new akka.actor.Terminated(
        system.provider.resolveActorRef(vals(0)))(
        java.lang.Boolean.valueOf(vals(1)),
        java.lang.Boolean.valueOf(vals(2))
      )
  }

  implicit val gotUidWriter = Writer[akka.remote.ReliableDeliverySupervisor.GotUid]{
    case gu =>
      Js.Str(
        gu.uid+" "+write(gu.remoteAddres)
      )
  }
  implicit val gotUidReader = Reader[akka.remote.ReliableDeliverySupervisor.GotUid]{
    case Js.Str(str) =>
      val vals = str.split(" ")
      val uid = vals(0).toInt
      val addr = str.replaceFirst(vals(0)+" ", "")

      new akka.remote.ReliableDeliverySupervisor.GotUid(
        uid, read[akka.actor.Address](addr)
      )
  }

  def toBinary(obj: AnyRef): Array[Byte] = {
    try {
      //println("---------------> GOING HERE")
      //throw new Exception("stop here now ")
      companions(obj.getClass)._1(obj)
    } catch {
      case err: Throwable =>
            println("*********************************")
            println("*********************************")
            println("          ERROR                  ")
            println("*********************************")
            println("*********************************")
            println()
            println("cannot serialize "+obj)
            System.exit(0)
            throw err
    }

  }

  // "fromBinary" deserializes the given array,
  // using the type hint (if any, see "includeManifest" above)
  def fromBinary(
    bytes: Array[Byte],
    clazz: Option[Class[_]]): AnyRef = {
    clazz match {
      case Some(clz) =>
        try {
          companions(clz)._2(bytes)
            .asInstanceOf[AnyRef]
        } catch {
          case err : Throwable =>
            println("*********************************")
            println("*********************************")
            println("          ERROR                  ")
            println("*********************************")
            println("*********************************")
            println()
            println("cannot deserialize "+clz)
            System.exit(0)
            throw err
        }
      case _ =>
        throw new Exception("no class in manifest")
    }
  }
}
