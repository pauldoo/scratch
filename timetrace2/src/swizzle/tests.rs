use rand::{SeedableRng, RngCore};
use crate::swizzle::{swizzle, unswizzle};

#[test]
pub fn zeros() -> () {
    let x: u32 = 0;
    let y: u32 = 0;

    let s: u64 = swizzle(x, y);
    let (xa, ya) = unswizzle(s);

    assert_eq!(xa, x);
    assert_eq!(ya, y);
}

#[test]
pub fn ones() -> () {
    let x: u32 = 0xffffffff;
    let y: u32 = 0xffffffff;

    let s: u64 = swizzle(x, y);
    print!("s: {:#064b}", s);
    let (xa, ya) = unswizzle(s);

    assert_eq!(xa, x);
    assert_eq!(ya, y);
}

#[test]
pub fn only_x_bits() -> () {
    let x: u32 = 0xffffffff;
    let y: u32 = 0;

    let s: u64 = swizzle(x, y);
    let (xa, ya) = unswizzle(s);

    assert_eq!(xa, x);
    assert_eq!(ya, y);
}

#[test]
pub fn only_y_bits() -> () {
    let x: u32 = 0;
    let y: u32 = 0xffffffff;

    let s: u64 = swizzle(x, y);
    let (xa, ya) = unswizzle(s);

    assert_eq!(xa, x);
    assert_eq!(ya, y);
}

#[test]
pub fn fuzz() -> () {
    let mut rng = rand::rngs::StdRng::seed_from_u64(42);

    for _i in 0..1_000_000 {
        let x: u32 = rng.next_u32();
        let y: u32 = rng.next_u32();

        let s: u64 = swizzle(x, y);
        let (xa, ya) = unswizzle(s);

        assert_eq!(xa, x);
        assert_eq!(ya, y);
    }

}