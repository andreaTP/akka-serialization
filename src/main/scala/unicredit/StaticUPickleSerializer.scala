package akka.remote

import akka.serialization._
import upickle.default._

class StaticUPickleSerializer extends Serializer {

  // This is whether "fromBinary" requires a "clazz" or not
  def includeManifest: Boolean = true

  def identifier = 567890

  val companions = Picklables.all

  val magicNumber: Byte = 5

  // "toBinary" serializes the given object to an Array of Bytes
  def toBinary(obj: AnyRef): Array[Byte] = {
    try {
      companions(obj.getClass)._1(obj)
    } catch {
      case err: Throwable =>
        println("*************************")
        println("cannot pickle "+obj.getClass)
        println("*************************")
        throw err
/*
        try {
          obj.getClass match {
            case classOf[akka.remote.EndpointWriter.AckIdleCheckTimer] =>
              Array(magicNumber, 1.toByte)
            case classOf[ akka.remote.ReliableDeliverySupervisor.GotUid] =>
              Array(magicNumber, 3.toByte)
              //println("next is the picklable counterpart...")
              //final case class GotUid(uid: Int, remoteAddres: Address)
            case classOf[akka.remote.RemoteWatcher.HeartbitTick] =>
              Array(magicNumber, 3.toByte)
            case classOf[akka.remote.RemoteWatcher.ReapUnreachableTick] =>
              Array(magicNumber, 4.toByte)
            case _ =>
              throw new Exception("cannot serialize")
          }
          if (obj.getClass == classOf[akka.remote.EndpointWriter.AckIdleCheckTimer])
            Array(magicNumber, 1.toByte)

        } catch {
            case ex: Throwable =>
              println("missing")
              throw err
        }
*/


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
            throw err
          /*
            if (bytes(0) == magicNumber) {
              bytes(1) match {
                case 1 => akka.remote.EndpointWriter.AckIdleCheckTimer
                case _ => throw new Exception("unknown encoding")
              }
            } else {
              throw new Exception("unknown message")
            }
          */
        }
      case _ =>
        throw new Exception("no class in manifest")
    }
  }
}
