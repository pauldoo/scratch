use crate::math::vector::Vector4;

pub trait Light {

}

pub struct IntervalLight {

}

impl IntervalLight {
    pub fn new(
        from: Vector4,
        to: Vector4
    ) -> Box<dyn Light> {
        unimplemented!();
    }
}
