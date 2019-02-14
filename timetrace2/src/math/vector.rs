extern crate log;

pub fn foobar() {
    info!("hello");
}

#[derive(Clone)]
pub struct Vector4 {
    v: [f64; 4]
}

impl Vector4 {
    pub fn x(&self) -> f64 {
        self.v[0]
    }
    pub fn y(&self) -> f64 {
        self.v[1]
    }
    pub fn z(&self) -> f64 {
        self.v[2]
    }
    pub fn t(&self) -> f64 {
        self.v[3]
    }

    pub fn zero() -> Vector4 {
        Vector4::create(0.0, 0.0, 0.0, 0.0)
    }

    pub fn create(x: f64, y: f64, z: f64, t: f64) -> Vector4 {
        Vector4 {
            v: [x, y, z, t]
        }
    }

    pub fn mins(a: &Vector4, b: &Vector4) -> Vector4 {
        Vector4 {
            v: [
                a.x().min(b.x()),
                a.y().min(b.y()),
                a.z().min(b.z()),
                a.t().min(b.t())
            ]
        }
    }

    pub fn maxs(a: &Vector4, b: &Vector4) -> Vector4 {
        Vector4 {
            v: [
                a.x().max(b.x()),
                a.y().max(b.y()),
                a.z().max(b.z()),
                a.t().max(b.t())
            ]
        }
    }
}
