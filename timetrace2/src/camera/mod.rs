use crate::math::vector::Vector4;

pub trait Camera {

}

pub struct StaticCamera {
}

impl StaticCamera {
    pub fn new (
        position: Vector4,
        look_direction: Vector4,
        up_direction: Vector4,
        t_min: f64,
        t_max: f64
    ) -> Box<dyn Camera> {
        unimplemented!();
    }
}