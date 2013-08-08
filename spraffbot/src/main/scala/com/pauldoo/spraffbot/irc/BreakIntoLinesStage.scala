package com.pauldoo.spraffbot.irc

import akka.io.SymmetricPipelineStage
import akka.io.SymmetricPipePair
import akka.io.PipelineContext
import akka.util.ByteString
import scala.annotation.tailrec

class BreakIntoLinesStage extends SymmetricPipelineStage[PipelineContext, String, ByteString] {

  def apply(ctx: PipelineContext) = new SymmetricPipePair[String, ByteString] {
    var buffer: ByteString = ByteString.empty;

    override val commandPipeline = { msg: String =>
      val bs = ByteString.newBuilder;
      bs.putBytes(msg.getBytes());
      bs.putBytes("\r\n".getBytes());
      ctx.singleCommand(bs.result);
    }

    @tailrec
    def takeOffLines(acc: List[String]): Seq[String] = {
      // TODO: Look for "\r\n" only after UTF-8 decoding
      val index = buffer.indexOfSlice("\r\n");
      if (index == -1) {
        acc;
      } else {
        val split = buffer.splitAt(index);
        buffer = split._2.drop(2);
        takeOffLines(split._1.decodeString("UTF-8") :: acc);
      }
    }

    override val eventPipeline = { bs: ByteString =>
      buffer = buffer ++ bs;
      takeOffLines(Nil) reverseMap (Left(_));
    }
  }
}