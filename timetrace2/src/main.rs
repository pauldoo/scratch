extern crate memmap;
extern crate rand;
extern crate stopwatch;
#[macro_use] extern crate log;
extern crate env_logger;
extern crate core;
extern crate owning_ref;
#[cfg(test)]
extern crate tempfile;

use std::fs;
use rand::prelude::*;
use math::vector::Vector4;
use photon::Photon;
use photonmap::{PhotonMapBuilder, PhotonMap};
use std::path::PathBuf;

mod math;
mod photon;
mod photonmap;

fn main() -> std::io::Result<()> {
    env_logger::init();
    info!("boop");


    let file_path: PathBuf = PathBuf::from("./test.data");
    fs::remove_file(file_path.as_path()).ok();


    {
        let batch_size:u64 = 10;
        let batch_count:u64 = 10;
        let photon_count :u64 = batch_size * batch_count;

        let mut map_builder: PhotonMapBuilder = PhotonMapBuilder::create(photon_count, file_path.as_path());

        let mut rng = thread_rng();

        for _i in 0..batch_count {
            let mut vec:Vec<Photon> = Vec::new();
            for _j in 0..batch_size {
                let random_photon = Photon{
                    position: Vector4::create(
                        rng.gen_range(-10.0, 100.0),
                        rng.gen_range(-10.0, 100.0),
                        rng.gen_range(-10.0, 100.0),
                        rng.gen_range(-10.0, 100.0)),
                    id: ((_i * batch_size) + _j) as u32
                };
                vec.push(random_photon);
            }
            map_builder.add_photons(&vec);
        }

        info!("finishing");
        let _map: PhotonMap = map_builder.finish();

        info!("closing files.");
    }
    info!("files closed, about to delete.");

    //fs::remove_file(file_name)?;
    //info!("Deleted.");

    Ok(())
    //let mmap = unsafe { MmapOptions::new().map(&file)? };
    //assert_eq!(b"# memmap", &mmap[0..8]);
}
