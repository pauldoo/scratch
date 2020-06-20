use crate::geometry::vector::Vector4;
use rand::Rng;

#[cfg(test)]
mod tests;

#[derive(Copy, Clone, Debug)]
pub struct Direction {
    vec: Vector4,
}

impl Direction {
    pub fn fromVec(v: Vector4) -> Direction {
        assert!(v.t() == -1.0 || v.t() == 1.0);
        assert!(
            v.with_t(0.0).is_normalized(),
            "Should be normalized: {:?}",
            v
        );
        return Direction { vec: v };
    }

    pub fn random(rng: &mut impl Rng, t: f64) -> Direction {
        assert!(t == -1.0 || t == 1.0);

        loop {
            let v = Vector4::create(
                rng.gen_range(-1.0, 1.0),
                rng.gen_range(-1.0, 1.0),
                rng.gen_range(-1.0, 1.0),
                0.0,
            );

            let len = v.l2norm();

            if len >= 0.1 && len <= 1.0 {
                return Direction::fromVec(*(v / len).set_t(t));
            }
        }
    }
}

impl From<Direction> for Vector4 {
    fn from(direction: Direction) -> Self {
        return direction.vec;
    }
}
