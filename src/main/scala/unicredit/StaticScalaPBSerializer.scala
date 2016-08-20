package unicredit

import akka.serialization._
import com.trueaccord.scalapb._
import eu.unicredit.scalapb.GeneratedMessageCompanion

class StaticScalaPBSerializer extends Serializer {

  // This is whether "fromBinary" requires a "clazz" or not
  def includeManifest: Boolean = true

  def identifier = 567890

  // "toBinary" serializes the given object to an Array of Bytes
  def toBinary(obj: AnyRef): Array[Byte] = {
    obj match {
      case gm: GeneratedMessage =>
        gm.toByteArray
      case _ =>
        throw new Exception("Don't want to serialize differrent kind of messages here")
    }
  }

  val companions = GeneratedMessageCompanion.all

  // "fromBinary" deserializes the given array,
  // using the type hint (if any, see "includeManifest" above)
  def fromBinary(
    bytes: Array[Byte],
    clazz: Option[Class[_]]): AnyRef = {
    clazz match {
      case Some(clz) =>
        companions(clz)
          .parseFrom(bytes)
          .asInstanceOf[AnyRef]
      case _ =>
        throw new Exception("no class in manifest")
    }
  }
}
