use crate::geometry::bounds::Bounds4;
use crate::geometry::vector::Vector4;
use crate::geometry::Dimension;
use crate::photon::Photon;
use log::{debug, info};
use memmap::Mmap;
use memmap::MmapOptions;
use owning_ref::OwningRef;
use std::cmp::Ordering;
use std::collections::BinaryHeap;
use std::fs::File;
use std::fs::OpenOptions;
use std::mem::size_of;
use std::path::Path;
use std::slice;

pub mod builder;

#[cfg(test)]
mod tests;

#[derive(Copy, Clone)]
struct PhotonMapHeader {
    capacity: usize,
    bounds: Bounds4,
}

#[derive(Copy, Clone)]
struct Node {
    photon: Photon,
}

pub struct PhotonMap {
    _file_ro: File,
    _data: OwningRef<Box<Mmap>, [Node]>,
    header: PhotonMapHeader,
}

const HEADER_SIZE_IN_BYTES: usize = size_of::<PhotonMapHeader>();
const NODE_SIZE_IN_BYTES: usize = size_of::<Node>();

struct RangeToSearch {
    min_distance_to_search_point: f64,
    bounds: Bounds4,
    begin: usize,
    end: usize,
}

impl PartialOrd for RangeToSearch {
    fn partial_cmp(&self, other: &Self) -> Option<Ordering> {
        // Binary heap is a max-heap, and we want smaller distance ranges
        // to be top, so this is a reverse ordering.
        // rangeA is "greater than" rangeB if it is closer to the target.
        return self
            .min_distance_to_search_point
            .partial_cmp(&other.min_distance_to_search_point)
            .map(|o| o.reverse());
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

impl Eq for RangeToSearch {}

impl PhotonMap {
    pub fn open_existing_map(file_path: &Path) -> PhotonMap {
        info!(
            "Opening photon map read-only: {}",
            file_path.to_str().unwrap()
        );
        let file: File = OpenOptions::new()
            .read(true)
            .write(false)
            .create_new(false)
            .open(file_path)
            .unwrap();

        let mmap: Mmap = unsafe { MmapOptions::new().map(&file).unwrap() };

        let header: PhotonMapHeader =
            unsafe { &*((mmap.as_ptr().offset(0)) as *const PhotonMapHeader) }.clone();

        let data = OwningRef::new(Box::new(mmap));
        let data: OwningRef<Box<Mmap>, [Node]> = data.map(|mm| unsafe {
            slice::from_raw_parts(
                mm.as_ptr().offset(HEADER_SIZE_IN_BYTES as isize) as *const Node,
                header.capacity as usize,
            )
        });

        let result = PhotonMap {
            _file_ro: file,
            _data: data,
            header,
        };

        result.validate();
        info!("Validation complete");

        return result;
    }

    fn validate(&self) -> () {
        self.check_bounds(self.header.bounds, 0, self.header.capacity as usize);
    }

    fn split_direction(bounds: Bounds4) -> Dimension {
        return Vector4::max_index(bounds.max() - bounds.min());
    }

    fn check_bounds(&self, bounds: Bounds4, begin: usize, end: usize) -> () {
        assert!(begin <= end);
        let length = end - begin;
        if length >= 1 {
            let pivot_idx: usize = begin + (length / 2);
            let pivot_node: Node = (&*(self._data))[pivot_idx];

            debug!("{:?}", bounds);
            debug!("{:?}", pivot_node.photon.position);
            assert!(bounds.contains_point(pivot_node.photon.position));

            let split_direction: Dimension = PhotonMap::split_direction(bounds);

            let left_bounds =
                PhotonMap::split_left(bounds, split_direction, pivot_node.photon.position);
            let right_bounds =
                PhotonMap::split_right(bounds, split_direction, pivot_node.photon.position);
            assert!(bounds.contains_bounds(left_bounds));
            assert!(bounds.contains_bounds(right_bounds));

            self.check_bounds(left_bounds, begin, pivot_idx);
            self.check_bounds(right_bounds, pivot_idx + 1, end);
        }
    }

    pub fn photon_count(&self) -> usize {
        self.header.capacity
    }

    pub fn do_search(&self, search_point: Vector4, result_size_limit: usize) -> Vec<Photon> {
        let nodes: &[Node] = &*self._data;

        let enqueue_single = |idx: usize, queue: &mut BinaryHeap<RangeToSearch>| -> () {
            let node_position = nodes[idx].photon.position;
            let distance = (node_position - search_point).l2norm();

            let range = RangeToSearch {
                min_distance_to_search_point: distance,
                bounds: Bounds4::new(node_position, node_position),
                begin: idx,
                end: idx + 1,
            };

            queue.push(range);
        };

        let enqueue_fn = |begin: usize,
                          end: usize,
                          bounds: Bounds4,
                          queue: &mut BinaryHeap<RangeToSearch>|
         -> () {
            assert!(begin <= end);
            if begin == end {
                return;
            }

            if (end - begin) == 1 {
                enqueue_single(begin, queue);
                return;
            }

            let closest_point = bounds.closest_point_to(search_point);
            let distance = (closest_point - search_point).l2norm();

            let range = RangeToSearch {
                min_distance_to_search_point: distance,
                bounds,
                begin,
                end,
            };

            queue.push(range);
        };

        let mut queue: BinaryHeap<RangeToSearch> = BinaryHeap::new();
        enqueue_fn(0, self.header.capacity, self.header.bounds, &mut queue);

        let mut result: Vec<Photon> = Vec::with_capacity(result_size_limit);

        while result.len() < result_size_limit && !queue.is_empty() {
            let next = queue.pop().unwrap();
            assert!(next.begin <= next.end);
            let length = next.end - next.begin;

            if length == 1 {
                // single photon
                let photon = &nodes[next.begin].photon;
                assert!(next.bounds.contains_point(photon.position));
                result.push(photon.clone());
            } else {
                let pivot_idx = next.begin + (length / 2);
                let pivot_position = nodes[pivot_idx].photon.position;
                assert!(next.bounds.contains_point(pivot_position));

                let split_direction = PhotonMap::split_direction(next.bounds);
                let left_bounds =
                    PhotonMap::split_left(next.bounds, split_direction, pivot_position);
                let right_bounds =
                    PhotonMap::split_right(next.bounds, split_direction, pivot_position);

                enqueue_fn(next.begin, pivot_idx, left_bounds, &mut queue);
                enqueue_single(pivot_idx, &mut queue);
                enqueue_fn(pivot_idx + 1, next.end, right_bounds, &mut queue);
            }
        }

        return result;
    }

    fn split_left(bounds: Bounds4, split: Dimension, pivot: Vector4) -> Bounds4 {
        return Bounds4::new(bounds.min(), bounds.max().with(split, pivot.get(split)));
    }

    fn split_right(bounds: Bounds4, split: Dimension, pivot: Vector4) -> Bounds4 {
        return Bounds4::new(bounds.min().with(split, pivot.get(split)), bounds.max());
    }
}
