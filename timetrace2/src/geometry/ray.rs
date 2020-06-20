use crate::geometry::direction::Direction;
use crate::geometry::vector::Vector4;

#[derive(Copy, Clone, Debug)]
pub struct Ray {
    pub start: Vector4,
    pub direction: Direction,
}
