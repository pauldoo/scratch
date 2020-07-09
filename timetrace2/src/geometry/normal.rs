use crate::geometry::vector::Vector4;

#[derive(Copy, Clone, Debug, PartialEq)]
pub struct Normal {
    vec: Vector4,
}

impl Normal {
    pub fn fromVec(v: Vector4) -> Normal {
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
