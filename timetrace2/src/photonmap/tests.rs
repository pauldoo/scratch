use super::*;
use rand::prelude::*;
use tempfile::TempDir;
use math::*;
use math::vector::*;


struct Config {
    bounds: Bounds4,
    sample_bounds: Bounds4
}

const CONFIG: Config = Config {
    bounds: Bounds4 {
        min: Vector4::create(-10.0, -10.0, -10.0, -10.0),
        max: Vector4::create(10.0, 10.0, 10.0, 10.0)
    },

    sample_bounds: Bounds4 {
        min: Vector4::create(-20.0, -20.0, -20.0, -20.0),
        max: Vector4::create(20.0, 20.0, 20.0, 20.0)
    }
};

struct TestMap {
    _temp_dir: TempDir, // For RAII
    all_photons: Vec<Photon>, // All photons in the map, for reference
    photon_map: PhotonMap // Constructed PhotonMap
}

fn random_vec_in_bounds<R: Rng + ?Sized>(rng: &mut R, bounds: Bounds4) -> Vector4 {
    return Vector4::create(
        rng.gen_range(bounds.min.x(), bounds.max.x()),
        rng.gen_range(bounds.min.y(), bounds.max.y()),
        rng.gen_range(bounds.min.z(), bounds.max.z()),
        rng.gen_range(bounds.min.t(), bounds.max.t()));
}

fn create_test_map<R: Rng + ?Sized>(rng: &mut R) -> TestMap {
    let batch_count = 100;
    let batch_size = 100;

    let temp_dir = tempfile::tempdir().unwrap();
    let temp_file = temp_dir.path().join("photonmap");
    info!("Using temp file: {}", temp_file.to_str().unwrap());

    let mut builder: PhotonMapBuilder = PhotonMapBuilder::create(batch_count * batch_size, temp_file.as_path());
    let mut all_photons :Vec<Photon> = Vec::new();

    for _i in 0..batch_count {
        let mut batch : Vec<Photon> = Vec::new();
        for _j in 0..batch_size {
            let random_photon = Photon{
                position: random_vec_in_bounds(rng, CONFIG.bounds),
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
    let mut rng = rand::rngs::StdRng::seed_from_u64(42);

    let test_map = create_test_map(&mut rng);

    assert_eq!(test_map.photon_map.photon_count(), test_map.all_photons.len() as u64);

    for _i in 0..100 {
        let random_search_point = random_vec_in_bounds(&mut rng, CONFIG.sample_bounds);

        test_map.photon_map.do_search();
    }
}
