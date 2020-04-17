use crate::math::vector::Vector4;

pub trait Surface {

}

pub struct StaticPlane {
    point_on_plane: Vector4,
    normal: Vector4
}

impl StaticPlane {
    pub fn new(
        point_on_plane: Vector4,
        normal: Vector4
    ) -> Box<dyn Surface> {
        return Box::new(StaticPlane {
            point_on_plane,
            normal
        });
    }
}

impl Surface for StaticPlane {

}
