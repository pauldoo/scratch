package com.pauldoo.spraffbot

import com.pauldoo.spraffbot.irc.IrcConnection
import akka.actor.Actor
import akka.actor.ActorLogging
import com.pauldoo.spraffbot.irc.IrcUtterance
import akka.actor.ActorRef
import spraffer.Spraffer
import java.io.File
import com.pauldoo.spraffbot.irc.SayMessage
import java.net.InetSocketAddress
import com.pauldoo.spraffbot.toys.Cheer
import com.pauldoo.spraffbot.toys.Memory

object SpraffBot {
  val username: String = "spraffbot";
  val randomResponseRate = 1.0 / 100;
  val ircServer = new InetSocketAddress("chat.freenode.net", 6697);
  val ircChannels = List("#sprafftest", "#sprafftest2");
}

// TODO: Add handler for '\o/' messages
class SpraffBot extends Actor with ActorLogging {
  val connection = context.actorOf(IrcConnection.props(self), "irc");
  val handlers: List[ActorRef] = List( //
    context.actorOf(Spraffer.props(new File("corpus.txt")), "spraffer"),
    context.actorOf(Cheer.props()),
    context.actorOf(Memory.props()));

  def receive = {
    case k: IrcUtterance => {
      log.info(k.toString);
      for (h <- handlers) h ! k
    }
    case k: SayMessage => {
      log.info(k.toString);
      connection ! k
    }
  }
}