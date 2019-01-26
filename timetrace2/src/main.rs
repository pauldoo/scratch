extern crate memmap;
extern crate rand;
extern crate stopwatch;
#[macro_use] extern crate log;
extern crate env_logger;

use memmap::MmapOptions;
use memmap::MmapMut;
use memmap::Mmap;
use std::fs;
use std::fs::File;
use std::fs::OpenOptions;
use rand::prelude::*;
use stopwatch::Stopwatch;
use math::vector::Vector4;
use photon::Photon;
use std::slice;
use std::mem::size_of;

mod math;
mod photon;

fn main() -> std::io::Result<()> {
    env_logger::init();
    info!("boop");

    let mut v4 : math::vector::Vector4 = Vector4::zero();

    info!("{}", v4.x());
    v4 = Vector4::create(1.0, 2.0, 3.0, 4.0);
    info!("{}", v4.x());


    photon::foo();
    math::bar();
    math::vector::foobar();

    let file_name = "test.data";

    {
        let photonCount :u64 = 1 * 1000 * 1000;
        let sizeInBytes :u64 = (size_of::<Photon>() as u64) * photonCount;
        let ops_count :u32 = 10 * 1000;
        info!("file will be {} bytes", sizeInBytes);

        let mut rng = thread_rng();

        let file_rw: File = OpenOptions::new()
            .read(true)
            .write(true)
            .create_new(true)
            .open(file_name)?;
        file_rw.set_len(sizeInBytes)?;

        let mut mmap_rw :MmapMut = unsafe { MmapOptions::new().map_mut(&file_rw)? };


        let ptr: * mut Photon = mmap_rw.as_mut_ptr() as *mut Photon;
        let slice : &mut[Photon] = unsafe {slice::from_raw_parts_mut(ptr, photonCount as usize)};

        //let &mut foob: [Photon] = mmap_rw;
        /*
            let file_ro: File = OpenOptions::new()
              .read(true)
              .open(file_name)?;

            let mmap_ro : Mmap = unsafe { MmapOptions::new().map(&file_ro)? };

            info!("value: {}", mmap_ro[3]);

            let v :u8 = 32;
            mmap_rw[3] = v;

            info!("value: {}", mmap_ro[3]);
        */

        info!("zeroing");
        for _i in 0..(photonCount as usize) {
            let randomPhoton = Photon{
                position: Vector4::zero(),
                id: _i as u32
            };
            slice[_i] = randomPhoton;
        }

        info!("random ops");

        let mut sw = Stopwatch::start_new();

        for _i in 1..=ops_count {
            let idx :usize = rng.gen_range(0, photonCount) as usize;
            slice[idx].id += 1;
        }
        sw.stop();

        // do something that takes some time
        info!("{} ops took {}ms", ops_count, sw.elapsed_ms());
        info!("closing files.");
    }
    info!("files closed, about to delete.");

    fs::remove_file(file_name)?;
    info!("Deleted.");

    Ok(())
    //let mmap = unsafe { MmapOptions::new().map(&file)? };
    //assert_eq!(b"# memmap", &mmap[0..8]);
}
