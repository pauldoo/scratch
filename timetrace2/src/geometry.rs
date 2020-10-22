pub mod bounds;
pub mod direction;
pub mod impact;
pub mod ray;
pub mod vector;
pub mod normal;

#[derive(Copy, Clone, Debug)]
pub enum Dimension {
    X,
    Y,
    Z,
    T,
}
