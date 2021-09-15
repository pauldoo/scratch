use crate::geometry::vector::Vector4;

#[derive(Copy, Clone, Debug, PartialEq)]
pub struct Photon {
    position: [f32; 4],
    pub id: u32,
}

impl Photon {
    pub fn new(position: Vector4, id: u32) -> Photon {
        return Photon {
            position: [
                position.x() as f32,
                position.y() as f32,
                position.z() as f32,
                position.t() as f32],
            id
        };
    }

    pub fn position(&self) -> Vector4 {
        return Vector4::create(
            self.position[0] as f64,
            self.position[1] as f64,
            self.position[2] as f64,
            self.position[3] as f64
        );
    }

}
