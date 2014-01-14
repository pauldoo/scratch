package timetrace

import org.scalatest.FlatSpec
import org.scalatest.prop.PropertyChecks
import org.scalatest.matchers.ShouldMatchers

abstract class UnitSpec extends FlatSpec with PropertyChecks with ShouldMatchers {}
