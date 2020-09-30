use crate::geometry::vector::Vector4;

#[cfg(test)]
mod tests;

#[derive(Copy, Clone, Debug)]
pub struct Bounds4 {
    min: Vector4,
    max: Vector4,
}

impl Bounds4 {
    fn assert_consistency(&self) -> () {
        assert!(self.min.x() <= self.max.x());
        assert!(self.min.y() <= self.max.y());
        assert!(self.min.z() <= self.max.z());
        assert!(self.min.t() <= self.max.t());
    }

    pub fn new(min: Vector4, max: Vector4) -> Bounds4 {
        let result = Bounds4 { min, max };
        result.assert_consistency();
        return result;
    }

    pub fn min(&self) -> Vector4 {
        return self.min;
    }

    pub fn set_min(&mut self, v: Vector4) -> () {
        self.min = v;
        self.assert_consistency();
    }

    pub fn max(&self) -> Vector4 {
        return self.max;
    }

    pub fn set_max(&mut self, v: Vector4) -> () {
        self.max = v;
        self.assert_consistency();
    }

    pub fn contains_point(&self, p: Vector4) -> bool {
        return self.min.x() <= p.x()
            && self.min.y() <= p.y()
            && self.min.z() <= p.z()
            && self.min.t() <= p.t()
            && p.x() <= self.max.x()
            && p.y() <= self.max.y()
            && p.z() <= self.max.z()
            && p.t() <= self.max.t();
    }

    pub fn contains_bounds(&self, other: Bounds4) -> bool {
        return self.contains_point(other.min()) && self.contains_point(other.max());
    }

    pub fn closest_point_to(&self, p: Vector4) -> Vector4 {
        return Vector4::create(
            p.x().max(self.min.x()).min(self.max.x()),
            p.y().max(self.min.y()).min(self.max.y()),
            p.z().max(self.min.z()).min(self.max.z()),
            p.t().max(self.min.t()).min(self.max.t()),
        );
    }

    pub fn farthest_point_from(&self, p: Vector4) -> Vector4 {
        let center = (self.min + self.max) / 2.0;

        return Vector4::create(
            if p.x() < center.x() { self.max.x() } else { self.min.x() },
            if p.y() <= center.y() { self.max.y() } else { self.min.y() },
            if p.z() <= center.z() { self.max.z() } else { self.min.z() },
            if p.t() <= center.t() { self.max.t() } else { self.min.t() }
        );
    }
}
