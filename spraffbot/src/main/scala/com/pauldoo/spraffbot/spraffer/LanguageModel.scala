package com.pauldoo.spraffbot.spraffer

import akka.actor.ActorLogging
import akka.actor.Actor
import akka.actor.Props
import scala.collection.immutable.SortedMap
import scala.language.implicitConversions
import scala.util.Random
import scala.annotation.tailrec
import scala.collection.SeqLike

trait SentenceTypes {
  // Productions to the "none" token denote reaching the end of a sentence
  type Token = Option[String];
  type SubSentence = Tuple3[Token, Token, Token];
  type WordTable = Map[Token, Int];
  type ForwardWords = WordTable;
  type BackwardWords = WordTable;
  type Productions = SortedMap[SubSentence, Pair[ForwardWords, BackwardWords]];
  val prefixLength = 3;

  implicit def stringToToken(s: String): Token = Some(s);
}

object LanguageModel extends SentenceTypes {
  def props: Props =
    Props(classOf[LanguageModel]);

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

  def splitSentenceIntoWords(sentence: String): Seq[String] =
    sentence.split("\\s+").map(_.intern)

  def splitSentenceIntoTokens(sentence: String): Seq[Token] =
    (List(None) ++ splitSentenceIntoWords(sentence).map(Option(_)) ++ List(None, None, None));

  private def randomWeightedPick[T](n: Iterable[Tuple2[T, Int]], random: Random): T = {
    require(n.isEmpty == false);
    @tailrec
    def weightedPick[T](n: Iterable[Tuple2[T, Int]], r: Int): T = {
      require(n.isEmpty == false);
      if (r < n.head._2) {
        n.head._1
      } else {
        weightedPick(n.tail, r - n.head._2)
      }
    }

    val totalCount: Int = n.map(_._2).reduce(_ + _);
    val randomSelection = random.nextInt(totalCount);
    weightedPick(n, randomSelection)
  }

  private def selectSeed(ngrams: Productions, keywords: Seq[String], random: Random): Option[SubSentence] = {
    val candidates: Seq[Tuple2[SubSentence, Int]] = keywords.flatMap(word => {
      val begin = new SubSentence(word, None, None);
      val end = new SubSentence(word + "\0", None, None);
      ngrams.range(begin, end).mapValues((v: Pair[ForwardWords, BackwardWords]) => { v._1.values.reduce(_ + _) });
    });
    if (candidates.isEmpty) {
      None
    } else {
      Some(randomWeightedPick(candidates, random))
    }
  }

  private def generateFromSeed(seed: SubSentence, ngrams: Productions, random: Random): String = {
    require(seed._1.isDefined);
    def forward(k: SubSentence): Token = {
      randomWeightedPick(ngrams.get(k).get._1, random)
    }
    def backward(k: SubSentence): Token = {
      randomWeightedPick(ngrams.get(k).get._2, random)
    }

    def genForward(k: SubSentence): List[String] = {
      forward(k) match {
        case None => List.empty;
        case Some(word) => word +: genForward(new SubSentence(k._2, k._3, word));
      }
    }
    def genBackward(k: SubSentence): List[String] = {
      backward(k) match {
        case None => List.empty;
        case Some(word) => genBackward(new SubSentence(word, k._1, k._2)) :+ word;
      }
    }

    val prefix: List[String] = genBackward(seed);
    val suffix: List[String] = {
      if (seed._2.isDefined && seed._3.isDefined) {
        genForward(seed);
      } else {
        List.empty
      }
    };

    val middle: List[String] = List(seed._1.get) ++ seed._2.toList ++ seed._3.toList;

    (prefix ++ middle ++ suffix).reduce(_ + " " + _)
  }

  def generateSentence(ngrams: Productions, keywords: Seq[String], random: Random): Option[String] = {
    selectSeed(ngrams, keywords, random)
      .map(seed => generateFromSeed(seed, ngrams, random));
  }

}

class LanguageModel extends Actor with ActorLogging with SentenceTypes {
  log.info("Creating language model.")

  var ngrams: Productions = SortedMap.empty;
  val random: Random = new Random();

  def receive: Receive = {
    case ConsumeSentence(sentence) => {
      ngrams = LanguageModel.consumeSentence(ngrams, LanguageModel.splitSentenceIntoTokens(sentence));
    }

    case GenerateSentence(prompt) => {
      log.info(s"Generating from prompt: ${prompt}")
      val sentence: Option[String] = LanguageModel.generateSentence(ngrams, LanguageModel.splitSentenceIntoWords(prompt), random);
      sender ! new GeneratedSentece(sentence.getOrElse("I don't know about those things, why don't you teach me?"));
    }
  }

}