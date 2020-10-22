use crate::geometry::vector::Vector4;
use approx::AbsDiffEq;
use crate::constants::SMALL_DISTANCE;

#[derive(Copy, Clone, Debug, PartialEq)]
pub struct Normal {
    vec: Vector4,
}

impl Normal {
    pub fn from_vec(v: Vector4) -> Normal {
        assert_eq!(v.t(), 0.0);
        assert!(
            v.is_normalized(),
            "Should be normalized: {:?}",
            v
        );
        return Normal { vec: v };
    }

    pub fn flip(&self) -> Normal {
        Normal {
            vec: -self.vec
        }
    }
}

impl From<Normal> for Vector4 {
    fn from(direction: Normal) -> Self {
        return direction.vec;
    }
}

impl AbsDiffEq for Normal where {
    type Epsilon = f64;

    fn default_epsilon() -> Self::Epsilon {
        return SMALL_DISTANCE;
    }

    fn abs_diff_eq(&self, other: &Self, epsilon: Self::Epsilon) -> bool {
        return self.vec.abs_diff_eq(&other.vec, epsilon);
    }
}
