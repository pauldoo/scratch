package com.pauldoo.spraffbot.irc

// A message of the IRC protocol
case class IrcProtocolMessage(
  val prefix: Option[String],
  val command: String,
  val params: List[String]) {
}