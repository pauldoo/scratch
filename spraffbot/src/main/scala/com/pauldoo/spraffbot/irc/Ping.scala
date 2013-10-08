package com.pauldoo.spraffbot.irc

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.Props

object Ping {
  def props(): Props =
    Props(classOf[Ping]);
}

class Ping extends Actor with ActorLogging {
  def receive: Receive = {
    case msg: IrcMessage => {
      msg.command match {
        case "PING" => {
          log.info("Received Ping, sending Pong.");
          val server = msg.params.head;
          sender ! IrcMessage(None, "PONG", List(server));
        }
        case _ => {
        }
      }
    }
  }
}
