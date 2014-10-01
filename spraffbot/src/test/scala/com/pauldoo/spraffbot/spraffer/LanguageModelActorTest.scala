package com.pauldoo.spraffbot.spraffer

import org.scalatest.Assertions
import akka.testkit.TestActorRef
import akka.actor.ActorSystem
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.concurrent._
import scala.util.{ Try, Success }
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import com.pauldo.spraffbot.UnitSpec
import org.junit.Test

@RunWith(classOf[JUnitRunner])
class LanguageModelActorTest extends UnitSpec {
  implicit lazy val system = ActorSystem()
  implicit val timeout: Timeout = 500 millis

  private def pokeActor(actorRef: TestActorRef[LanguageModel], prompt: String) = {
    val future: Future[GeneratedSentece] = (actorRef ? new GenerateSentence(prompt)).mapTo[GeneratedSentece]

    future.value.get match {
      case Success(GeneratedSentece(response)) => {
        response
      }
      case _ => ???
    }
  }

  @Test
  def basicTest() = {
    val actorRef = TestActorRef(new LanguageModel)

    val words = List("a", "b", "c", "d", "e", "f", "g", "h")
    val utteredSentence = words.reduce(_ + " " + _)

    actorRef ! new ConsumeSentence(utteredSentence)

    for (prompt <- words) {
      assert(pokeActor(actorRef, prompt).equals(utteredSentence))
    }
  }

  @Test
  def joiningForwardTest() = {
    val actorRef = TestActorRef(new LanguageModel)

    actorRef ! new ConsumeSentence("a b c d e f g h")
    actorRef ! new ConsumeSentence("e f g h i j k l")

    val responses: Stream[String] = {
      def get(): Stream[String] = {
        pokeActor(actorRef, "a") #:: get()
      }
      get()
    }

    assert(responses.contains("a b c d e f g h"))
    assert(responses.contains("a b c d e f g h i j k l"))
  }

  @Test
  def joiningBackwardTest() = {
    val actorRef = TestActorRef(new LanguageModel)

    actorRef ! new ConsumeSentence("a b c d e f g h")
    actorRef ! new ConsumeSentence("e f g h i j k l")

    val responses: Stream[String] = {
      def get(): Stream[String] = {
        pokeActor(actorRef, "l") #:: get()
      }
      get()
    }

    assert(responses.contains("a b c d e f g h i j k l"))
    assert(responses.contains("e f g h i j k l"))
  }

  @Test
  def joiningBothDirectionsTest() = {
    val actorRef = TestActorRef(new LanguageModel)

    actorRef ! new ConsumeSentence("a b c d e f g h")
    actorRef ! new ConsumeSentence("e f g h i j k l")

    val responses: Stream[String] = {
      def get(): Stream[String] = {
        pokeActor(actorRef, "f") #:: get()
      }
      get()
    }

    assert(responses.contains("a b c d e f g h"))
    assert(responses.contains("a b c d e f g h i j k l"))
    assert(responses.contains("e f g h"))
    assert(responses.contains("e f g h i j k l"))
  }

}