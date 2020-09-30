use crate::geometry::impact::Impact;
use crate::geometry::ray::Ray;
use crate::lights::Light;
use crate::photonmap::builder::PhotonMapBuilder;
use crate::photonmap::PhotonMap;
use crate::scene::Scene;
use crate::surfaces::Surface;
use crate::Config;
use image::{ImageBuffer, Rgb};
use log::info;
use rand::thread_rng;
use std::path::PathBuf;
use crate::geometry::vector::Vector4;
use crate::photon::Photon;
use ordered_float::OrderedFloat;
use std::cmp::min;
use rand::seq::SliceRandom;
use rayon::prelude::*;

#[cfg(test)]
mod tests;

fn trace_single_ray(ray: Ray, surfaces: &Vec<Box<dyn Surface>>) -> Option<Impact> {
    let raySign = Vector4::from(ray.direction).t().signum();

    surfaces.iter()
        .flat_map(|s| s.intersect(ray))
        .min_by_key(|i| OrderedFloat(i.location.t() * raySign))
}

pub fn create_photon_map(config: &Config, file_path: &PathBuf, scene: &Scene) -> PhotonMap {
    assert_eq!(scene.lights.len(), 1);

    let mut map_builder: PhotonMapBuilder =
        PhotonMapBuilder::create(config.photon_count as usize, file_path.as_path());

    let mut rng = thread_rng();

    let light: &dyn Light = scene.lights.get(0).unwrap().as_ref();

    while map_builder.has_capacity() {
        let ray = light.emit(&mut rng);
        assert_eq!(Vector4::from(ray.direction).t(), 1.0);

        let impact = trace_single_ray(ray, &scene.surfaces);

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

fn query_photon_map_intensity(map: &PhotonMap, hit: &Impact) -> f64 {
    let closest_photons = map.do_search(hit.location, 20);

    let furthest_closest_photon = closest_photons.last().unwrap();

    let distance = (hit.location - furthest_closest_photon.position).l2norm();

    // TODO: do this calculation properly.
    let intensity = (1.0 / (distance.powf(3.0))) / (map.photon_count() as f64);

    return intensity;
}

fn expose(d: f64, b: f64) -> u8 {
    assert!(d >= 0.0);

    return ((1.0 - (-d*b).exp())* 255.0) as u8;
}

pub fn do_raytrace(config: &Config, map: &PhotonMap, scene: &Scene) -> () {
    info!("Doing the raytrace");
    assert!(config.max_t > config.min_t);

    let background_colour: Rgb<u8> = image::Rgb([255u8, 128u8, 128u8]);

    let render_frame = |frame : u32| {
        let t = config.min_t + (frame as f64 / (config.frame_count - 1) as f64) * (config.max_t - config.min_t);
        info!("Frame: {} (t={})", frame, t);
        let half_size: f64 = (min(config.width, config.height) as f64) / 2.0;

        let mut img = ImageBuffer::new(config.width, config.height);
        for (x, y, pixel) in img.enumerate_pixels_mut() {

            let xfrac: f64 = ((x as f64 + 0.5) - (config.width as f64 / 2.0)) / half_size;
            let yfrac: f64 = ((y as f64 + 0.5) - (config.height  as f64 / 2.0)) / half_size;

            let ray: Ray = scene.camera.emit(xfrac, - yfrac, t);
            assert_eq!(Vector4::from(ray.start).t(), t);
            assert_eq!(Vector4::from(ray.direction).t(), -1.0);

            let impact  = trace_single_ray(ray, &scene.surfaces);

            if impact.is_some() {
                let d: f64 = query_photon_map_intensity(map, &impact.unwrap());
                let b = expose(d, config.brightness);
                *pixel = image::Rgb([b, b, b])
            } else {
                *pixel = background_colour;
            }
        }

        info!("Saving frame");
        let filename = PathBuf::from(format!("frame_{:06}.png", frame));
        let full_frame_path = config.output_directory.join(filename);
        img.save(&full_frame_path).unwrap();
        info!("Frame saved");
    };

    let mut frame_numbers :Vec<u32> = (0..(config.frame_count)).collect();
    frame_numbers.shuffle(&mut thread_rng());
    frame_numbers
        .par_iter() // Replace with ".iter()" to disable threading.
        .for_each(|f| render_frame(*f));

    info!("All frames done!")
}
