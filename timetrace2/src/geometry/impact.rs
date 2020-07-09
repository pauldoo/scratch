use crate::geometry::vector::Vector4;
use crate::geometry::normal::Normal;

#[derive(Copy, Clone, Debug, PartialEq)]
pub struct Impact {
    pub location: Vector4,
    pub surface_normal: Normal
}
