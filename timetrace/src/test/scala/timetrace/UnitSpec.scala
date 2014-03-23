package timetrace

import org.scalatest.FlatSpec
import org.scalatest.prop.PropertyChecks
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.Matchers

abstract class UnitSpec extends FlatSpec with PropertyChecks with Matchers {}
