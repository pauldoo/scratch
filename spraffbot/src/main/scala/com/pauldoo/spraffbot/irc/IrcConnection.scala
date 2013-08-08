package com.pauldoo.spraffbot.irc

import java.net.InetSocketAddress

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.Deploy
import akka.actor.Props
import akka.actor.actorRef2Scala
import akka.io.IO
import akka.io.Tcp
import akka.io.TcpPipelineHandler
import akka.io.TcpPipelineHandler.Init
import akka.io.TcpPipelineHandler.WithinActorContext
import akka.io.TcpReadWriteAdapter

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
        new IrcMessageStage() >>
          new BreakIntoLinesStage() >>
          new TcpReadWriteAdapter());

      val pipeline = context.actorOf(TcpPipelineHandler.props(
        init, connection, self).withDeploy(Deploy.local))

      connection ! Tcp.Register(pipeline);

      pipeline ! init.Command(IrcMessage(None, "NICK", List("spraffbot"), None));
      pipeline ! init.Command(IrcMessage(None, "USER", List("spraffbot", "0", "*"), Some("Sir Spraff")));

      context become connectedReceive(init)
    }
    case k => {
      log.info("Recieved something else")
      log.info(k.toString)
    }

  }

  def connectedReceive(init: Init[WithinActorContext, IrcMessage, IrcMessage]): Receive = {
    case init.Event(data) => {
      log.info(s"< ${data}")
    }
    case _: Tcp.ConnectionClosed => {
      log.info("Connection closed")
      context stop self
    }
  }
}

