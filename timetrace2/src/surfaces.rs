use crate::geometry::vector::Vector4;
use crate::geometry::ray::Ray;
use crate::geometry::impact::{Impact, SurfaceType};
use crate::geometry::normal::Normal;
use std::cmp::{min, max};
use ordered_float::OrderedFloat;
use rand::prelude::ThreadRng;
use rand::Rng;

#[cfg(test)]
mod tests;

pub trait Surface : Sync {
    fn intersect(&self, ray: Ray, rng: &mut ThreadRng) -> Option<Impact>;
}

pub struct Fog {
    half_length: f64
}

impl Fog {
    pub fn new(half_length: f64) -> Box<dyn Surface> {
        assert!(half_length > 0.0);
        return Box::new(Fog {
            half_length
        });
    }
}

impl Surface for Fog {
    fn intersect(&self, ray: Ray, rng: &mut ThreadRng) -> Option<Impact> {
        let p: f64 = 1.0 - rng.gen_range(0.0, 1.0);
        assert!(p > 0.0 && p <= 1.0);
        let d: f64 = self.half_length * ((1.0f64 / p ).ln() / (2.0f64).ln());
        return Option::Some(
            Impact::create(d,SurfaceType::Gas)
        );
    }
}

pub struct StaticPlane {
    point_on_plane: Vector4,
    normal: Normal,
}

impl StaticPlane {
    pub fn new(point_on_plane: Vector4, normal: Normal) -> Box<dyn Surface> {
        assert_eq!(point_on_plane.t(), 0.0);
        return Box::new(StaticPlane {
            point_on_plane,
            normal,
        });
    }
}

impl Surface for StaticPlane {
    fn intersect(&self, ray: Ray, _: &mut ThreadRng) -> Option<Impact> {
        let distance_to_surface = Vector4::dot(self.point_on_plane - ray.start, self.normal.into());
        let rate_of_approach = Vector4::dot(ray.direction.into(), self.normal.into());

        let time_to_approach = distance_to_surface / rate_of_approach;

        if time_to_approach.is_finite() && time_to_approach >= 0.0 {
            let normal: Normal = if distance_to_surface.is_sign_positive() { self.normal.flip() } else { self.normal };
            return Option::Some(Impact::create(time_to_approach, SurfaceType::Solid{normal}));
        }

        return Option::None;
    }
}

pub struct StaticSphere {
    center: Vector4,
    radius: f64
}

impl StaticSphere {
    pub fn new(center: Vector4, radius: f64) -> Box<dyn Surface> {
        assert_eq!(center.t(), 0.0); // t value is redundant.
        return Box::new( StaticSphere {
            center,
            radius
        });
    }
}

fn lowest_positive_quadratic_solution(a: f64, b: f64, c: f64) -> Option<f64> {
    let det = (b*b) - (4.0*a*c);
    if det >= 0.0 {
        let det_sqrt = det.sqrt();
        let x1 = OrderedFloat((-b - det_sqrt) / (2.0 * a));
        let x2 = OrderedFloat((-b + det_sqrt) / (2.0 * a));
        if min(x1, x2).0 > 0.0 {
            return Some(min(x1, x2).0);
        }
        if max(x1, x2).0 > 0.0 {
            return Some(max(x1, x2).0);
        }
    }
    return None;
}

impl Surface for StaticSphere {
    fn intersect(&self, ray: Ray, _: &mut ThreadRng) -> Option<Impact> {
        let offset = ray.start.with_t(0.0) - self.center;
        let a = Vector4::from(ray.direction).with_t(0.0).l2norm_squared();
        let b= 2.0 * Vector4::dot(Vector4::from(ray.direction).with_t(0.0), offset);
        let c = offset.l2norm_squared() - (self.radius * self.radius);

        let sol = lowest_positive_quadratic_solution(a, b, c);

        return sol.map(|t| {
            let hit_location = ray.march(t).with_t(0.0);
            let normal = Normal::from_vec((hit_location - self.center) / self.radius);
            return Impact::create(t, SurfaceType::Solid{ normal });
        });
    }

}
