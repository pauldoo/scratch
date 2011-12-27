println(
    List("foo", "bar", "baz").foldLeft(0)( _ + _.length ) );


trait Censor {
    def censorMapping() : Map[String, String];

    def censor(original: String) =
        censorMapping().foldLeft( original )( (s, e) => s.replaceAll(e._1, e._2) );
}

class FixedCensor extends Censor {
    val censorMapping = Map( "shoot" -> "pucky", "darn" -> "beans" );
}

val fixed = new FixedCensor;
println(fixed.censor("hello shoot world darn"));


class FileCensor(filename:String) extends Censor {
    def censorMapping() = scala.io.Source.fromFile(filename).getLines.map( line => (line.split("->")(0), line.split("->")(1)) ).toMap;
}

val filecensor = new FileCensor("censor.txt");
println(filecensor.censor("hello shoot world darn"));

