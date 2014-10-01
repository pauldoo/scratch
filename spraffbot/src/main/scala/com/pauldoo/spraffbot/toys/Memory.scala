package com.pauldoo.spraffbot.toys

import akka.actor.Actor
import akka.actor.Props
import akka.actor.ActorLogging
import com.pauldoo.spraffbot.irc.IrcUtterance
import com.pauldoo.spraffbot.irc.SayMessage

object Memory {
  def props(): Props =
    Props(classOf[Memory])

}

class Memory extends Actor with ActorLogging {
  import context._

  def receive: Receive = {
    case u: IrcUtterance => {
      if (u.message == "!memory") {
        val runtime = Runtime.getRuntime()
        val total = runtime.totalMemory()
        val free = runtime.freeMemory()
        val used = total - free

        val response = s"Total: ${toMiB(total)}MiB - Used: ${toMiB(used)}MiB - Free: ${toMiB(free)}MiB"

        sender ! SayMessage(u.replyDestination, response)
      }
    }
  }

  private def toMiB(bytes: Long): Long = (bytes.toDouble / (1024 * 1024)).round

}
