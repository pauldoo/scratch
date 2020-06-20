use crate::geometry::direction::Direction;
use rand::thread_rng;

#[test]
pub fn random_directions_are_normalized() -> () {
    let mut rng = thread_rng();

    for _ in 0..10 {
        let actual = Direction::random(&mut rng, 1.0);
        assert_eq!(actual.vec.t(), 1.0);
        let l2norm = actual.vec.with_t(0.0).l2norm();
        assert!(0.999 <= l2norm && l2norm <= 1.001);
    }

    for _ in 0..10 {
        let actual = Direction::random(&mut rng, -1.0);
        assert_eq!(actual.vec.t(), -1.0);
        let l2norm = actual.vec.with_t(0.0).l2norm();
        assert!(0.999 <= l2norm && l2norm <= 1.001);
    }
}
