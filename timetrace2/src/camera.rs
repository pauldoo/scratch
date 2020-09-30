use crate::geometry::vector::Vector4;
use crate::geometry::ray::Ray;
use crate::geometry::direction::Direction;
use crate::geometry::normal::Normal;
use crate::geometry::ERROR_EPSILON;

pub trait Camera : Sync {
    fn emit(&self, xfrac: f64, yfrac: f64, t: f64) -> Ray;
}

pub struct StaticCamera {
    position: Vector4,
    look_direction: Normal,
    up_direction: Normal
}

impl StaticCamera {
    pub fn new(
        position: Vector4,
        look_direction: Normal,
        up_direction: Normal
    ) -> Box<dyn Camera> {
        assert!(Vector4::dot(look_direction.into(), up_direction.into()) <= ERROR_EPSILON);
        return Box::new(StaticCamera {
            position,
            look_direction,
            up_direction
        });
    }

    fn right_direction(&self) -> Normal {
        return Normal::fromVec(
            Vector4::cross_3(
                    Vector4::from(self.look_direction),
                    Vector4::from(self.up_direction)));
    }
}

impl Camera for StaticCamera {
    fn emit(&self, xfrac: f64, yfrac: f64, t: f64) -> Ray {
        let direction: Vector4 =
            Vector4::from(self.look_direction) +
            Vector4::from(self.up_direction) * yfrac +
            Vector4::from(self.right_direction()) * xfrac;

        return Ray {
            start: self.position.with_t(t),
            direction: Direction::fromVec(
                *(direction * ( 1.0 / direction.l2norm())).set_t(-1.0) )
        };
    }
}
