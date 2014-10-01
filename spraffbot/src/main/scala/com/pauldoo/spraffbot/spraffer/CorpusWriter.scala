package com.pauldoo.spraffbot.spraffer

import akka.actor.Props
import java.io.File
import akka.actor.Actor
import akka.actor.ActorLogging
import java.io.OutputStreamWriter
import java.io.FileOutputStream

object CorpusWriter {
  def props(corpusFile: File): Props =
    Props(classOf[CorpusWriter], corpusFile)
}

class CorpusWriter(corpusFile: File) extends Actor with ActorLogging {
  def receive(): Receive = {
    case s: String => {
      val writer = new OutputStreamWriter(new FileOutputStream(corpusFile, true), "UTF-8")
      try {
        writer.write(s + "\n")
      } finally {
        writer.close()
      }
    }
  }
}