use crate::geometry::bounds::Bounds4;
use crate::geometry::vector::Vector4;

#[test]
pub fn contains() -> () {
    let b = Bounds4::new(
        Vector4::create(1.0, 2.0, 3.0, 4.0),
        Vector4::create(2.0, 3.0, 4.0, 5.0),
    );

    assert!(b.contains_point(Vector4::create(1.5, 2.5, 3.5, 4.5))); // Center

    // X bounds
    assert!(b.contains_point(Vector4::create(1.0, 2.5, 3.5, 4.5)));
    assert!(b.contains_point(Vector4::create(2.0, 2.5, 3.5, 4.5)));
    assert!(!b.contains_point(Vector4::create(0.99, 2.5, 3.5, 4.5)));
    assert!(!b.contains_point(Vector4::create(2.01, 2.5, 3.5, 4.5)));

    // Y bounds
    assert!(b.contains_point(Vector4::create(1.5, 2.0, 3.5, 4.5)));
    assert!(b.contains_point(Vector4::create(1.5, 3.0, 3.5, 4.5)));
    assert!(!b.contains_point(Vector4::create(1.5, 1.99, 3.5, 4.5)));
    assert!(!b.contains_point(Vector4::create(1.5, 3.01, 3.5, 4.5)));

    // Z bounds
    assert!(b.contains_point(Vector4::create(1.5, 2.5, 3.0, 4.5)));
    assert!(b.contains_point(Vector4::create(1.5, 2.5, 4.0, 4.5)));
    assert!(!b.contains_point(Vector4::create(1.5, 2.5, 2.99, 4.5)));
    assert!(!b.contains_point(Vector4::create(1.5, 2.5, 4.01, 4.5)));

    // T bounds
    assert!(b.contains_point(Vector4::create(1.5, 2.5, 3.5, 4.0)));
    assert!(b.contains_point(Vector4::create(1.5, 2.5, 3.5, 5.0)));
    assert!(!b.contains_point(Vector4::create(1.5, 2.5, 3.5, 3.99)));
    assert!(!b.contains_point(Vector4::create(1.5, 2.5, 3.5, 5.01)));
}
