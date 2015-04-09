package com.pauldoo.spraffbot.toys

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.Props
import com.pauldoo.spraffbot.irc.IrcUtterance
import com.pauldoo.spraffbot.irc.SayMessage
import scala.util.Random

object Reviewer {
  def props(): Props =
    Props(classOf[Cheer])
}

class Reviewer extends Actor with ActorLogging {
  import context._

  private val rng : Random = new Random()

  private val reviewers : List[String] = List(
    "caluml",
    "graemeh",
    "hougaard",
    "kodzhaba",
    "laddac",
    "paulrich",
    "qinanlai",
    "reubenp",
    "stevenm")

  def receive: Receive = {
    case u: IrcUtterance => {
      if (u.message.startsWith("!rr")) {
        sender ! SayMessage(u.replyDestination, pickRandomReviewer())
      }
    }
  }

  private def pickRandomReviewer(): String =
     rng.shuffle(reviewers).head
}
