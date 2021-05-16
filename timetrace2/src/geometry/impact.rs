use crate::geometry::vector::Vector4;
use crate::geometry::normal::Normal;
use approx::AbsDiffEq;
use crate::constants::SMALL_DISTANCE;
use crate::geometry::ray::Ray;

#[derive(Copy, Clone, Debug, PartialEq)]
pub struct Impact {
    time_to_hit: f64,
    surface_type: SurfaceType
}

#[derive(Copy, Clone, Debug, PartialEq)]
pub enum SurfaceType {
    Solid {
        normal: Normal
    },
    Gas,
}

impl Impact {
    pub fn create(time_to_hit: f64, surface_type: SurfaceType) -> Impact {
        assert!(time_to_hit > 0.0);
        return Impact {
            time_to_hit,
            surface_type
        };
    }

    pub fn time_to_hit(&self) -> f64 {
        return self.time_to_hit;
    }

    pub fn surface_type(&self) -> SurfaceType { return self.surface_type; }
}

impl AbsDiffEq for SurfaceType where {
    type Epsilon = f64;

    fn default_epsilon() -> Self::Epsilon {
        return SMALL_DISTANCE;
    }

    fn abs_diff_eq(&self, other: &Self, epsilon: Self::Epsilon) -> bool {
        match (self, other) {
            (SurfaceType::Gas, SurfaceType::Gas) =>
                true,

            (SurfaceType::Solid{ normal: n1 }, SurfaceType::Solid{ normal: n2}) =>
                n1.abs_diff_eq(n2, epsilon),

            (x, y) =>
                false
        }
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
            self.surface_type.abs_diff_eq(&other.surface_type, epsilon);
    }
}
