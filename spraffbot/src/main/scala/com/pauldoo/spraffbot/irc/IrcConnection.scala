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
import com.pauldoo.spraffbot.SpraffBot
import scala.concurrent.Future
import javax.net.ssl.SSLEngine
import javax.net.ssl.SSLContext
import akka.io.SslTlsSupport
import akka.io.BackpressureBuffer

object IrcConnection {
  def props(app: ActorRef): Props =
    Props(classOf[IrcConnection], SpraffBot.ircServer, app);
}

class IrcConnection(remote: InetSocketAddress, app: ActorRef) extends Actor with ActorLogging {
  import context._

  private val handlers: List[ActorRef] =
    List(
      context.actorOf(Ping.props, "ping"),
      context.actorOf(Privmsg.props(app), "privmsg"));

  {
    IO(Tcp) ! Tcp.Connect(remote);
  }

  def receive = unconnectedReceive;

  private val sslEngine: SSLEngine = {
    val engine = SSLContext.getDefault.createSSLEngine()
    engine.setUseClientMode(true)
    log.info(s"Supported SSL protocols: ${engine.getSupportedProtocols().toList}")
    log.info(s"Enabled SSL protocols: ${engine.getEnabledProtocols().toList}")
    engine
  }

  def unconnectedReceive: Receive = {
    case Tcp.Connected(remote, local) => {
      log.info("Connected!");
      val connection = sender;

      val init = TcpPipelineHandler.withLogger(log,
        new IrcMessageStage() >>
          new BreakIntoLinesStage() >>
          new TcpReadWriteAdapter() >>
          new SslTlsSupport(sslEngine) >>
          new BackpressureBuffer(lowBytes = 1000000, highBytes = 2000000, maxBytes = 4000000) // Don't think this helps
          );

      val pipeline = context.actorOf(TcpPipelineHandler.props(
        init, connection, self).withDeploy(Deploy.local))

      connection ! Tcp.Register(pipeline);

      val send = sendMessage(pipeline, init)_;
      context become connectedReceive(init, send);

      // TODO: Why does this step need to be delayed?
      Future {
        Thread.sleep(2000);
        self ! IrcProtocolMessage(None, "NICK", List(SpraffBot.username));
        self ! IrcProtocolMessage(None, "USER", List(SpraffBot.username, "0", "*", "Sir Spraff"));
        self ! IrcProtocolMessage(None, "JOIN", List(SpraffBot.ircChannel));
      }
    }
  }

  def sendMessage(pipeline: ActorRef, init: Init[WithinActorContext, IrcProtocolMessage, IrcProtocolMessage])(message: IrcProtocolMessage): Unit = {
    log.info(s"> ${message}")
    pipeline ! init.Command(message)
  }

  def connectedReceive( //
    init: Init[WithinActorContext, IrcProtocolMessage, IrcProtocolMessage], //
    send: IrcProtocolMessage => Unit): Receive = {
    case init.Event(data) => {
      log.info(s"< ${data}")
      // TODO: replace with a pub-sub thing.
      for (h <- handlers) h ! data
    }
    case _: Tcp.ConnectionClosed => {
      log.info("Connection closed")
      context stop self
    }
    case message: IrcProtocolMessage => {
      send(message)
    }
  }
}

