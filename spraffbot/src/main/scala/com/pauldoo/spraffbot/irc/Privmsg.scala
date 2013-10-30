package com.pauldoo.spraffbot.irc

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.Props
import akka.actor.ActorRef

object Privmsg {
  def props(app: ActorRef): Props =
    Props(classOf[Privmsg], app);
}

class Privmsg(app: ActorRef) extends Actor with ActorLogging {
  def receive: Receive = {
    case msg: IrcProtocolMessage => {
      msg.command match {
        case "PRIVMSG" => {
          require(msg.params.length == 2)
          val from: String = (msg.prefix.get split "!")(0)
          val to: String = msg.params(0)
          val message: String = msg.params(1)
          log.info(s"${from} -> ${to}: ${message}")

          app ! IrcUtterance(IrcUser(from), IrcDestination(to), message)
        }
        case _ => {
        }
      }
    }
  }
}