package com.pauldoo.spraffbot.spraffer

import akka.actor.Props
import java.io.File
import akka.actor.Actor
import akka.actor.ActorLogging
import scala.concurrent.Future
import akka.actor.actorRef2Scala
import scala.io.Codec

object Spraffer {
  def props(corpusFile: File): Props =
    Props(classOf[Spraffer], corpusFile);
}

class Spraffer(corpusFile: File) extends Actor with ActorLogging {

  val languageModel = context.actorOf(LanguageModel.props, "languageModel");

  {
    import context.dispatcher
    Future {
      log.info("loading corpus")
      try {
        for (line <- scala.io.Source.fromFile(corpusFile)(Codec.UTF8).getLines)
          languageModel ! LanguageModel.ConsumeSentence(line)
      } catch {
        case e: Exception => log.error(e, "Failed to load corpus");
      }
      log.info("corpus loaded")
    }
  }

  def receive: Receive = {
    case s: String => {
      log.info(s);
    }
  }

}