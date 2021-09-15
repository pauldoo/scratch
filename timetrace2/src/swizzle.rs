
#[cfg(test)]
mod tests;

const SCATTER_MASK: u64 =
    0b_01010101_01010101_01010101_01010101_01010101_01010101_01010101_01010101;

const MASK: [u64; 5] = [
    0b_01000100_01000100_01000100_01000100_01000100_01000100_01000100_01000100,
    0b_00110000_00110000_00110000_00110000_00110000_00110000_00110000_00110000,
    0b_00001111_00000000_00001111_00000000_00001111_00000000_00001111_00000000,
    0b_00000000_11111111_00000000_00000000_00000000_11111111_00000000_00000000,
    0b_00000000_00000000_11111111_11111111_00000000_00000000_00000000_00000000
];

const SHIFT: [u8; 5] = [
   1, 2, 4, 8, 16
];

fn scatter(k: u32) -> u64 {
    let mut r: u64 = k as u64;

    for i in (0..5).rev() {
        let mask = MASK[i] >> SHIFT[i];
        r = ((r & mask) << SHIFT[i]) | (r & !mask);
    }

    debug_assert!((r & SCATTER_MASK) == r);
    return r;
}

fn unscatter(k: u64) -> u32 {
    debug_assert!((k & SCATTER_MASK) == k);

    let mut r : u64 = k;
    for i in 0..5 {
        r = ((r & MASK[i]) >> SHIFT[i]) | (r & !MASK[i]);
    }

    debug_assert!((r & 0x00000000_ffffffff) == r);
    return r as u32;
}

pub fn swizzle(x: u32, y: u32) -> u64 {
    return scatter(x) | (scatter(y) << 1);
}

pub fn unswizzle(v: u64) -> (u32, u32) {
    return (
        unscatter(v & SCATTER_MASK),
        unscatter((v >> 1) & SCATTER_MASK)
    );
}
