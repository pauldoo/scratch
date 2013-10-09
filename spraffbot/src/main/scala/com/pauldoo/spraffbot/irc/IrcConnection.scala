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
import akka.actor.ActorRef

object IrcConnection {
  def props(app: ActorRef): Props =
    Props(classOf[IrcConnection], new InetSocketAddress("localhost", 6667), app);
}

class IrcConnection(remote: InetSocketAddress, app: ActorRef) extends Actor with ActorLogging {

  val handlers: List[ActorRef] = List(
    context.actorOf(Ping.props, "ping"),
    context.actorOf(Privmsg.props(app), "privmsg"));

  {
    import context.system
    IO(Tcp) ! Tcp.Connect(remote);
  }

  def receive = unconnectedReceive;

  def unconnectedReceive: Receive = {
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

      val send = sendMessage(pipeline, init)_;
      send(IrcMessage(None, "NICK", List("spraffbot")));
      send(IrcMessage(None, "USER", List("spraffbot", "0", "*", "Sir Spraff")));
      send(IrcMessage(None, "JOIN", List("#sprafftest")));
      context become connectedReceive(init, send)
    }
  }

  def sendMessage(pipeline: ActorRef, init: Init[WithinActorContext, IrcMessage, IrcMessage])(message: IrcMessage): Unit = {
    log.info(s"> ${message}")
    pipeline ! init.Command(message)
  }

  def connectedReceive(init: Init[WithinActorContext, IrcMessage, IrcMessage], send: IrcMessage => Unit): Receive = {
    case init.Event(data) => {
      log.info(s"< ${data}")
      // TODO: replace with a pub-sub thing.
      for (h <- handlers) h ! data
    }
    case _: Tcp.ConnectionClosed => {
      log.info("Connection closed")
      context stop self
    }
    case message: IrcMessage => {
      send(message)
    }
  }
}

