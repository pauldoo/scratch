use crate::geometry::vector::Vector4;
use crate::geometry::normal::Normal;

#[derive(Copy, Clone, Debug, PartialEq)]
pub struct Impact {
    pub location: Vector4, // TODO: Return only the "time to hit"
    pub surface_normal: Normal
}
