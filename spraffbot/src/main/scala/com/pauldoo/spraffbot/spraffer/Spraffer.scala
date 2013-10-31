package com.pauldoo.spraffbot.spraffer

import akka.actor.Props
import java.io.File
import akka.actor.Actor
import akka.actor.ActorLogging
import scala.concurrent.Future
import akka.actor.actorRef2Scala
import scala.io.Codec
import com.pauldoo.spraffbot.irc.IrcUtterance
import akka.pattern.{ ask, pipe }
import akka.util.Timeout
import scala.concurrent.duration._
import scala.language.postfixOps
import com.pauldoo.spraffbot.irc.SayMessage
import com.pauldoo.spraffbot.SpraffBot
import scala.util.Random

object Spraffer {
  def props(corpusFile: File): Props =
    Props(classOf[Spraffer], corpusFile);
}

class Spraffer(corpusFile: File) extends Actor with ActorLogging {
  import context._

  val languageModel = context.actorOf(LanguageModel.props, "languageModel");
  val corpusWriter = context.actorOf(CorpusWriter.props(corpusFile), "corpusWriter");
  val random: Random = new Random();

  // TODO: Move this into the CorpusWriter?
  {
    Future {
      log.info("loading corpus")
      try {
        for (line <- scala.io.Source.fromFile(corpusFile)(Codec.UTF8).getLines) {
          languageModel ! ConsumeSentence(line)
        }
      } catch {
        case e: Exception => log.error(e, "Failed to load corpus");
      }
      log.info("corpus loaded")
    }
  }

  def receive: Receive = {
    case u: IrcUtterance => {
  	  corpusWriter ! u.message;

      val issueRandomReply = (random.nextDouble < SpraffBot.randomResponseRate); 
      if (u.message.contains(SpraffBot.username) || issueRandomReply) {
        implicit val timeout: Timeout = Timeout(1 minute);
        val f: Future[GeneratedSentece] = (languageModel ? (new GenerateSentence(u.message))).mapTo[GeneratedSentece];
        f.map(s => new SayMessage(u.to, s.sentence)) pipeTo sender;
      }
      
      languageModel ! new ConsumeSentence(u.message);
    }
    case s: String => {
      log.info(s);
    }
  }

}