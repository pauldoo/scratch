package com.pauldoo.spraffbot.irc

import akka.io.SymmetricPipelineStage
import akka.io.PipelineContext
import akka.io.SymmetricPipePair
import scala.util.parsing.combinator.RegexParsers

class IrcMessageStage extends SymmetricPipelineStage[PipelineContext, IrcMessage, String] {

  def apply(ctx: PipelineContext) = new SymmetricPipePair[IrcMessage, String] {
    override val commandPipeline = { msg: IrcMessage =>
      val segments: Seq[Option[String]] =
        msg.prefix.map(":" + _) ::
          Some(msg.command) ::
          (msg.params.map(Some(_)) :+
            msg.trailing.map(":" + _));
      val message = segments.flatten.mkString(" ")
      ctx.singleCommand(message);
    }

    override val eventPipeline = { str: String =>
      ctx.singleEvent(IrcMessageParser(str))
    }
  }
}

