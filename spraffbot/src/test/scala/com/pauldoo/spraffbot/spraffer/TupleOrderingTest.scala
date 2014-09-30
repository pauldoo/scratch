package com.pauldoo.spraffbot.spraffer

import org.scalatest.Assertions
import org.junit.Test
import scala.collection.immutable.SortedMap
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import com.pauldo.spraffbot.UnitSpec

@RunWith(classOf[JUnitRunner])
class TupleOrderingTest extends UnitSpec with SentenceTypes {
  @Test
  def ordering() {

    val expectedOrdering: List[SubSentence] = List(
      new SubSentence(None, "a", "a"),
      new SubSentence("a", None, None),
      new SubSentence("a", "", ""), // don't actually care about empty strings
      new SubSentence("a", "a", None),
      new SubSentence("a", "a", "a"),
      new SubSentence("a", "a", "b"),
      new SubSentence("a", "b", "a"),
      new SubSentence("a\0", None, None),
      new SubSentence("a\0", "b", "a"),
      new SubSentence("b", "a", "a"));

    val emptyMap: Productions = SortedMap.empty;

    val map: Productions = expectedOrdering.foldLeft(emptyMap)((m, k) => {
      val p: Pair[SubSentence, Pair[ForwardWords, BackwardWords]] = (k, null);
      m + p
    });

    println(map.keys.toList)
    assert(map.keys.toList == expectedOrdering);
  }

}