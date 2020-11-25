use crate::geometry::direction::Direction;
use crate::geometry::vector::Vector4;

#[derive(Copy, Clone, Debug)]
pub struct Ray {
    pub start: Vector4,
    pub direction: Direction,
}

impl Ray {
    pub fn march(&self, t: f64) -> Vector4 {
        return self.start + (Vector4::from(self.direction) * t);
    }
}
