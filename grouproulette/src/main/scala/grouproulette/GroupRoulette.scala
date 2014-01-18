package grouproulette

import scala.util.Random
import scala.math.{ log, floor }

object GroupRoulette {

  val trials = 1000
  val rng = new Random()

  def main(args: Array[String]) = {
    for (n <- 1 to 1000) {
      val e = runTrials(n)
      val f = log(n) - floor(log(n))
      println(s"$e $f")
    }
  }

  def selectRandom[T](set: Set[T]): T = {
    rng.shuffle(set.toList).head
  }

  def singleRound[T](people: Set[T]): Set[T] = {
    val targets: Set[T] = people.map(p => selectRandom(people - p))
    people &~ targets
  }

  def runTrials(n: Int): Double = {
    val initialSet: Set[Int] = (1 to n).toSet

    val outcomes: Seq[Boolean] = for (t <- 1 to trials) yield {
      val finalSet = Iterator.iterate(initialSet)(singleRound _).dropWhile(_.size >= 2).toSeq.head
      !(finalSet.isEmpty)
    }

    assert(outcomes.size == trials)

    outcomes.filter(identity).size.toDouble / outcomes.size
  }

}