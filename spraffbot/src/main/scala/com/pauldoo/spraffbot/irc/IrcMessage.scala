package com.pauldoo.spraffbot.irc

case class IrcMessage(
  val prefix: Option[String],
  val command: String,
  val params: List[String]) {
}