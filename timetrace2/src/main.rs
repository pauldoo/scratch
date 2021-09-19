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
mod constants;
mod swizzle;

#[macro_use]
extern crate approx;

extern crate num_cpus;

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
        Normal::from_vec(Vector4::create(0.0, 1.0, 0.0, 0.0)),
    );

    let wall = surfaces::StaticPlane::new(
        Vector4::create(-2.0, 0.0, 0.0, 0.0),
        Normal::from_vec(Vector4::create(1.0, 0.0, 0.0, 0.0)),
    );

    let fog = surfaces::Fog::new(1.0);

    let sphere = surfaces::StaticSphere::new(
        Vector4::create(0.0, 0.0, 2.5, 0.0),
        1.0
    );

    return vec![floor, wall, sphere, fog];
}

fn create_lights() -> Vec<Box<dyn lights::Light>> {
    let short = lights::IntervalLight::new(
        Vector4::create(2.0, 2.0, 1.0, 0.0),
        Vector4::create(2.0, 2.0, 1.0, 0.2),
        500.0
    );

    let long = lights::IntervalLight::new(
        Vector4::create(2.0, 2.0, 1.0, 10.0),
        Vector4::create(2.0, 2.0, 1.0, 20.0),
        10000.0
    );

    return vec![short, long];
}

fn create_camera() -> Box<dyn camera::Camera> {
    return camera::StaticCamera::new(
        Vector4::create(0.0, 0.0, 0.0, 0.0),
        Normal::from_vec(Vector4::create(0.0, 0.0, 1.0, 0.0)),
        Normal::from_vec(Vector4::create(0.0, 1.0, 0.0, 0.0))
    );
}

pub struct Config {
    photon_map_size: u64,
    frame_count: u32,
    width: u32,
    height: u32,
    min_t: f64,
    max_t: f64,
    photon_map_sample_size: u32,
    pixel_sample_size: u32,
    reflectiveness: f64,
    output_directory: PathBuf,
}

fn configure_thread_pool() -> () {
    let num_threads = num_cpus::get() * 2;
    info!("Will use {} worker threads.", num_threads);
    rayon::ThreadPoolBuilder::new()
        .num_threads(num_threads)
        .start_handler(|num| {
            info!("Thread #{} starting", num);
        })
        .build_global()
        .unwrap();
}

fn main() -> std::io::Result<()> {
    env_logger::init();
    info!("Starting.");
    configure_thread_pool();

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

    let quick_factor: u64 = if matches.is_present(quick_arg_name) {
        warn!("Quick mode is go!");
        4
    } else {
        1
    };

    let config: Config = Config {
        photon_map_size: 1_000_000_000u64 / quick_factor,
        frame_count: (2000 / quick_factor) as u32,
        width: (640 / quick_factor) as u32,
        height: (360 / quick_factor) as u32,
        min_t: 0.0,
        max_t: 30.0,
        photon_map_sample_size: 50,
        pixel_sample_size: 8,
        reflectiveness: 0.80,
        output_directory: PathBuf::from("./output"),
    };
    if !config.output_directory.exists() {
        fs::create_dir(&config.output_directory).ok();
    }
    assert!(config.output_directory.is_dir(),
        "Output directory should exist"
    );

    let scene: Scene = create_scene();

    let file_path: PathBuf = PathBuf::from("./output/test.data");

    let map: PhotonMap = if matches.is_present(skip_building_photon_map_arg_name) {
        warn!("Not building a new photon map. YOLO.");
        let map = PhotonMap::open_existing_map(&file_path);
        assert_eq!(map.collected_photon_count(), config.photon_map_size as usize);
        map
    } else {
        fs::remove_file(file_path.as_path()).ok();
        create_photon_map(&config, &file_path, &scene)
    };

    do_raytrace(&config, &map, &scene);

    info!("Done.");
    return Ok(());
}
