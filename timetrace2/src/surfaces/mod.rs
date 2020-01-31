use crate::math::vector::Vector4;

pub trait Surface {

}

pub struct StaticPlane {

}

impl StaticPlane {
    pub fn new(
        point_on_plane: Vector4,
        normal: Vector4
    ) -> Box<dyn Surface> {
        unimplemented!();
    }
}