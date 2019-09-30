use std::fs::File;
use std::fs::OpenOptions;
use memmap::MmapOptions;
use memmap::MmapMut;
use memmap::Mmap;
use photon::Photon;
use std::slice;
use std::mem::size_of;
use math::vector::{Vector4, max_index};
use math::{Dimension, Bounds4};
use rand::{thread_rng, Rng};
use owning_ref::OwningRef;
use std::path::{Path, PathBuf};

#[cfg(test)]
mod tests;

#[derive(Clone)]
struct PhotonMapHeader {
    capacity: u64,
    bounds: Bounds4
}

#[derive(Clone)]
struct Node {
    split_direction: Dimension,
    photon: Photon
}

pub struct PhotonMapBuilder {
    capacity: u64,
    file_path: PathBuf,
    file_rw: File,
    mmap_rw: MmapMut,
    usage: u64
}

pub struct PhotonMap {
    _file_ro: File,
    _data: OwningRef<Box<Mmap>, [Node]>,
    header: PhotonMapHeader
}

const HEADER_SIZE_IN_BYTES: u64= size_of::<PhotonMapHeader>() as u64;
const NODE_SIZE_IN_BYTES: u64= size_of::<Node>() as u64;

impl PhotonMapBuilder {

    pub fn create(photon_count: u64, file_path: &Path) -> PhotonMapBuilder {
        info!("Creating new photon map at: {}", file_path.to_str().unwrap());
        let file_size_in_bytes : u64 = HEADER_SIZE_IN_BYTES + NODE_SIZE_IN_BYTES * photon_count;
        let file: File = OpenOptions::new()
            .read(true)
            .write(true)
            .create_new(true)
            .open(file_path)
            .unwrap();
        file.set_len(file_size_in_bytes)
            .unwrap();

        let mmap = unsafe { MmapOptions::new().map_mut(&file).unwrap() };

        return PhotonMapBuilder {
            capacity: photon_count,
            file_path: file_path.to_path_buf(),
            file_rw: file,
            mmap_rw: mmap,
            usage: 0
        };
    }

    fn header(&mut self) -> &mut PhotonMapHeader {
        unsafe { &mut *((self.mmap_rw.as_mut_ptr().offset(0)) as *mut PhotonMapHeader) }
    }

    fn nodes_slice(&mut self) -> &mut[Node] {
        let result = unsafe { slice::from_raw_parts_mut(
            self.mmap_rw.as_mut_ptr().offset(HEADER_SIZE_IN_BYTES as isize) as *mut Node,
            self.capacity as usize
        )};

        assert_eq!(result.len() as u64, self.capacity);

        return result;
    }

    pub fn add_photons(&mut self, new_photons: &[Photon]) -> () {
        let capacity = self.capacity;
        let mut usage = self.usage;
        {
            let ptr: &mut [Node] = self.nodes_slice();

            for np in new_photons {
                assert!(usage < capacity);
                let node: &mut Node = &mut(ptr[usage as usize]);
                node.split_direction = Dimension::X;
                node.photon = np.clone();
                usage += 1;
            }
        }

        self.usage = usage;
    }

    fn do_sort(nodes: &mut [Node]) -> Option<PhotonMapHeader> {
        debug!("do_sort {}", nodes.len());
        if nodes.is_empty() {
            return None;
        }

        let mut header: PhotonMapHeader = PhotonMapHeader {
            capacity: nodes.len() as u64,
            bounds: Bounds4 {
                min: nodes.first().unwrap().photon.position,
                max: nodes.first().unwrap().photon.position
            }
        };

        if nodes.len() >= 2 {
            let pivot_index: i64 = (nodes.len() as i64) / 2;

            for np in &nodes[..] {
                header.bounds.min = Vector4::mins(header.bounds.min, np.photon.position);
                header.bounds.max = Vector4::maxs(header.bounds.max, np.photon.position);
            }

            let split = max_index(header.bounds.max - header.bounds.min);

            PhotonMapBuilder::partition_by_axis(&mut nodes[..], split, pivot_index);
            nodes[pivot_index as usize].split_direction = split;

            {
                let pivot_value = nodes[pivot_index as usize].photon.position.get(split);
                for _i in 0..=pivot_index {
                    assert!(nodes[_i as usize].photon.position.get(split) <= pivot_value);
                }
                for _i in pivot_index..(nodes.len() as i64) {
                    assert!(nodes[_i as usize].photon.position.get(split) >= pivot_value);
                }
            }

            PhotonMapBuilder::do_sort(&mut nodes[..(pivot_index as usize)]);
            PhotonMapBuilder::do_sort(&mut nodes[((pivot_index +1) as usize)..]);
        }

        return Some(header);
    }

