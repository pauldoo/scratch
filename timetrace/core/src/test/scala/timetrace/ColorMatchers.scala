package timetrace

import org.scalatest.matchers.Matcher
import org.scalatest.matchers.MatchResult

trait ColorMatchers {
  class ColorApproximatelyEqualMatcher(expectedColor: Color) extends Matcher[Color] {
    def apply(actualColor: Color) = {

      val maxAbsDiff: Double = List( //
        actualColor.red - expectedColor.red, //
        actualColor.green - expectedColor.green, // 
        actualColor.blue - expectedColor.blue).map(Math.abs _).max

      MatchResult(
        maxAbsDiff < 1e-6,
        s"""Color $actualColor is not close to $expectedColor""",
        s"""Color $actualColor is $maxAbsDiff away from $expectedColor""")
    }
  }

  def colorMatch(expectedColor: Color) = new ColorApproximatelyEqualMatcher(expectedColor)
}