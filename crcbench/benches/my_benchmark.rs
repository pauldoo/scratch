use std::hint::black_box;

use criterion::{BenchmarkId, Criterion, criterion_group, criterion_main};

fn do_all(c: &mut Criterion) {
    let mut group = c.benchmark_group("crc");

    let mut do_one = |name: &str, func: fn(&[u8]) -> u64, limit| {
        let sizes = (1..=limit).step_by(1);

        for size in sizes {
            group.bench_with_input(BenchmarkId::new(name, size), &size, |b, &size| {
                let mut data: Vec<u8> = vec![0u8; size];
                rand::fill(&mut data);
                let data = data.as_slice();

                b.iter(|| {
                    black_box(func(black_box(data)));
                });
            });
        }

    };

    //do_one( "classic_0", crcbench::crc_classic_0, 16);
    //do_one( "classic_1", crcbench::crc_classic_1, 32);
    
    //do_one( "blended", crcbench::blended);

    do_one("twox_xxh3", crcbench::twox_xxh3, 128);
    do_one("xxhash_xxh3", crcbench::xxhash_xxh3, 128);
    do_one( "crc_table1", crcbench::crc_table1, 32);
    do_one( "crc_table16", crcbench::crc_table16, 128);
    do_one( "crc_fast", crcbench::crc_fast, 128);

    
    //do_one( "blended_wh", crcbench::blended_with_hint);

    group.finish();
}


criterion_group!(benches, do_all);
criterion_main!(benches);
