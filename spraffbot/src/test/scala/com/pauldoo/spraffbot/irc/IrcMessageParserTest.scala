package com.pauldoo.spraffbot.irc

import org.scalatest.Assertions
import org.junit.Test

class IrcMessageParserTest extends Assertions {
  @Test
  def fullExample() {
    val t = IrcMessageParser(":someprefix 999 foo bar baz :cheese strings");
    assert(t == new IrcMessage(Some("someprefix"), "999", List("foo", "bar", "baz"), Some("cheese strings")))
  }

  @Test
  def missingPrefix() {
    val t = IrcMessageParser("999 foo bar baz :cheese strings");
    assert(t == new IrcMessage(None, "999", List("foo", "bar", "baz"), Some("cheese strings")))
  }

  @Test
  def missingTrailingMessage() {
    val t = IrcMessageParser(":someprefix 999 foo bar baz");
    assert(t == new IrcMessage(Some("someprefix"), "999", List("foo", "bar", "baz"), None))
  }

  @Test
  def missingParams() {
    val t = IrcMessageParser(":someprefix 999 :cheese strings");
    assert(t == new IrcMessage(Some("someprefix"), "999", Nil, Some("cheese strings")))
  }

  @Test
  def bareBones() {
    val t = IrcMessageParser("999");
    assert(t == new IrcMessage(None, "999", Nil, None))
  }
}