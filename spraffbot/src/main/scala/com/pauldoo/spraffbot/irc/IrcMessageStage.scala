package com.pauldoo.spraffbot.irc

import akka.io.SymmetricPipelineStage
import akka.io.PipelineContext
import akka.io.SymmetricPipePair
import scala.util.parsing.combinator.RegexParsers

class IrcMessageStage extends SymmetricPipelineStage[PipelineContext, IrcProtocolMessage, String] {

  def apply(ctx: PipelineContext) = new SymmetricPipePair[IrcProtocolMessage, String] {
    override val commandPipeline = { msg: IrcProtocolMessage =>
      val segments: Seq[Option[String]] =
        msg.prefix.map(":" + _) ::
          Some(msg.command) ::
          (msg.params.dropRight(1).map(Some(_)) :+
            msg.params.lastOption.map(":" + _));
      val message = segments.flatten.mkString(" ")
      ctx.singleCommand(message);
    }

    override val eventPipeline = { str: String =>
      ctx.singleEvent(IrcMessageParser(str))
    }
  }
}

