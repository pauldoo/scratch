use crate::surfaces::Surface;
use crate::lights::Light;
use crate::camera::Camera;

pub struct Scene {
    pub surfaces: Vec<Box<dyn Surface>>,
    pub lights: Vec<Box<dyn Light>>,
    pub camera: Box<dyn Camera>
}
