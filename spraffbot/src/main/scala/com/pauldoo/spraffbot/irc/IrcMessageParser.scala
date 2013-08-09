package com.pauldoo.spraffbot.irc

import scala.util.parsing.combinator.RegexParsers

object IrcMessageParser extends RegexParsers {
  override val skipWhitespace = false;

  def prefix: Parser[String] = """[\S]+""".r
  def command: Parser[String] = """[\S]+""".r
  def param: Parser[String] = """[^:][\S]*""".r
  def trailing: Parser[String] = """.*""".r

  def message: Parser[IrcMessage] = (":" ~ prefix ~ " ").? ~ command ~ (" " ~ param).* ~ (" :" ~ trailing).? ^^ {
    case prefix ~ command ~ params ~ trailing =>
      {
        val prefix_ = prefix.map(_ match { case (":" ~ p ~ " ") => p })
        val params_ = params.map(_ match { case (" " ~ p) => p })
        val trailing_ = trailing.map(_ match { case (" :" ~ t) => t })
        new IrcMessage(prefix_, command, (params_.map(Some(_)) :+ trailing_) flatten);
      }
  }

  def apply(input: String): IrcMessage = {
    parseAll(message, input) match {
      case Success(result, _) => result
      case _ => throw new IllegalArgumentException(
        s"Unable to parse IRC message: ${input}")
    }
  }
}
