package com.pauldoo.spraffbot.spraffer

case class ConsumeSentence(
  val sentence: String)
case class GenerateSentence(
  val prompt: String)
case class GeneratedSentece(
  val sentence: String)