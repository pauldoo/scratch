use std::fs;
use rand::prelude::*;
use math::vector::Vector4;
use photon::Photon;
use photonmap::PhotonMap;
use std::path::PathBuf;
use photonmap::builder::PhotonMapBuilder;
use scene::Scene;
use log::{info};
use image::{ImageBuffer};

mod math;
mod photon;
mod photonmap;
mod scene;
mod camera;
mod lights;
mod surfaces;

fn create_scene() -> scene::Scene {
    return Scene {
        surfaces: create_surfaces(),
        lights: create_lights(),
        camera: create_camera()
    };
}

fn create_surfaces() -> Vec<Box<dyn surfaces::Surface>> {
    let floor= surfaces::StaticPlane::new(
        Vector4::create(0.0, -1.0, 0.0, 0.0),
        Vector4::create(0.0, 1.0, 0.0, 0.0),
    );

    return vec![floor];
}

fn create_lights() -> Vec<Box<dyn lights::Light>> {
    return vec![lights::IntervalLight::new(
        Vector4::create(1.0, 2.0, 0.0, 0.0),
        Vector4::create(1.0, 2.0, 0.0, 1.0)
    )];
}

fn create_camera() -> Box<dyn camera::Camera> {
    return camera::StaticCamera::new(
        Vector4::create(0.0, 0.0, 0.0, 0.0),
        Vector4::create(0.0, 0.0, 1.0, 0.0),
        Vector4::create(0.0, 1.0, 0.0, 0.0),
        0.0,
        100.0
    );
}

struct Config {
    photon_count: usize,
    frame_count: usize,
    width: usize,
    height: usize,
    output_directory: PathBuf
}

fn create_photon_map(config: &Config, file_path: &PathBuf, scene: &Scene) -> PhotonMap {
    assert_eq!(scene.lights.len(), 1);

    let mut map_builder: PhotonMapBuilder = PhotonMapBuilder::create(config.photon_count, file_path.as_path());

    let mut rng = thread_rng();

    for _i in 0..config.photon_count {
        let random_photon = Photon{
            position: Vector4::create(
                rng.gen_range(-10.0, 100.0),
                rng.gen_range(-10.0, 100.0),
                rng.gen_range(-10.0, 100.0),
                rng.gen_range(-10.0, 100.0)),
            id: _i as u32
        };
        map_builder.add_photon(random_photon);
    }

    info!("finishing");
    return map_builder.finish();
}

fn do_raytrace(config: &Config, map: &PhotonMap, scene: &Scene) -> () {
    info!("Doing the raytrace");
    for frame in 0..(config.frame_count) {
        info!("Frame: {}", frame);
        let mut img = ImageBuffer::new(config.width as u32, config.height as u32);
        for (x, y, pixel) in img.enumerate_pixels_mut() {
            let r = (0.3 * x as f32) as u8;
            let b = (0.3 * y as f32) as u8;
            *pixel = image::Rgb([r, 0, b]);
        }

        info!("Saving frame");
        let filename = PathBuf::from(format!("frame_{:06}.png", frame));
        let full_frame_path = config.output_directory.join(filename);
        img.save(&full_frame_path).unwrap();
        info!("Frame saved");
    }
    info!("All frames done!")
}

fn main() -> std::io::Result<()> {
    env_logger::init();
    info!("Starting.");

    let config: Config = Config {
        photon_count: 10 * 1000 * 1000,
        frame_count: 100,
        width: 320,
        height: 240,
        output_directory: PathBuf::from("./output")
    };
    if !config.output_directory.exists() {
        fs::create_dir(&config.output_directory);
    }
    assert!(config.output_directory.is_dir(), "Output directory should exist");

    let scene: Scene = create_scene();

    let file_path: PathBuf = PathBuf::from("./test.data");
    fs::remove_file(file_path.as_path()).ok();

    let map: PhotonMap = create_photon_map(&config, &file_path, &scene);

    do_raytrace(&config, &map, &scene);

    info!("Done.");
    return Ok(());
}
