use crate::math::vector::Vector4;

pub trait Light {

}

pub struct IntervalLight {
    from: Vector4,
    to: Vector4
}

impl IntervalLight {
    pub fn new(
        from: Vector4,
        to: Vector4
    ) -> Box<dyn Light> {
        return Box::new(IntervalLight {
            from, to
        });
    }
}

impl Light for IntervalLight {

}