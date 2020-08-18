use crate::geometry::impact::Impact;
use crate::geometry::ray::Ray;
use crate::lights::Light;
use crate::photonmap::builder::PhotonMapBuilder;
use crate::photonmap::PhotonMap;
use crate::scene::Scene;
use crate::surfaces::Surface;
use crate::Config;
use image::ImageBuffer;
use log::info;
use rand::thread_rng;
use std::path::PathBuf;
use crate::geometry::vector::Vector4;
use crate::photon::Photon;
use ordered_float::OrderedFloat;

fn trace_light_ray(ray: Ray, surfaces: &Vec<Box<dyn Surface>>) -> Option<Impact> {
    let raySign = Vector4::from(ray.direction).t().signum();

    surfaces.iter()
        .flat_map(|s| s.intersect(ray))
        .min_by_key(|i| OrderedFloat(i.location.t() * raySign))
}

pub fn create_photon_map(config: &Config, file_path: &PathBuf, scene: &Scene) -> PhotonMap {
    assert_eq!(scene.lights.len(), 1);

    let mut map_builder: PhotonMapBuilder =
        PhotonMapBuilder::create(config.photon_count, file_path.as_path());

    let mut rng = thread_rng();

    let light: &dyn Light = scene.lights.get(0).unwrap().as_ref();

    while map_builder.has_capacity() {
        let ray = light.emit(&mut rng);

        let impact = trace_light_ray(ray, &scene.surfaces);

        if impact.is_some() {
            map_builder.add_photon(Photon {
                position: impact.unwrap().location,
                id: 0
            });
        }
    }

    info!("finishing");
    return map_builder.finish();
}

pub fn do_raytrace(config: &Config, map: &PhotonMap, scene: &Scene) -> () {
    info!("Doing the raytrace");
    for frame in 0..(config.frame_count) {
        info!("Frame: {}", frame);
        let mut img = ImageBuffer::new(config.width as u32, config.height as u32);
        for (x, y, pixel) in img.enumerate_pixels_mut() {
            let r = (0.3 * x as f32) as u8;
            let b = (0.3 * y as f32) as u8;
            *pixel = image::Rgb([r, 0, b]);
        }

        unimplemented!();

        info!("Saving frame");
        let filename = PathBuf::from(format!("frame_{:06}.png", frame));
        let full_frame_path = config.output_directory.join(filename);
        img.save(&full_frame_path).unwrap();
        info!("Frame saved");
    }
    info!("All frames done!")
}
