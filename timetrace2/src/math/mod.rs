use math::vector::Vector4;

pub mod vector;

#[derive(Clone, Copy, Debug)]
pub enum Dimension {
    X, Y, Z, T
}

#[derive(Clone, Copy, Debug)]
pub struct Bounds4 {
    pub min: Vector4,
    pub max: Vector4
}

impl Bounds4 {
    pub fn contains(&self, p: Vector4) -> bool {
        return
            self.min.x() <= p.x() &&
            self.min.y() <= p.y() &&
            self.min.z() <= p.z() &&
            self.min.t() <= p.t() &&
            p.x() <= self.max.x() &&
            p.y() <= self.max.y() &&
            p.z() <= self.max.z() &&
            p.t() <= self.max.t();
    }
}