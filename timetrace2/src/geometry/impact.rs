use crate::geometry::vector::Vector4;
use crate::geometry::normal::Normal;
use approx::AbsDiffEq;
use crate::constants::SMALL_DISTANCE;
use crate::geometry::ray::Ray;

#[derive(Copy, Clone, Debug, PartialEq)]
pub struct Impact {
    time_to_hit: f64,
    surface_normal: Normal
}

impl Impact {
    pub fn create(time_to_hit: f64, surface_normal: Normal) -> Impact {
        assert!(time_to_hit > 0.0);
        return Impact {
            time_to_hit,
            surface_normal
        };
    }

    pub fn time_to_hit(&self) -> f64 {
        return self.time_to_hit;
    }
}

impl AbsDiffEq for Impact where {
    type Epsilon = f64;

    fn default_epsilon() -> Self::Epsilon {
        return SMALL_DISTANCE;
    }

    fn abs_diff_eq(&self, other: &Self, epsilon: Self::Epsilon) -> bool {
        return
            self.time_to_hit.abs_diff_eq(&other.time_to_hit, epsilon) &&
            self.surface_normal.abs_diff_eq(&other.surface_normal, epsilon);
    }
}
