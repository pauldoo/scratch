use crate::tracing::expose;

#[test]
pub fn exposure() -> () {
    assert_eq!(expose(0.0), 0u8);
    assert_eq!(expose(0.1), 24u8);
    assert_eq!(expose(1.0), 161u8);
    assert_eq!(expose(2.0), 220u8);
    assert_eq!(expose(10.0), 254u8);
}