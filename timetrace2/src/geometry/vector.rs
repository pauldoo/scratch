use crate::geometry::Dimension;
use std::ops::{Add, Div, Mul, Sub, Neg};
use approx::AbsDiffEq;
use crate::constants::SMALL_DISTANCE;

#[cfg(test)]
mod tests;

#[derive(Copy, Clone, Debug, PartialEq)]
pub struct Vector4 {
    v: [f64; 4],
}

fn check_finite(v: f64) -> f64 {
    debug_assert!(v.is_finite());
    return v;
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

    pub fn set_x(&mut self, value: f64) -> &mut Vector4 {
        self.v[0] = check_finite(value);
        return self;
    }

    pub fn set_y(&mut self, value: f64) -> &mut Vector4 {
        self.v[1] = check_finite(value);
        return self;
    }

    pub fn set_z(&mut self, value: f64) -> &mut Vector4 {
        self.v[2] = check_finite(value);
        return self;
    }

    pub fn set_t(&mut self, value: f64) -> &mut Vector4 {
        self.v[3] = check_finite(value);
        return self;
    }

    pub fn with_x(&self, value: f64) -> Vector4 {
        return *(self.clone().set_x(value));
    }
    pub fn with_y(&self, value: f64) -> Vector4 {
        return *(self.clone().set_y(value));
    }
    pub fn with_z(&self, value: f64) -> Vector4 {
        return *(self.clone().set_z(value));
    }
    pub fn with_t(&self, value: f64) -> Vector4 {
        return *(self.clone().set_t(value));
    }

    pub fn with(&self, dim: Dimension, value: f64) -> Vector4 {
        return *(self.clone().set(dim, value));
    }

    pub fn set(&mut self, dim: Dimension, value: f64) -> &mut Vector4 {
        match dim {
            Dimension::X => self.set_x(value),
            Dimension::Y => self.set_y(value),
            Dimension::Z => self.set_z(value),
            Dimension::T => self.set_t(value),
        }
    }

    pub fn get(&self, dim: Dimension) -> f64 {
        match dim {
            Dimension::X => self.x(),
            Dimension::Y => self.y(),
            Dimension::Z => self.z(),
            Dimension::T => self.t(),
        }
    }

    pub fn zero() -> Vector4 {
        Vector4::create(0.0, 0.0, 0.0, 0.0)
    }

    pub fn create(x: f64, y: f64, z: f64, t: f64) -> Vector4 {
        Vector4 { v: [check_finite(x), check_finite(y), check_finite(z), check_finite(t)] }
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

    pub fn dot(lhs: Vector4, rhs: Vector4) -> f64 {
        return lhs.v.iter().zip(rhs.v.iter()).map(|e| e.0 * e.1 ).sum();
    }

    pub fn cross_3(lhs: Vector4, rhs: Vector4) -> Vector4 {
        assert_eq!(lhs.t(), 0.0);
        assert_eq!(rhs.t(), 0.0);

        return Vector4::create(
        check_finite(lhs.y() * rhs.z() - lhs.z() * rhs.y()),
        check_finite(lhs.z() * rhs.x() - lhs.x() * rhs.z()),
        check_finite(lhs.x() * rhs.y() - lhs.y() * rhs.x()),
        0.0
        );
    }

    fn element_wise_op(lhs: Vector4, rhs: Vector4, op: fn(f64, f64) -> f64) -> Vector4 {
        return Vector4 {
            v: [
                check_finite(op(lhs.x(), rhs.x())),
                check_finite(op(lhs.y(), rhs.y())),
                check_finite(op(lhs.z(), rhs.z())),
                check_finite(op(lhs.t(), rhs.t())),
            ],
        };
    }

    pub fn mins(a: Vector4, b: Vector4) -> Vector4 {
        return Vector4::element_wise_op(a, b, f64::min);
    }

    pub fn maxs(a: Vector4, b: Vector4) -> Vector4 {
        return Vector4::element_wise_op(a, b, f64::max);
    }

    pub fn l2norm_squared(&self) -> f64 {
        return self.v.iter().map(|v| v * v).sum::<f64>();
    }

    pub fn l2norm(&self) -> f64 {
        return self.l2norm_squared().sqrt();
    }

    pub fn is_normalized(&self) -> bool {
        return (self.l2norm() - 1.0).abs() <= SMALL_DISTANCE;
    }
}

impl Add for Vector4 {
    type Output = Vector4;

    fn add(self, rhs: Vector4) -> Vector4 {
        return Vector4::element_wise_op(self, rhs, |a, b| a + b);
    }
}

impl Sub for Vector4 {
    type Output = Vector4;

    fn sub(self, rhs: Vector4) -> Vector4 {
        return Vector4::element_wise_op(self, rhs, |a, b| a - b);
    }
}

impl Neg for Vector4 {
    type Output = Vector4;

    fn neg(self) -> Vector4 {
        return Vector4 {
            v: [
                check_finite(- self.x()),
                check_finite(- self.y()),
                check_finite(- self.z()),
                check_finite(- self.t()),
            ]
        };
    }
}

impl Mul<f64> for Vector4 {
    type Output = Vector4;

    fn mul(self, rhs: f64) -> Vector4 {
        return Vector4 {
            v: [
                check_finite(self.x() * rhs),
                check_finite(self.y() * rhs),
                check_finite(self.z() * rhs),
                check_finite(self.t() * rhs),
            ],
        };
    }
}

impl Div<f64> for Vector4 {
    type Output = Vector4;

    fn div(self, rhs: f64) -> Vector4 {
        return Vector4 {
            v: [
                check_finite(self.x() / rhs),
                check_finite(self.y() / rhs),
                check_finite(self.z() / rhs),
                check_finite(self.t() / rhs),
            ],
        };
    }
}

impl AbsDiffEq for Vector4 where {
    type Epsilon = f64;

    fn default_epsilon() -> Self::Epsilon {
        return SMALL_DISTANCE;
    }

    fn abs_diff_eq(&self, other: &Self, epsilon: Self::Epsilon) -> bool {
        return
            self.x().abs_diff_eq(&other.x(), epsilon) &&
                self.y().abs_diff_eq(&other.y(), epsilon) &&
                self.z().abs_diff_eq(&other.z(), epsilon) &&
                self.t().abs_diff_eq(&other.t(), epsilon);
    }
}
