use crate::geometry::vector::Vector4;
use crate::geometry::normal::Normal;
use approx::AbsDiffEq;
use crate::constants::SMALL_DISTANCE;

#[derive(Copy, Clone, Debug, PartialEq)]
pub struct Impact {
    pub location: Vector4, // TODO: Return only the "time to hit"
    pub surface_normal: Normal
}

impl AbsDiffEq for Impact where {
    type Epsilon = f64;

    fn default_epsilon() -> Self::Epsilon {
        return SMALL_DISTANCE;
    }

    fn abs_diff_eq(&self, other: &Self, epsilon: Self::Epsilon) -> bool {
        return
            self.location.abs_diff_eq(&other.location, epsilon) &&
            self.surface_normal.abs_diff_eq(&other.surface_normal, epsilon);
    }
}
