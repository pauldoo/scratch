package timetrace

import org.scalatest.prop.PropertyChecks
import org.scalatest.FlatSpec
import java.io.File
import scala.annotation.tailrec
import org.scalatest.FunSuite
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.prop.Checkers
import org.scalatest.exceptions.TestFailedException
import java.io.File
import scala.annotation.tailrec
import org.scalatest.FunSuite
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.prop.Checkers
import org.scalatest.exceptions.TestFailedException
import org.scalatest.junit.JUnitSuite
import org.scalatest.prop.Checkers
import org.junit.Test

abstract class UnitSpec extends FlatSpec with PropertyChecks with Checkers {}
