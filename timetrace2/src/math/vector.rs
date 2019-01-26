extern crate log;

pub fn foobar() {
    info!("hello");
}

pub struct Vector4 {
    v: [f64; 4]
}

impl Vector4 {
    pub fn x(&self) -> f64 {
        self.v[0]
    }

    pub fn zero() -> Vector4 {
        Vector4::create(0.0, 0.0, 0.0, 0.0)
    }

    pub fn create(x: f64, y: f64, z: f64, t: f64) -> Vector4 {
        Vector4 {
            v: [x, y, z, t]
        }
    }
}
