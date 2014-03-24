package timetrace

object Raytrace {

  def firstHit(scene: Scene, ray: Ray): Option[RayHit] = {
    def pickClosest(a: RayHit, b: RayHit) = {
      if (a.t < b.t) a else b
    }

    scene.things.flatMap(_.shape.intersect(ray)).reduceOption(pickClosest _)
  }

}