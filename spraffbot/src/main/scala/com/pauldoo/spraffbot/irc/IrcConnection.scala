package com.pauldoo.spraffbot.irc

import java.net.InetSocketAddress
import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.Deploy
import akka.actor.Props
import akka.actor.actorRef2Scala
import akka.io.IO
import akka.io.Tcp
import akka.actor.ActorRef
import com.pauldoo.spraffbot.SpraffBot
import scala.concurrent.Future
import javax.net.ssl.SSLEngine
import javax.net.ssl.SSLContext
import akka.camel.{ CamelMessage, Consumer }
import akka.camel.Oneway
import akka.camel.Producer

object IrcConnection {
  def props(app: ActorRef): Props =
    Props(classOf[IrcConnection], SpraffBot.ircServer, app)
}

object IrcConsumer {
  def props(endpointUri: String, app: ActorRef): Props =
    Props(classOf[IrcConsumer], endpointUri, app)
}

object IrcProducer {
  def props(endpointUri: String): Props =
    Props(classOf[IrcProducer], endpointUri)
}

class IrcConsumer(val endpointUri: String, val app: ActorRef) extends Actor with ActorLogging with Consumer {

  def receive: Receive = {
    case msg: CamelMessage => {
      val messageType = msg.headerAs[String]("irc.messageType").get
      if (messageType == "PRIVMSG") {
        val message: String = msg.bodyAs[String]
        val target: String = msg.headerAs[String]("irc.target").get
        val source: String = msg.headerAs[String]("irc.user.nick").get

        app ! IrcUtterance(IrcUser(source), IrcDestination(target), message)
      }
    }
  }
}

class IrcProducer(val endpointUri: String) extends Producer with Oneway {
}

class IrcConnection(remote: InetSocketAddress, app: ActorRef) extends Actor with ActorLogging {
  import context._

  val endpointUri: String = {
    import SpraffBot._
    import SpraffBot.ircServer.{ getHostName, getPort }
    s"ircs:${username}@${getHostName}:${getPort}/${ircChannels.reduce(_ + "," + _)}"
  }
  log.info(s"Endpoint: ${endpointUri}")

  private val consumer: ActorRef =
    context.actorOf(IrcConsumer.props(endpointUri, app), "inbound")

  private val producer: ActorRef =
    context.actorOf(IrcProducer.props(endpointUri), "outbound")

  def receive: Receive = {
    case msg: SayMessage => {
      producer ! CamelMessage(msg.message, Map("irc.target" -> msg.to.target))
    }
  }

}

