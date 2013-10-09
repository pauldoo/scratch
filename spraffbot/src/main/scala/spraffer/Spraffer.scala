package spraffer

import akka.actor.Props
import java.io.File
import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorRef
import scala.concurrent.Future

object Spraffer {
  def props(corpusFile: File): Props =
    Props(classOf[Spraffer], corpusFile);
}

class Spraffer(corpusFile: File) extends Actor with ActorLogging {

  {
    import context.dispatcher
    Future {
      for (line <- scala.io.Source.fromFile(corpusFile).getLines)
        self ! line
    }
  }

  def receive: Receive = {
    case s: String => {
      log.info(s);
    }
  }

}