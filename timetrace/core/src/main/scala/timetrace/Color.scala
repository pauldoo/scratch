package timetrace

object Color {
  val BLACK = Color(0.0, 0.0, 0.0)
  val WHITE = Color(1.0, 1.0, 1.0)
}

sealed case class Color(val red: Double, val green: Double, val blue: Double) {
  assume(red >= 0.0)
  assume(green >= 0.0)
  assume(blue >= 0.0)

  def +(that: Color): Color = new Color(
    this.red + that.red,
    this.green + that.green,
    this.blue + that.blue)

  def *(v: Double): Color = new Color(
    red * v,
    green * v,
    blue * v)

}
