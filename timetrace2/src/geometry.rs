pub mod bounds;
pub mod direction;
pub mod impact;
pub mod ray;
pub mod vector;

const ERROR_EPSILON: f64 = 1e-10;

#[derive(Copy, Clone, Debug)]
pub enum Dimension {
    X,
    Y,
    Z,
    T,
}
