use crate::camera::Camera;
use crate::lights::Light;
use crate::surfaces::Surface;

pub struct Scene {
    pub surfaces: Vec<Box<dyn Surface>>,
    pub lights: Vec<Box<dyn Light>>,
    pub camera: Box<dyn Camera>,
}
