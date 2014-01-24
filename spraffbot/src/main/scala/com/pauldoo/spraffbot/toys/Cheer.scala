package com.pauldoo.spraffbot.toys

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.Props
import com.pauldoo.spraffbot.irc.IrcUtterance
import com.pauldoo.spraffbot.irc.SayMessage

object Cheer {
  def props(): Props =
    Props(classOf[Cheer]);
}

class Cheer extends Actor with ActorLogging {
  import context._

  def receive: Receive = {
    case u: IrcUtterance => {
      if (u.message.contains("\\o/")) {
        sender ! SayMessage(u.replyDestination, "🙌")
      }
    }
  }
}
