object RandomCycles {
  def main(args: Array[String]) = {
    for (i <- 0 to 24) {
      val n = 1 << i;
      println(n + " " + g(0, n, 0.0, 1.0)); //+ " " + f(0, n));
    }

  }

  def f(l: Int, n: Int): Double = {
    val r = (n - l - 1);
    (1 +
      (if (r > 0) (r * f(l + 1, n)) else 0)).toDouble / n;
  }

  def g(l: Int, n: Int, a: Double, m: Double): Double = {
    val r = (n - l - 1);
    if (r > 0)
      g(l + 1, n, a + (m / n), (m * r) / n);
    else
      a + (m / n);
  }
}