#[cfg(test)]
use super::*;
use rand::prelude::*;
use tempfile::TempDir;

struct TestMap {
    _temp_dir: TempDir, // For RAII
    all_photons: Vec<Photon>, // All photons in the map, for reference
    photon_map: PhotonMap // Constructed PhotonMap
}

fn create_test_map() -> TestMap {
    let batch_count = 100;
    let batch_size = 100;

    let temp_dir = tempfile::tempdir().unwrap();
    let temp_file = temp_dir.path().join("photonmap");
    info!("Using temp file: {}", temp_file.to_str().unwrap());

    let mut builder: PhotonMapBuilder = PhotonMapBuilder::create(batch_count * batch_size, temp_file.as_path());
    let mut all_photons :Vec<Photon> = Vec::new();
    let mut rng = rand::ChaChaRng::seed_from_u64(42);

    for _i in 0..batch_count {
        let mut batch : Vec<Photon> = Vec::new();
        for _j in 0..batch_size {
            let random_photon = Photon{
                position: Vector4::create(
                    rng.gen_range(-10.0, 100.0),
                    rng.gen_range(-10.0, 100.0),
                    rng.gen_range(-10.0, 100.0),
                    rng.gen_range(-10.0, 100.0)),
                id: ((_i * batch_size) + _j) as u32
            };
            batch.push(random_photon);
        }
        builder.add_photons(&batch);
        all_photons.extend(batch);
    }

    let photon_map = builder.finish();

    return TestMap {
        _temp_dir: temp_dir,
        all_photons,
        photon_map
    };
}

#[test]
pub fn photon_map_has_expected_photon_count() {
    let test_map = create_test_map();

    assert_eq!(test_map.photon_map.photon_count(), test_map.all_photons.len() as u64);
}
