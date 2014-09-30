package com.pauldoo.spraffbot.irc

import org.scalatest.Assertions
import org.junit.Test
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import com.pauldo.spraffbot.UnitSpec

@RunWith(classOf[JUnitRunner])
class IrcMessageParserTest extends UnitSpec {
  @Test
  def fullExample() {
    val t = IrcMessageParser(":someprefix 999 foo bar baz :cheese strings");
    assert(t == new IrcProtocolMessage(Some("someprefix"), "999", List("foo", "bar", "baz", "cheese strings")))
  }

  @Test
  def missingPrefix() {
    val t = IrcMessageParser("999 foo bar baz :cheese strings");
    assert(t == new IrcProtocolMessage(None, "999", List("foo", "bar", "baz", "cheese strings")))
  }

  @Test
  def missingTrailingMessage() {
    val t = IrcMessageParser(":someprefix 999 foo bar baz");
    assert(t == new IrcProtocolMessage(Some("someprefix"), "999", List("foo", "bar", "baz")))
  }

  @Test
  def missingParams() {
    val t = IrcMessageParser(":someprefix 999 :cheese strings");
    assert(t == new IrcProtocolMessage(Some("someprefix"), "999", List("cheese strings")))
  }

  @Test
  def bareBones() {
    val t = IrcMessageParser("999");
    assert(t == new IrcProtocolMessage(None, "999", Nil))
  }
}