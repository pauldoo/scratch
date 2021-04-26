use crate::geometry::direction::Direction;
use crate::geometry::ray::Ray;
use crate::geometry::vector::Vector4;
use rand::prelude::ThreadRng;
use rand::Rng;

pub trait Light  : Sync {
    fn emit(&self, rng: &mut ThreadRng) -> Ray;

    fn energy_total(&self) -> f64;
}

pub struct IntervalLight {
    from: Vector4,
    to: Vector4,
    energy: f64
}

impl IntervalLight {
    pub fn new(from: Vector4, to: Vector4, energy: f64) -> Box<dyn Light> {
        return Box::new(IntervalLight { from, to, energy });
    }
}

impl Light for IntervalLight {
    fn emit(&self, rng: &mut ThreadRng) -> Ray {
        let d: f64 = rng.gen_range(0.0, 1.0);

        let start = self.from + (self.to - self.from) * d;
        let direction: Direction = Direction::random(rng, 1.0);

        return Ray { start, direction };
    }

    fn energy_total(&self) -> f64 {
        return self.energy;
    }
}
