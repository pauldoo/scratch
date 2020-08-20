use crate::geometry::vector::Vector4;
use crate::geometry::ray::Ray;
use crate::geometry::impact::Impact;
use crate::geometry::normal::Normal;

#[cfg(test)]
mod tests;

pub trait Surface {
    fn intersect(&self, ray: Ray) -> Option<Impact>;
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
    fn intersect(&self, ray: Ray) -> Option<Impact> {
        let distance_to_surface = Vector4::dot(self.point_on_plane - ray.start, self.normal.into());
        let rate_of_approach = Vector4::dot(ray.direction.into(), self.normal.into());

        let time_to_approach = distance_to_surface / rate_of_approach;

        if time_to_approach.is_finite() && time_to_approach >= 1e-3 {
            let normal: Normal = if distance_to_surface.is_sign_positive() { self.normal.flip() } else { self.normal };
            return Option::Some(Impact{
                location: ray.start + (Vector4::from(ray.direction) * time_to_approach),
                surface_normal: normal
            });
        }

        return Option::None;
    }
}
