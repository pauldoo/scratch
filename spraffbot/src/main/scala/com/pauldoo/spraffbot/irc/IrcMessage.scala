package com.pauldoo.spraffbot.irc

// A message of the IRC protocol
case class IrcMessage(
  val prefix: Option[String],
  val command: String,
  val params: List[String]) {
}