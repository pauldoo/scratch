use crate::math::vector::Vector4;

pub trait Camera {

}

pub struct StaticCamera {
    position: Vector4,
    look_direction: Vector4,
    up_direction: Vector4,
    t_min: f64,
    t_max: f64
}

impl StaticCamera {
    pub fn new (
        position: Vector4,
        look_direction: Vector4,
        up_direction: Vector4,
        t_min: f64,
        t_max: f64
    ) -> Box<dyn Camera> {
        return Box::new( StaticCamera {
            position,
            look_direction,
            up_direction,
            t_min,
            t_max
        });
    }
}

impl Camera for StaticCamera {

}
