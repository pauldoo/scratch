package com.pauldoo.spraffbot.irc

// Incoming message said in a channel or private message
case class IrcUtterance(
  val from: IrcUser,
  val to: IrcDestination,
  val message: String) {
}

case class IrcDestination(target: String) {}

case class IrcUser(user: String) {}

case class IrcContext(me: IrcUser) {}
