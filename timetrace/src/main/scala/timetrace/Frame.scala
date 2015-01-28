package timetrace

case class Frame(val number: Int, val width: Int, val height: Int, val pixels: Array[Color]) {
  assert(pixels.length == width * height)

}