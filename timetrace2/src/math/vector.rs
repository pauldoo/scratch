use math::Dimension;
use std::ops::{Add, Sub};

#[derive(Clone,Copy,Debug)]
pub struct Vector4 {
    v: [f64; 4]
}

pub fn max_index(v: Vector4) -> Dimension {
    let mut max_value = v.x();
    let mut result = Dimension::X;

    if v.y() > max_value {
        max_value = v.y();
        result = Dimension::Y;
    }

    if v.z() > max_value {
        max_value = v.z();
        result = Dimension::Z;
    }

    if v.t() > max_value {
        //max_value = v.t();
        result = Dimension::T;
    }

    return result;
}

impl Vector4 {
    pub fn x(&self) -> f64 {
        self.v[0]
    }
    pub fn y(&self) -> f64 {
        self.v[1]
    }
    pub fn z(&self) -> f64 {
        self.v[2]
    }
    pub fn t(&self) -> f64 {
        self.v[3]
    }

    pub fn set(&mut self, dim: Dimension, value: f64) -> &mut Vector4 {
        match dim {
            Dimension::X => self.v[0] = value,
            Dimension::Y => self.v[1] = value,
            Dimension::Z => self.v[2] = value,
            Dimension::T => self.v[3] = value
        }

        return self;
    }

    pub fn get(&self, dim: Dimension) -> f64 {
        match dim {
            Dimension::X => self.x(),
            Dimension::Y => self.y(),
            Dimension::Z => self.z(),
            Dimension::T => self.t()
        }
    }

    pub const fn zero() -> Vector4 {
        Vector4::create(0.0, 0.0, 0.0, 0.0)
    }

    pub const fn create(x: f64, y: f64, z: f64, t: f64) -> Vector4 {
        Vector4 {
            v: [x, y, z, t]
        }
    }

    pub fn mins(a: Vector4, b: Vector4) -> Vector4 {
        Vector4 {
            v: [
                a.x().min(b.x()),
                a.y().min(b.y()),
                a.z().min(b.z()),
                a.t().min(b.t())
            ]
        }
    }

    pub fn maxs(a: Vector4, b: Vector4) -> Vector4 {
        Vector4 {
            v: [
                a.x().max(b.x()),
                a.y().max(b.y()),
                a.z().max(b.z()),
                a.t().max(b.t())
            ]
        }
    }

    pub fn l2norm(&self) -> f64 {
        return (
            self.x() * self.x() +
            self.y() * self.y() +
            self.z() * self.z() +
            self.t() * self.t()).sqrt();
    }
}

impl Add for Vector4 {
    type Output = Vector4;

    fn add(self, b: Vector4) -> Vector4 {
        Vector4 {
            v: [
                self.x() + b.x(),
                self.y() + b.y(),
                self.z() + b.z(),
                self.t() + b.t()
            ]
        }
    }
}

impl Sub for Vector4 {
    type Output = Vector4;

    fn sub(self, b: Vector4) -> Vector4 {
        Vector4 {
            v: [
                self.x() - b.x(),
                self.y() - b.y(),
                self.z() - b.z(),
                self.t() - b.t()
            ]
        }
    }
}
