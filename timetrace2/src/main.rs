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

fn main() -> std::io::Result<()> {
  env_logger::init();
  info!("boop");

  let file_name = "test.data";

  {
    let size :u64 = 2 * 1000 * 1000 * 1000;
    let ops_count :u32 = 1000 * 1000;
    info!("file will be {} bytes", size);

    let mut rng = thread_rng();

    let file_rw: File = OpenOptions::new()
      .read(true)
      .write(true)
      .create_new(true)
      .open(file_name)?;
    file_rw.set_len(size)?;

    let mut mmap_rw :MmapMut = unsafe { MmapOptions::new().map_mut(&file_rw)? };

    let file_ro: File = OpenOptions::new()
      .read(true)
      .open(file_name)?;

    let mmap_ro : Mmap = unsafe { MmapOptions::new().map(&file_ro)? };

    info!("value: {}", mmap_ro[3]);

    let v :u8 = 32;
    mmap_rw[3] = v;

    info!("value: {}", mmap_ro[3]);


    let mut sw = Stopwatch::start_new();

    for _i in 1..=ops_count {
        let idx :usize = rng.gen_range(0, size as usize);
        mmap_rw[idx] += 1;
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
