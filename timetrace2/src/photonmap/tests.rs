use super::*;
use crate::geometry::bounds::Bounds4;
use crate::geometry::vector::Vector4;
use crate::photonmap::builder::PhotonMapBuilder;
use rand::prelude::*;
use tempfile::TempDir;
use std::collections::BTreeSet;

struct Config {
    bounds: Bounds4,
    sample_bounds: Bounds4,
    max_search_range: f64
}

fn config() -> Config {
    return Config {
        bounds: Bounds4::new(
            Vector4::create(-10.0, -10.0, -10.0, -10.0),
            Vector4::create(10.0, 10.0, 10.0, 10.0),
        ),

        sample_bounds: Bounds4::new(
            Vector4::create(-20.0, -20.0, -20.0, -20.0),
            Vector4::create(20.0, 20.0, 20.0, 20.0),
        ),

        max_search_range: 30.0
    };
}

struct TestMap {
    _temp_dir: TempDir,       // For RAII
    all_photons: Vec<Photon>, // All photons in the map, for reference
    photon_map: PhotonMap,    // Constructed PhotonMap
}

fn random_vec_in_bounds(rng: &mut impl Rng, bounds: Bounds4) -> Vector4 {
    return Vector4::create(
        rng.gen_range(bounds.min().x(), bounds.max().x()),
        rng.gen_range(bounds.min().y(), bounds.max().y()),
        rng.gen_range(bounds.min().z(), bounds.max().z()),
        rng.gen_range(bounds.min().t(), bounds.max().t()),
    );
}

fn create_test_map(rng: &mut impl Rng) -> TestMap {
    let photon_count = 10000;

    let temp_dir = tempfile::tempdir().unwrap();
    let temp_file = temp_dir.path().join("photonmap");
    info!("Using temp file: {}", temp_file.to_str().unwrap());

    let mut builder: PhotonMapBuilder = PhotonMapBuilder::create(photon_count, temp_file.as_path());
    let mut all_photons: Vec<Photon> = Vec::new();

    for _i in 0..photon_count {
        let random_photon = Photon {
            position: random_vec_in_bounds(rng, config().bounds),
            id: _i as u32,
        };
        builder.add_photon(random_photon);
        all_photons.push(random_photon);
    }

    let photon_map = builder.finish();

    return TestMap {
        _temp_dir: temp_dir,
        all_photons,
        photon_map,
    };
}

fn create_test_map_common_plane(rng: &mut impl Rng) -> TestMap {
    let photon_count = 100000;

    let temp_dir = tempfile::tempdir().unwrap();
    let temp_file = temp_dir.path().join("photonmap");
    info!("Using temp file: {}", temp_file.to_str().unwrap());

    let mut builder: PhotonMapBuilder = PhotonMapBuilder::create(photon_count, temp_file.as_path());
    let mut all_photons: Vec<Photon> = Vec::new();


    let photon_off_plane = Photon {
        position: Vector4::create(0.0, 1000.0, 0.0, 0.0),
        id: 0
    };
    builder.add_photon(photon_off_plane);
    all_photons.push(photon_off_plane);

    for _i in 1..photon_count {
        let random_photon = Photon {
            position: random_vec_in_bounds(rng, config().bounds).with_y(0.0),
            id: _i as u32,
        };
        builder.add_photon(random_photon);
        all_photons.push(random_photon);
    }

    let photon_map = builder.finish();

    return TestMap {
        _temp_dir: temp_dir,
        all_photons,
        photon_map,
    };
}

fn brute_force_search(
    photons: &Vec<Photon>,
    search_point: Vector4,
    result_size_limit: usize,
    max_distance: f64
) -> Vec<Photon> {
    let mut sorted_photons: Vec<Photon> = photons.clone();

    let d = |p : &Photon| -> f64 {
        (p.position - search_point).l2norm()
    };

    let cmp = |a: &Photon, b: &Photon| -> Ordering {
        return d(a).partial_cmp(&d(b)).unwrap();
    };

    sorted_photons.sort_by(cmp);
    return sorted_photons.iter()
        .take(result_size_limit)
        .filter(|p| d(p) < max_distance)
        .map(|p| *p)
        .collect();
}

#[test]
pub fn photon_map_finds_correct_points() {
    let mut rng = rand::rngs::StdRng::seed_from_u64(42);

    let test_map = create_test_map(&mut rng);

    do_random_searches(&test_map, &mut rng);
}

#[test]
pub fn photon_map_works_with_points_on_common_plane() {
    // From a stack overflow..
    // Create lots of points sharing the same Y value, and verify that
    // partitioning about the Y dimension doesn't cause stack overflow.
    // To cause a partition about the Y dimension we need at least one point
    // far off the plane of the others.
    let mut rng = rand::rngs::StdRng::seed_from_u64(42);

    let test_map = create_test_map_common_plane(&mut rng);

    do_random_searches(&test_map, &mut rng);
}

fn do_random_searches(test_map: &TestMap, rng: &mut impl Rng) -> () {
    assert_eq!(
        test_map.photon_map.photon_count(),
        test_map.all_photons.len()
    );

    for _i in 0..100 {
        let random_search_point = random_vec_in_bounds(rng, config().sample_bounds);
        let random_search_limit: f64 = rng.gen_range(0.0, config().max_search_range);

        let expected: Vec<Photon> =
            brute_force_search(&test_map.all_photons, random_search_point, 100, random_search_limit);
        let actual: Vec<Photon> = test_map.photon_map.do_search(random_search_point, 100, random_search_limit);

        assert_eq!(actual, expected);

        let expected_ids = expected.iter().map(|p| p.id).collect::<BTreeSet<_>>();
        let actual_ids = actual.iter().map(|p| p.id).collect::<BTreeSet<_>>();

        assert_eq!(actual_ids, expected_ids);
    }
}
