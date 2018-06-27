#[macro_use]
extern crate memmap;
extern crate rand;
extern crate stopwatch;
extern crate log;

use memmap::MmapOptions;
use memmap::MmapMut;
use memmap::Mmap;
use std::fs;
use std::fs::File;
use std::fs::OpenOptions;
use rand::prelude::*;
use stopwatch::Stopwatch;

fn main() -> std::io::Result<()> {

  let fileName = "test.data";

{
  let size :u64 = 4 * 1000 * 1000 * 1000;
  let opsCount :u32 = 1000 * 1000;

  
  let mut rng = thread_rng();

  let file_rw: File = OpenOptions::new()
    .read(true)
    .write(true)
    .create_new(true)
    .open(fileName)?;
  file_rw.set_len(size)?;

  let mut mmap_rw :MmapMut = unsafe { MmapOptions::new().map_mut(&file_rw)? };

  let file_ro: File = OpenOptions::new()
    .read(true)
    .open(fileName)?;
  
  let mmap_ro : Mmap = unsafe { MmapOptions::new().map(&file_ro)? };

  println!("value: {}", mmap_ro[3]);

  let v :u8 = 32;
  mmap_rw[3] = v;

  println!("value: {}", mmap_ro[3]);


  let mut sw = Stopwatch::start_new();

  for _i in 1..=opsCount {
      let idx :usize = rng.gen_range(0, size as usize);
      mmap_rw[idx] += 1;
  }
  sw.stop();

  // do something that takes some time
  println!("{} ops took {}ms", opsCount, sw.elapsed_ms());
  println!("closing files.");
}
println!("files closed, about to delete.");

  fs::remove_file(fileName);
println!("Deleted.");

  Ok(())
  //let mmap = unsafe { MmapOptions::new().map(&file)? };
  //assert_eq!(b"# memmap", &mmap[0..8]);
}
