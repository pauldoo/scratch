use crc::{Crc, Table};

pub fn crc_fast(data: &[u8]) -> u64 {
    crc_fast::crc64_nvme(data)
}
pub fn crc_table1(data: &[u8]) -> u64 {
    const CLASSIC_1: Crc<u64, Table<1>> = Crc::<u64, Table<1>>::new(&crc::CRC_64_NVME);
    CLASSIC_1.checksum(data)
}

pub fn crc_table16(data: &[u8]) -> u64 {
    const CLASSIC_16: Crc<u64, Table<16>> = Crc::<u64, Table<16>>::new(&crc::CRC_64_NVME);
    CLASSIC_16.checksum(data)
}

pub fn twox_xxh3(data: &[u8]) -> u64 {
    twox_hash::XxHash3_64::oneshot(data)
}

pub fn xxhash_xxh3(data: &[u8]) -> u64 {
    xxhash_rust::xxh3::xxh3_64(data)
}


#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn it_works() {
        assert_eq!(crc_fast(b"hello world"), 10171896271030881982);
        assert_eq!(crc_table1(b"hello world"), 10171896271030881982);
        assert_eq!(crc_table16(b"hello world"), 10171896271030881982);

        assert_eq!(twox_xxh3(b"hello world"), 15296390279056496779);
        assert_eq!(xxhash_xxh3(b"hello world"), 15296390279056496779);
    }
}
