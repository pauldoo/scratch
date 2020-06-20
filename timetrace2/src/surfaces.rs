use crate::geometry::vector::{Vector4, dot};
use crate::geometry::ray::Ray;
use crate::geometry::impact::Impact;

#[cfg(test)]
mod tests;

pub trait Surface {
    fn intersect(&self, ray: Ray) -> Option<Impact>;
}

pub struct StaticPlane {
    point_on_plane: Vector4,
    normal: Vector4,
}

impl StaticPlane {
    pub fn new(point_on_plane: Vector4, normal: Vector4) -> Box<dyn Surface> {
        assert_eq!(point_on_plane.t(), 0.0);
        assert_eq!(normal.t(), 0.0);
        assert!(normal.is_normalized());
        return Box::new(StaticPlane {
            point_on_plane,
            normal,
        });
    }
}

impl Surface for StaticPlane {
    fn intersect(&self, ray: Ray) -> Option<Impact> {
        let distance_to_surface = dot(self.point_on_plane - ray.start, self.normal);
        let rate_of_approach = dot(ray.direction.into(), self.normal);

        let time_to_approach = distance_to_surface / rate_of_approach;

        if time_to_approach.is_finite() && time_to_approach >= 1e-3 {
            return Option::Some(Impact{
                location: ray.start + (Vector4::from(ray.direction) * time_to_approach),
                surface_normal: self.normal * (-distance_to_surface.signum())
            });
        }

        return Option::None;
    }
}
