use std::fs::File;
use std::fs::OpenOptions;
use memmap::MmapOptions;
use memmap::Mmap;
use photon::Photon;
use std::slice;
use std::mem::size_of;
use math::{Dimension, Bounds4};
use owning_ref::OwningRef;
use std::path::Path;
use math::vector::Vector4;
use std::collections::BinaryHeap;
use std::cmp::Ordering;

pub mod builder;

#[cfg(test)]
mod tests;

#[derive(Clone)]
struct PhotonMapHeader {
    capacity: usize,
    bounds: Bounds4
}

#[derive(Clone)]
struct Node {
    split_direction: Dimension,
    photon: Photon
}


pub struct PhotonMap {
    _file_ro: File,
    _data: OwningRef<Box<Mmap>, [Node]>,
    header: PhotonMapHeader
}

const HEADER_SIZE_IN_BYTES: usize = size_of::<PhotonMapHeader>();
const NODE_SIZE_IN_BYTES: usize = size_of::<Node>();

struct RangeToSearch {
    min_distance_to_search_point: f64,
    bounds: Bounds4,
    begin: u64,
    end: u64
}

impl PartialOrd for RangeToSearch {
    fn partial_cmp(&self, other: &Self) -> Option<Ordering> {
        // Binary heap is a max-heap, and we want smaller distance ranges
        // to be top, so this is a reverse ordering.
        // rangeA is "greater than" rangeB if it is closer to the target.
        return self.min_distance_to_search_point
            .partial_cmp(&other.min_distance_to_search_point)
            .map(|o| {o.reverse()});
    }
}

impl PartialEq for RangeToSearch {
    fn eq(&self, other: &Self) -> bool {
        return self.begin == other.begin && self.end == other.end;
    }
}

impl Ord for RangeToSearch {
    fn cmp(&self, other: &Self) -> Ordering {
        return self.partial_cmp(other).unwrap();
    }
}

impl Eq for RangeToSearch {

}


impl PhotonMap {
    pub fn open_existing_map(file_path: &Path) -> PhotonMap {
        info!("Opening photon map read-only: {}", file_path.to_str().unwrap());
        let file: File = OpenOptions::new()
            .read(true)
            .write(false)
            .create_new(false)
            .open(file_path)
            .unwrap();

        let mmap: Mmap = unsafe { MmapOptions::new().map(&file).unwrap() };

        let header: PhotonMapHeader = unsafe { & *((mmap.as_ptr().offset(0)) as *const PhotonMapHeader) } .clone();

        let data = OwningRef::new(Box::new(mmap));
        let data: OwningRef<Box<Mmap>, [Node]> = data.map(|mm| unsafe { slice::from_raw_parts(
            mm.as_ptr().offset(HEADER_SIZE_IN_BYTES as isize) as *const Node,
            header.capacity as usize
        )} );

        let result = PhotonMap {
            _file_ro: file,
            _data: data,
            header
        };

        result.validate();

        return result;
    }


    fn validate(&self) -> () {
        self.check_bounds(self.header.bounds, 0, self.header.capacity as usize);
    }

    fn check_bounds(&self, bounds: Bounds4, begin: usize, end: usize) -> () {
        assert!(begin <= end);
        let length = end - begin;
        if length >= 1 {
            let pivot_idx: usize = begin + (length / 2);
            let pivot_node: &Node = &(&*(self._data))[pivot_idx];

            debug!("{:?}", bounds);
            debug!("{:?}", pivot_node.photon.position);
            assert!(bounds.contains(pivot_node.photon.position));

            let mut left_bounds = Bounds4::new(
                bounds.min(),
                bounds.max().clone().set(
                    pivot_node.split_direction,
                    pivot_node.photon.position.get(pivot_node.split_direction))
            );
            let mut right_bounds = Bounds4::new(
                bounds.min().clone().set(
                    pivot_node.split_direction,
                    pivot_node.photon.position.get(pivot_node.split_direction)),
                bounds.max());

            self.check_bounds(left_bounds, begin, pivot_idx);
            self.check_bounds(right_bounds, pivot_idx + 1, end);
        }
    }

    fn photon_count(&self) -> usize {
        self.header.capacity
    }


    fn do_search(&self, search_point: Vector4, result_size_limit: usize) -> Vec<Photon> {
        let mut result: Vec<Photon> = Vec::with_capacity(result_size_limit);

        let distance_fn = |bounds: Bounds4| {
            let closest_point = bounds.closest_point_to(search_point);
            return (closest_point - search_point).l2norm();
        };

        let mut queue: BinaryHeap<RangeToSearch> = BinaryHeap::new();

        unimplemented!();

    }
}