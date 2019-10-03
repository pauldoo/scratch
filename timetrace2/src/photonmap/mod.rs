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

            let mut left_bounds = bounds;
            let mut right_bounds = bounds;

            left_bounds.max.set(
                pivot_node.split_direction,
                pivot_node.photon.position.get(pivot_node.split_direction));

            right_bounds.min.set(
                pivot_node.split_direction,
                pivot_node.photon.position.get(pivot_node.split_direction));

            self.check_bounds(left_bounds, begin, pivot_idx);
            self.check_bounds(right_bounds, pivot_idx + 1, end);
        }
    }

    fn photon_count(&self) -> usize {
        self.header.capacity
    }

    fn do_search(&self) -> () {
        unimplemented!();
    }
}