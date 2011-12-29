import scala.io.Source
import scala.actors._
import scala.xml.XML
import java.net.URL
import Actor._


val urls = List(
    "http://en.wikipedia.org/wiki/Henry_Harmer")

def timeMethod(method: () => Unit) = {
    val start = System.nanoTime
    method()
    val end = System.nanoTime
    println("Method took " + (end - start)/(1e9) + " seconds.")
}

def getPageSize(url: URL, depth: Int) : Map[URL, Int] = {
    println("Fetching: " + url + " - depth=" + depth);
    try {
        val contentAsString = Source.fromURL(url).mkString;
        val partialResult = Map(url -> contentAsString.length);
        if (depth > 0) {
            val xml = XML.loadString(contentAsString);
            val links = (xml \\ "a").flatMap( (i) => (i \ "@href").map( (k) => new URL(url, k.toString)) ) : Seq[URL];
            val caller = self
            for (child <- links) {
                actor { caller ! getPageSize(child, depth-1) }
            }
            val all_sizes = links.map( (f) =>
                receive {
                    case sizes : Map[URL, Int] =>
                        sizes;
                } ) : Seq[Map[URL, Int]];
            return (partialResult :: (all_sizes toList)).reduce( (a, b) => a ++ b );
        } else {
            return partialResult;
        }
    } catch {
        case ioe: java.io.IOException => {
            return Map(url -> 0)
        }
    }
}

def getPageSizeConcurrently() = {
    val caller = self
    for (url <- urls) {
        actor { caller ! (url, getPageSize(new URL(url), 1)) }
    }
    for (i <- 1 to urls.size) {
        receive {
            case (url, size) =>
                println("Size for " + url + ": " + size)
        }
    }
}

println("Concurrent run:")
timeMethod { getPageSizeConcurrently }

