use crate::tracing::{expose, expose_inv, max_distance_to_intensity, intensity_to_max_distance};

#[test]
pub fn exposure() -> () {
    assert_eq!(expose(0.0, 1.0), 0u8);
    assert_eq!(expose(0.1, 1.0), 24u8);
    assert_eq!(expose(1.0, 0.1), 24u8);
    assert_eq!(expose(1.0, 1.0), 161u8);
    assert_eq!(expose(2.0,1.0), 220u8);
    assert_eq!(expose(1.0,2.0), 220u8);
    assert_eq!(expose(10.0, 1.0), 255u8);
}

#[test]
pub fn exposure_inv() -> () {
    let v = expose_inv(40u8, 5.0);
    assert!(v >= 0.0);
    assert_eq!(expose(v, 5.0), 40u8);
}

#[test]
pub fn max_distance_to_intensity_test() -> () {
    assert_eq!(max_distance_to_intensity(1.0 / 8.0, 1.0 / 1024.0), 0.5);
    assert_eq!(max_distance_to_intensity(1.0 / 64.0, 1.0 / 1024.0), 256.0);
    assert_eq!(max_distance_to_intensity(1.0 / 8.0, 1.0 / 2048.0), 0.25);
}

#[test]
pub fn intensity_to_max_distance_test() -> () {
    let v = intensity_to_max_distance(4.0, 1.0 / 1024.0);
    assert!(v >= 0.0);
    let i = max_distance_to_intensity(v, 1.0 / 1024.0);
    assert!(3.99 <= i && i <= 4.01);
}