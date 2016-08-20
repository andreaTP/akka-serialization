package unicredit

import akka.serialization._
import com.trueaccord.scalapb._
import scala.collection.mutable.HashMap

class ScalaPBSerializer extends Serializer {

  // This is whether "fromBinary" requires a "clazz" or not
  def includeManifest: Boolean = true

  def identifier = 456789

  // "toBinary" serializes the given object to an Array of Bytes
  def toBinary(obj: AnyRef): Array[Byte] = {
    obj match {
      case gm: GeneratedMessage =>
        gm.toByteArray
      case _ =>
        throw new Exception("Don't want to serialize differrent kind of messages here")
    }
  }

  val cache =
    new HashMap[Class[_], GeneratedMessageCompanion[_]]

  // "fromBinary" deserializes the given array,
  // using the type hint (if any, see "includeManifest" above)
  def fromBinary(
    bytes: Array[Byte],
    clazz: Option[Class[_]]): AnyRef = {
    clazz match {
      case Some(clz) =>
        val gmc =
          cache.get(clz) match {
            case Some(gmc) => gmc
            case _ =>
              val gmc =
                //Any better way to do this?
                Class.forName(clz.getCanonicalName+"$")
                  .getField("MODULE$").get(clz)
                  .asInstanceOf[GeneratedMessageCompanion[_]]
                  .defaultInstance.asInstanceOf[GeneratedMessage]
                  .companion

              cache.update(clz, gmc)
              gmc
          }
        gmc.asInstanceOf[GeneratedMessageCompanion[_ <: GeneratedMessage]].parseFrom(bytes)
      case _ =>
        throw new Exception("no class in manifest")
    }
  }
}
