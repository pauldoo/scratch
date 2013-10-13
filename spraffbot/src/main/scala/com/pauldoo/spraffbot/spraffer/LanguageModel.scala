package com.pauldoo.spraffbot.spraffer

import akka.actor.ActorLogging
import akka.actor.Actor
import akka.actor.Props
import com.pauldoo.spraffbot.spraffer.LanguageModel.ConsumeSentence
import scala.collection.immutable.SortedMap

trait SentenceTypes {
  // Productions to the "none" token denote reaching the end of a sentence
  type Token = Option[String];
  type SubSentence = Tuple3[Token, Token, Token];
  type WordTable = Map[Token, Int];
  type ForwardWords = WordTable;
  type BackwardWords = WordTable;
  type Productions = SortedMap[SubSentence, Pair[ForwardWords, BackwardWords]];
  val prefixLength = 3;
}

object LanguageModel extends SentenceTypes {
  def props: Props =
    Props(classOf[LanguageModel]);

  abstract class Message();

  case class ConsumeSentence(
    val sentence: String) extends Message;
  case class GenerateSentence(
    val prompt: String) extends Message;
  case class GeneratedSentece(
    val sentence: String) extends Message;

  def addForwardProduction(
    ngrams: Productions,
    group: Seq[Token]): Productions = {

    require(group.length == prefixLength + 1);
    val forwardGroup = new SubSentence(group(0), group(1), group(2));
    val forwardWord: Token = group(3);
    val tables: Pair[ForwardWords, BackwardWords] = ngrams.getOrElse(forwardGroup, (Map.empty, Map.empty));
    val newCount: Int = tables._1.getOrElse(forwardWord, 0) + 1;
    val newTables: Pair[ForwardWords, BackwardWords] = (tables._1 + ((forwardWord, newCount)), tables._2);
    ngrams + ((forwardGroup, newTables));
  }

  def addBackwardProduction(
    ngrams: Productions,
    group: Seq[Token]): Productions = {

    require(group.length == prefixLength + 1);
    val backwardGroup = new SubSentence(group(1), group(2), group(3));
    val backwardWord: Token = group(0);
    val tables: Pair[ForwardWords, BackwardWords] = ngrams.getOrElse(backwardGroup, (Map.empty, Map.empty));
    val newCount: Int = tables._2.getOrElse(backwardWord, 0) + 1;
    val newTables: Pair[ForwardWords, BackwardWords] = (tables._1, tables._2 + ((backwardWord, newCount)));
    ngrams + ((backwardGroup, newTables));
  }

  def consumeGroup(
    ngrams: Productions,
    group: Seq[Token]): Productions = {
    addForwardProduction(addBackwardProduction(ngrams, group), group)
  }

  def consumeSentence(
    ngrams: Productions,
    words: Seq[Token]): Productions = {

    val groups: Seq[Seq[Token]] = words.sliding(prefixLength + 1).toSeq;
    groups.foldLeft[Productions](ngrams)(consumeGroup _)
  }

  def splitSentenceIntoTokens(sentence: String): Seq[Token] =
    (None +: sentence.split("\\s+").map(_.intern).map(Option(_)) :+ None);

}

class LanguageModel extends Actor with ActorLogging with SentenceTypes {

  var ngrams: SortedMap[SubSentence, Pair[ForwardWords, BackwardWords]] = SortedMap.empty;

  def receive: Receive = {
    case ConsumeSentence(sentence) => {
      ngrams = LanguageModel.consumeSentence(ngrams, LanguageModel.splitSentenceIntoTokens(sentence));

      ngrams.map(k => println(k));
    }
  }

}