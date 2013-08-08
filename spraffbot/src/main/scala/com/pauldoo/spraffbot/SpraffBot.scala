package com.pauldoo.spraffbot

import com.pauldoo.spraffbot.irc.IrcConnection

import akka.actor.Actor
import akka.actor.ActorLogging

class SpraffBot extends Actor with ActorLogging {

  val irc = context.actorOf(IrcConnection.props, "irc");

  def receive = {
    case k => {
      log.info(k.toString);
    }
  }
}