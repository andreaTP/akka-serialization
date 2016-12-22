package eu.unicredit

import akka.actor._
import akka.remote.transport.Transport

import scala.concurrent.{Future, Promise}
import akka.remote.transport.AssociationHandle
import akka.remote.transport.Transport.AssociationEventListener

//import com.typesafe.config.Config

@scala.scalajs.js.annotation.JSExport
class DummyTransport (
  val settings: com.typesafe.config.Config,
  val system: ExtendedActorSystem) extends Transport {

  val trail = "\n\n\n****\n"
  println(trail+"transport started on system "+system)

  def associate(remoteAddress: Address): Future[AssociationHandle] = {

    println(trail+"ask association with "+remoteAddress)

    ???
  }

  def isResponsibleFor(address: Address): Boolean = {
    println(trail+"I'm responsible? "+address)

    false
  }

  def listen: Future[(Address, Promise[AssociationEventListener])] = {
    println(trail+"have to listen?")

    ???
  }

  def maximumPayloadBytes: Int = 10000

  def schemeIdentifier: String = "dummy"

  def shutdown(): Future[Boolean] =
    Future.successful(true)

}
