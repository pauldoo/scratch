package com.pauldoo.spraffbot.irc

import java.net.InetSocketAddress

import scala.annotation.tailrec

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.Deploy
import akka.actor.Props
import akka.actor.actorRef2Scala
import akka.io.IO
import akka.io.PipelineContext
import akka.io.SymmetricPipePair
import akka.io.SymmetricPipelineStage
import akka.io.Tcp
import akka.io.TcpPipelineHandler
import akka.io.TcpPipelineHandler.Init
import akka.io.TcpPipelineHandler.WithinActorContext
import akka.io.TcpReadWriteAdapter
import akka.util.ByteString

object IrcConnection {
  def props(): Props =
    Props(classOf[IrcConnection], new InetSocketAddress("localhost", 6667));
}

class IrcConnection(remote: InetSocketAddress) extends Actor with ActorLogging {

  {
    import context.system
    IO(Tcp) ! Tcp.Connect(remote);
  }

  def receive = {
    case c @ Tcp.Connected(remote, local) => {
      log.info("Connected!");
      val connection = sender;

      val init = TcpPipelineHandler.withLogger(log,
        new BreakIntoLinesStage() >>
          new TcpReadWriteAdapter());

      val pipeline = context.actorOf(TcpPipelineHandler.props(
        init, connection, self).withDeploy(Deploy.local))

      connection ! Tcp.Register(pipeline);

      pipeline ! init.Command("NICK spraffbot");
      pipeline ! init.Command("USER spraffbot 0 * :SpraffBot");

      context become connectedReceive(init)
    }
    case k => {
      log.info("Recieved something else")
      log.info(k.toString)
    }

  }

  def connectedReceive(init: Init[WithinActorContext, String, String]): Receive = {
    case init.Event(data) => {
      log.info(s"< ${data}")
    }
    case _: Tcp.ConnectionClosed => {
      log.info("Connection closed")
      context stop self
    }
  }
}

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