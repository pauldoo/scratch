use surfaces::Surface;
use lights::Light;
use camera::Camera;

pub struct Scene {
    pub surfaces: Vec<Box<Surface>>,
    pub lights: Vec<Box<Light>>,
    pub camera: Box<Camera>
}
