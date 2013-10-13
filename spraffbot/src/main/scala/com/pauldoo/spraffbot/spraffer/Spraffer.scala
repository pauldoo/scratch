package com.pauldoo.spraffbot.spraffer

import akka.actor.Props
import java.io.File
import akka.actor.Actor
import akka.actor.ActorLogging
import scala.concurrent.Future
import akka.actor.actorRef2Scala

object Spraffer {
  def props(corpusFile: File): Props =
    Props(classOf[Spraffer], corpusFile);
}

class Spraffer(corpusFile: File) extends Actor with ActorLogging {

  val languageModel = context.actorOf(LanguageModel.props, "languageModel");

  
  {
    import context.dispatcher
    Future {
      for (line <- scala.io.Source.fromFile(corpusFile).getLines)
        languageModel ! LanguageModel.ConsumeSentence(line)
    }
  }

  def receive: Receive = {
    case s: String => {
      log.info(s);
    }
  }

}