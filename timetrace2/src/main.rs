use crate::tracing::{create_photon_map, do_raytrace};
use geometry::vector::Vector4;
use log::{info, warn};
use photonmap::PhotonMap;
use scene::Scene;
use std::fs;
use std::path::PathBuf;
use crate::geometry::normal::Normal;
use clap::{App, Arg, ArgMatches};

mod camera;
mod geometry;
mod lights;
mod photon;
mod photonmap;
mod scene;
mod surfaces;
mod tracing;

fn create_scene() -> scene::Scene {
    return Scene {
        surfaces: create_surfaces(),
        lights: create_lights(),
        camera: create_camera(),
    };
}

fn create_surfaces() -> Vec<Box<dyn surfaces::Surface>> {
    let floor = surfaces::StaticPlane::new(
        Vector4::create(0.0, -1.0, 0.0, 0.0),
        Normal::fromVec(Vector4::create(0.0, 1.0, 0.0, 0.0)),
    );

    let sphere = surfaces::StaticSphere::new(
        Vector4::create(0.0, 0.0, 2.5, 0.0),
        1.0
    );

    return vec![floor, sphere];
}

fn create_lights() -> Vec<Box<dyn lights::Light>> {
    return vec![lights::IntervalLight::new(
        Vector4::create(2.0, 2.0, 1.0, 0.0),
        Vector4::create(2.0, 2.0, 1.0, 0.1),
    )];
}

fn create_camera() -> Box<dyn camera::Camera> {
    return camera::StaticCamera::new(
        Vector4::create(0.0, 0.0, 0.0, 0.0),
        Normal::fromVec(Vector4::create(0.0, 0.0, 1.0, 0.0)),
        Normal::fromVec(Vector4::create(0.0, 1.0, 0.0, 0.0))
    );
}

pub struct Config {
    photon_count: u64,
    frame_count: u32,
    width: u32,
    height: u32,
    min_t: f64,
    max_t: f64,
    brightness: f64,
    output_directory: PathBuf,
}

fn main() -> std::io::Result<()> {
    env_logger::init();
    info!("Starting.");

    let skip_building_photon_map_arg_name = "skip-building-photon-map";
    let quick_arg_name = "quick";

    let matches: ArgMatches = App::new("Timetrace 2")
        .version("1.0")
        .arg(Arg::with_name(skip_building_photon_map_arg_name)
                 .long("skip-building-photon-map")
                 .takes_value(false)
                 .help("If provided, skips building the photon map and we assume the one on disk is good."))

        .arg( Arg::with_name(quick_arg_name)
            .long("quick")
            .takes_value(false)
            .help("If provided, tweaks quality settings to go faster.")
        )
        .get_matches();

    let quick_factor = if matches.is_present(quick_arg_name) {
        warn!("Quick mode is go!");
        4
    } else {
        1
    };

    let config: Config = Config {
        photon_count: 10 * 1000 * 1000,
        frame_count: 100 / quick_factor,
        width: 1280 / quick_factor,
        height: 720 / quick_factor,
        min_t: 0.0,
        max_t: 10.0,
        brightness: 2e3f64,
        output_directory: PathBuf::from("./output"),
    };
    if !config.output_directory.exists() {
        fs::create_dir(&config.output_directory);
    }
    assert!(
        config.output_directory.is_dir(),
        "Output directory should exist"
    );

    let scene: Scene = create_scene();

    let file_path: PathBuf = PathBuf::from("./output/test.data");
    fs::remove_file(file_path.as_path()).ok();

    let map: PhotonMap = if matches.is_present(skip_building_photon_map_arg_name) {
        warn!("Not building a new photon map. YOLO.");
        let map = PhotonMap::open_existing_map(&file_path);
        assert_eq!(map.photon_count(), config.photon_count as usize);
        map
    } else {
        create_photon_map(&config, &file_path, &scene)
    };

    do_raytrace(&config, &map, &scene);

    info!("Done.");
    return Ok(());
}