    /*
        Partition the nodes by the given dimension, so that all those prior to the pivotIndex
        come before all those after the pivotIndex.
    */
    fn partition_by_axis(nodes: &mut [Node], axis: Dimension, pivot_index: i64) -> () {
        debug!("partition_by_axis {} {:?} {}", nodes.len(), axis, pivot_index);
        if nodes.len() <= 1 {
            return;
        }

        if nodes.len() == 2 {
            if nodes[0].photon.position.get(axis) > nodes[1].photon.position.get(axis) {
                nodes.swap(0, 1);
            }
            return;
        }

        assert!(0 <= pivot_index);
        assert!( pivot_index < (nodes.len() as i64));

        {
            let random_axis_value_initial_index = thread_rng().gen_range(0, nodes.len());
            nodes.swap(0, random_axis_value_initial_index);
        }
        let random_axis_value = nodes[0].photon.position.get(axis);
        // Now partition into all items < this value, and those >= to it.
        debug!("{}", random_axis_value);

        let min_index: i64 = 1;
        let max_index: i64 = (nodes.len() as i64) - 1;
        debug!("D: {} {}", min_index, max_index);
        let mut lower: i64 = min_index;
        let mut upper: i64 = max_index;


        loop {
            debug!("E: {} {}", lower, upper);
            while lower <= max_index &&
                nodes[lower as usize].photon.position.get(axis) <= random_axis_value {
                lower = lower + 1;
            }

            while upper >= min_index &&
                nodes[upper as usize].photon.position.get(axis) >= random_axis_value {
                upper = upper - 1;
            }

            debug!("F: {} {}", lower, upper);

            if lower > max_index {
                break;
            }

            if upper < min_index {
                break;
            }

            if lower < upper {
                assert!(nodes[lower as usize].photon.position.get(axis) > nodes[upper as usize].photon.position.get(axis),
                        "The photons about to be swapped should be in the wrong order");

                nodes.swap(lower as usize, upper as usize);

                assert!(nodes[lower as usize].photon.position.get(axis) < random_axis_value,
                        "Lower node should now be below the random axis value");
                assert!(nodes[upper as usize].photon.position.get(axis) > random_axis_value,
                        "Upper node should now be above the random axis value");
            } else {
                // slice is now partitioned about the axis.
                break;
            }
        }

        let random_axis_value_eventual_index = upper;

        if random_axis_value_eventual_index > 0 {
            nodes.swap(0, upper as usize);
        }

        {
            for _i in 0..=random_axis_value_eventual_index {
                assert!(nodes[_i as usize].photon.position.get(axis) <= random_axis_value);
            }
            // These two loops overlap by one index, namely random_axis_value_eventual_index
            for _i in random_axis_value_eventual_index..(nodes.len() as i64) {
                assert!(nodes[_i as usize].photon.position.get(axis) >= random_axis_value);
            }
        }


        //assert!(false);

        if pivot_index < random_axis_value_eventual_index {
            // Recurse down the bottom half
            PhotonMapBuilder::partition_by_axis(
                &mut nodes[..(random_axis_value_eventual_index as usize)],
                axis,
                pivot_index);
        } else if pivot_index > random_axis_value_eventual_index {
            PhotonMapBuilder::partition_by_axis(
                &mut nodes[((random_axis_value_eventual_index +1) as usize)..],
                axis,
                pivot_index - (random_axis_value_eventual_index +1));
        } else {
            // All done!
            return;
        }
    }

    pub fn finish(mut self) -> PhotonMap {
        info!("Sorting and closing the photon map.");
        assert_eq!(self.usage, self.capacity);
        let header = PhotonMapBuilder::do_sort(self.nodes_slice());
        *(self.header()) = header.unwrap();

        {
            // Close the memory mapping.
            let _mmap = self.mmap_rw;
        }
        {
            // Close the file.
            let _file = self.file_rw;
        }

        return PhotonMap::open_existing_map(&self.file_path);
    }
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
        self.checkBounds(self.header.bounds, 0, self.header.capacity as usize);
    }

    fn checkBounds(&self, bounds: Bounds4, begin: usize, end: usize) -> () {
        assert!(begin <= end);
        let length = end - begin;
        if length >= 1 {
            let pivotIdx: usize = begin + (length / 2);
            let pivotNode: &Node = &(&*(self._data))[pivotIdx];

            debug!("{:?}", bounds);
            debug!("{:?}", pivotNode.photon.position);
            assert!(bounds.contains(pivotNode.photon.position));

            let mut leftBounds = bounds;
            let mut rightBounds = bounds;

            leftBounds.max.set(
                pivotNode.split_direction,
                pivotNode.photon.position.get(pivotNode.split_direction));

            rightBounds.min.set(
                pivotNode.split_direction,
                pivotNode.photon.position.get(pivotNode.split_direction));

            self.checkBounds(leftBounds, begin, pivotIdx);
            self.checkBounds(rightBounds, pivotIdx + 1, end);
        }
    }

    fn photon_count(&self) -> u64 {
        self.header.capacity
    }

    fn do_search(&self) -> () {
        unimplemented!();
    }
}