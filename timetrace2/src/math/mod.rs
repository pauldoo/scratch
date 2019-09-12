use math::vector::Vector4;

pub mod vector;

#[derive(Clone, Copy, Debug)]
pub enum Dimension {
    X, Y, Z, T
}

pub struct Bounds4 {
    pub min: Vector4,
    pub max: Vector4
}
