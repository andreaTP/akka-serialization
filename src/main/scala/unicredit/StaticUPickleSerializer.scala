package unicredit

import akka.serialization._
import eu.unicredit.pickling.Picklables
import upickle.default._

class StaticUPickleSerializer extends Serializer {

  // This is whether "fromBinary" requires a "clazz" or not
  def includeManifest: Boolean = true

  def identifier = 567890

  val companions = Picklables.all

  // "toBinary" serializes the given object to an Array of Bytes
  def toBinary(obj: AnyRef): Array[Byte] = {
    companions(obj.getClass)._1(obj)
  }

  // "fromBinary" deserializes the given array,
  // using the type hint (if any, see "includeManifest" above)
  def fromBinary(
    bytes: Array[Byte],
    clazz: Option[Class[_]]): AnyRef = {
    clazz match {
      case Some(clz) =>
        companions(clz)._2(bytes)
          .asInstanceOf[AnyRef]
      case _ =>
        throw new Exception("no class in manifest")
    }
  }
}
