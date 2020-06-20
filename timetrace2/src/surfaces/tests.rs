use crate::surfaces::StaticPlane;
use crate::geometry::vector::Vector4;

fn flat_plane() -> StaticPlane {
    StaticPlane {
        point_on_plane: Vector4::create(0.0, 0.0, 4.0, 0.0),
        normal: Vector4::create(0.0, 0.0, 4.0, 0.0)
    }
}


#[test]
pub fn plane_straight_hit_front() -> () {
    unimplemented!()
}

#[test]
pub fn plane_straight_hit_back() -> () {
    unimplemented!()
}

#[test]
pub fn plane_going_away_front() -> () {
    unimplemented!()
}

#[test]
pub fn plane_going_away_back() -> () {
    unimplemented!()
}


#[test]
pub fn plane_parallel_ray() -> () {
    unimplemented!()
}

#[test]
pub fn plane_jaunty() -> () {
    unimplemented!()
}
