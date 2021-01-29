use crate::surfaces::{StaticPlane, Surface, StaticSphere};
use crate::geometry::vector::Vector4;
use crate::geometry::ray::Ray;
use crate::geometry::direction::Direction;
use crate::geometry::impact::Impact;
use crate::geometry::normal::Normal;

fn flat_plane() -> Box<dyn Surface> {
    StaticPlane::new(
        Vector4::create(0.0, 0.0, 4.0, 0.0),
        Normal::from_vec(Vector4::create(0.0, 0.0, -1.0, 0.0))
    )
}

#[test]
pub fn plane_straight_hit_front_forward_in_time() -> () {
    let ray: Ray = Ray {
       start: Vector4::create(4.0, 3.0, 2.0, 1.0),
        direction: Direction::from_vec(Vector4::create(0.0, 0.0, 1.0, 1.0))
    };

    let actual:Impact = flat_plane().intersect(ray).unwrap();

    let expected: Impact = Impact::create(2.0, Normal::from_vec(Vector4::create(0.0, 0.0, -1.0, 0.0)));

    assert_eq!(actual, expected);
}

#[test]
pub fn plane_straight_hit_front_backward_in_time() -> () {
    let ray: Ray = Ray {
        start: Vector4::create(4.0, 3.0, 2.0, 1.0),
        direction: Direction::from_vec(Vector4::create(0.0, 0.0, 1.0, -1.0))
    };

    let actual:Impact = flat_plane().intersect(ray).unwrap();

    let expected: Impact = Impact::create(2.0, Normal::from_vec(Vector4::create(0.0, 0.0, -1.0, 0.0)));

    assert_eq!(actual, expected);
}

#[test]
pub fn plane_straight_hit_back() -> () {
    let ray: Ray = Ray {
        start: Vector4::create(4.0, 3.0, 6.0, 1.0),
        direction: Direction::from_vec(Vector4::create(0.0, 0.0, -1.0, 1.0))
    };

    let actual:Impact = flat_plane().intersect(ray).unwrap();

    let expected: Impact = Impact::create(2.0, Normal::from_vec(Vector4::create(0.0, 0.0, 1.0, 0.0)));

    assert_eq!(actual, expected);
}

#[test]
pub fn plane_going_away_front() -> () {
    let ray: Ray = Ray {
        start: Vector4::create(4.0, 3.0, 2.0, 1.0),
        direction: Direction::from_vec(Vector4::create(0.0, 0.0, -1.0, 1.0))
    };

    let actual:Option<Impact> = flat_plane().intersect(ray);

    assert!(actual.is_none());
}

#[test]
pub fn plane_going_away_back() -> () {
    let ray: Ray = Ray {
        start: Vector4::create(4.0, 3.0, 6.0, 1.0),
        direction: Direction::from_vec(Vector4::create(0.0, 0.0, 1.0, 1.0))
    };

    let actual:Option<Impact> = flat_plane().intersect(ray);

    assert!(actual.is_none());
}


#[test]
pub fn plane_parallel_ray() -> () {
    let ray: Ray = Ray {
        start: Vector4::create(4.0, 3.0, 2.0, 1.0),
        direction: Direction::from_vec(Vector4::create(1.0, 0.0, 0.0, 1.0))
    };

    let actual: Option<Impact> = flat_plane().intersect(ray);

    assert!(actual.is_none());
}

#[test]
pub fn ray_jaunty() -> () {
    let s: f64 = 6.0f64.sqrt();
    let ray: Ray = Ray {
        start: Vector4::create(4.0, 3.0, 2.0, 1.0),
        direction: Direction::from_vec(Vector4::create(1.0/s, 2.0/s, 1.0/s, 1.0))
    };

    let actual:Impact = flat_plane().intersect(ray).unwrap();

    let expected: Impact = Impact::create(s*2.0, Normal::from_vec(Vector4::create(0.0, 0.0, -1.0, 0.0)));

    assert_eq!(actual, expected);
}

#[test]
pub fn plane_jaunty() -> () {
    let s: f64 = 6.0f64.sqrt();

    let ray: Ray = Ray {
        start: Vector4::create(4.0, 3.0, 2.0, 1.0),
        direction: Direction::from_vec(Vector4::create(0.0, 0.0, 1.0, 1.0))
    };

    let plane = StaticPlane::new(
        Vector4::create(0.0, 0.0, 4.0, 0.0),
        Normal::from_vec(Vector4::create(1.0/s, 2.0/s, -1.0/s, 0.0))
    );


    let actual:Impact = plane.intersect(ray).unwrap();
    println!("{:?}", actual);

    let expected: Impact = Impact::create(12.0, Normal::from_vec(Vector4::create(1.0/s, 2.0/s, -1.0/s, 0.0)));

    assert_eq!(actual, expected);
}

#[test]
pub fn sphere_example() -> () {
    let ray: Ray = Ray {
        start: Vector4::create(0.25, 0.0, -4.0, 3.0),
        direction: Direction::from_vec(Vector4::create(0.0, 0.0, 1.0, 1.0))
    };

    let sphere = StaticSphere::new(
        Vector4::create(-0.25, 0.0, 2.0, 0.0),
        1.0
    );

    let actual = sphere.intersect(ray).unwrap();

    let z = -(1.0f64 - (0.5*0.5)).sqrt();
    let expected: Impact = Impact::create(6.0 + z, Normal::from_vec(Vector4::create(0.5, 0.0, z, 0.0)));

    assert_abs_diff_eq!(actual, expected);
}
