package unicredit

import akka.serialization._
import com.trueaccord.scalapb._
import eu.unicredit.pickling.Picklables

class StaticBooPickleSerializer extends Serializer {

  // This is whether "fromBinary" requires a "clazz" or not
  def includeManifest: Boolean = true

  def identifier = 567890

  val companions = Picklables.all

  // "toBinary" serializes the given object to an Array of Bytes
  def toBinary(obj: AnyRef): Array[Byte] = {
    companions(obj.getClass)._1(obj).getBytes
  }

  // "fromBinary" deserializes the given array,
  // using the type hint (if any, see "includeManifest" above)
  def fromBinary(
    bytes: Array[Byte],
    clazz: Option[Class[_]]): AnyRef = {
    clazz match {
      case Some(clz) =>
        companions(clz)
          ._2(new String(bytes))
          .asInstanceOf[AnyRef]
      case _ =>
        throw new Exception("no class in manifest")
    }
  }
}
