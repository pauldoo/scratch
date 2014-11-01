package timetrace

case class Frame(val width: Int, val height: Int, val pixels: Array[Color]) {
  assert(pixels.length == width * height)

}