Raytracer to simulate the trillion fps camera: http://web.media.mit.edu/~raskar/trillionfps/

To generate eclipse project files: sbt eclipse

Test: sbt test

Style: sbt scalastyle-generate-config / sbt scalastyle


PLAN:

Will start by doing all lighting (even direct) from photon map
Raytrace out from camera will find first hit, then query PM
Later extend this to recurse only down the direct (straight) path, in case of participating media


TODO:
* Normalize photon intensity wrt how many were emitted
* Participating media
* Global illumination
* Specular surfaces
* Movable surfaces
* Movable lights
* Movable camera
