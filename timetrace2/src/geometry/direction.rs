use crate::geometry::vector::Vector4;
use rand::Rng;
use crate::geometry::normal::Normal;

#[cfg(test)]
mod tests;

#[derive(Copy, Clone, Debug)]
pub struct Direction {
    vec: Vector4,
}

impl Direction {
    pub fn from_vec(v: Vector4) -> Direction {
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
                return Direction::from_vec(*(v / len).set_t(t));
            }
        }
    }

    pub fn random_in_hemisphere(rng: &mut impl Rng, t: f64, hemisphere: Normal) -> Direction {
        loop {
            let r = Direction::random(rng, t);

            if Vector4::dot(r.vec, Vector4::from(hemisphere)) > 0.0 {
                return r;
            }
        }
    }
}

impl From<Direction> for Vector4 {
    fn from(direction: Direction) -> Self {
        return direction.vec;
    }
}
