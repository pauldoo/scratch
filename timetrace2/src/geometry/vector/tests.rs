use crate::geometry::vector::{Vector4, dot};
use crate::geometry::Dimension::{T, X, Y, Z};

#[test]
pub fn addition() -> () {
    let actual = Vector4::create(1.0, 2.0, 3.0, 4.0) + Vector4::create(10.0, 20.0, 30.0, 40.0);

    let expected = Vector4::create(11.0, 22.0, 33.0, 44.0);
    assert_eq!(actual, expected);
}

#[test]
pub fn subtraction() -> () {
    let actual = Vector4::create(1.0, 2.0, 3.0, 4.0) - Vector4::create(10.0, 20.0, 30.0, 40.0);

    let expected = Vector4::create(-9.0, -18.0, -27.0, -36.0);
    assert_eq!(actual, expected);
}

#[test]
pub fn l2norm() -> () {
    let actual = Vector4::create(1.0, 2.0, 3.0, 4.0).l2norm();
    let expected = (1.0f64 + 4.0 + 9.0 + 16.0).sqrt();

    assert_eq!(actual, expected);
}

#[test]
pub fn multiply() -> () {
    let actual = Vector4::create(1.0, 2.0, 3.0, 4.0) * 3.5;
    let expected = Vector4::create(3.5, 7.0, 10.5, 14.0);

    assert_eq!(actual, expected);
}

#[test]
pub fn divide() -> () {
    let actual = Vector4::create(1.0, 2.0, 3.0, 4.0) / 2.0;
    let expected = Vector4::create(0.5, 1.0, 1.5, 2.0);

    assert_eq!(actual, expected);
}

#[test]
pub fn fixed_set_and_get() -> () {
    let mut actual = Vector4::zero();

    actual.set_x(5.0).set_y(6.0).set_z(7.0).set_t(8.0);
    assert_eq!(actual, Vector4::create(5.0, 6.0, 7.0, 8.0));
    assert_eq!(actual.x(), 5.0);
    assert_eq!(actual.y(), 6.0);
    assert_eq!(actual.z(), 7.0);
    assert_eq!(actual.t(), 8.0);
}

#[test]
pub fn dynamic_set_and_get() -> () {
    let mut actual = Vector4::zero();

    actual.set(X, 10.0).set(Y, 11.0).set(Z, 12.0).set(T, 13.0);
    assert_eq!(actual, Vector4::create(10.0, 11.0, 12.0, 13.0));
    assert_eq!(actual.get(X), 10.0);
    assert_eq!(actual.get(Y), 11.0);
    assert_eq!(actual.get(Z), 12.0);
    assert_eq!(actual.get(T), 13.0);
}

#[test]
pub fn fixed_with() -> () {
    let orig = Vector4::zero();
    let withed = orig.with_x(5.0).with_y(4.0).with_z(3.0).with_t(2.0);
    assert_eq!(withed, Vector4::create(5.0, 4.0, 3.0, 2.0));
}

#[test]
pub fn dynamic_with() -> () {
    let orig = Vector4::zero();
    let withed = orig.with(X, 5.0).with(Y, 4.0).with(Z, 3.0).with(T, 2.0);
    assert_eq!(withed, Vector4::create(5.0, 4.0, 3.0, 2.0));
}

#[test]
pub fn dot_product() -> () {
    let a= Vector4::create(1.0, 2.0, 3.0, 4.0);
    let b= Vector4::create(5.0, 6.0, 7.0, 8.0);

    assert_eq!( dot(a, b), 5.0 + 12.0 + 21.0 + 32.0);
}
