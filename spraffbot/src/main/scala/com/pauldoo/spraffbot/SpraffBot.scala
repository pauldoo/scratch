package com.pauldoo.spraffbot

import com.pauldoo.spraffbot.irc.IrcConnection
import akka.actor.Actor
import akka.actor.ActorLogging
import com.pauldoo.spraffbot.irc.IrcUtterance
import akka.actor.ActorRef
import spraffer.Spraffer
import java.io.File

class SpraffBot extends Actor with ActorLogging {
  val connection = context.actorOf(IrcConnection.props(self), "irc");
  val handlers: List[ActorRef] = List(
    context.actorOf(Spraffer.props(new File("corpus.txt")), "spraffer"));

  def receive = {
    case k: IrcUtterance => {
      log.info(k.toString);
      for (h <- handlers) h ! k
    }
  }
}