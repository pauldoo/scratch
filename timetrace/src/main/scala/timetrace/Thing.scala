package timetrace

import timetrace.shape.Shape
import timetrace.material.Material

sealed case class Thing(val shape: Shape, val material: Material) {

}