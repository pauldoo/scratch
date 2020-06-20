use crate::tracing::{create_photon_map, do_raytrace};
use geometry::vector::Vector4;
use log::info;
use photonmap::PhotonMap;
use scene::Scene;
use std::fs;
use std::path::PathBuf;

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
        Vector4::create(0.0, 1.0, 0.0, 0.0),
    );

    return vec![floor];
}

fn create_lights() -> Vec<Box<dyn lights::Light>> {
    return vec![lights::IntervalLight::new(
        Vector4::create(1.0, 2.0, 0.0, 0.0),
        Vector4::create(1.0, 2.0, 0.0, 1.0),
    )];
}

fn create_camera() -> Box<dyn camera::Camera> {
    return camera::StaticCamera::new(
        Vector4::create(0.0, 0.0, 0.0, 0.0),
        Vector4::create(0.0, 0.0, 1.0, 0.0),
        Vector4::create(0.0, 1.0, 0.0, 0.0),
        0.0,
        100.0,
    );
}

pub struct Config {
    photon_count: usize,
    frame_count: usize,
    width: usize,
    height: usize,
    output_directory: PathBuf,
}

fn main() -> std::io::Result<()> {
    env_logger::init();
    info!("Starting.");

    let config: Config = Config {
        photon_count: 10 * 1000 * 1000,
        frame_count: 100,
        width: 320,
        height: 240,
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

    let file_path: PathBuf = PathBuf::from("./test.data");
    fs::remove_file(file_path.as_path()).ok();

    let map: PhotonMap = create_photon_map(&config, &file_path, &scene);

    do_raytrace(&config, &map, &scene);

    info!("Done.");
    return Ok(());
}
