use crate::geometry::bounds::Bounds4;
use crate::geometry::vector::Vector4;
use crate::geometry::Dimension;
use crate::photon::Photon;
use crate::photonmap::{
    Node, PhotonMap, PhotonMapHeader, HEADER_SIZE_IN_BYTES, NODE_SIZE_IN_BYTES,
};
use log::{debug, info};
use memmap::{MmapMut, MmapOptions};
use owning_ref::OwningRefMut;
use rand::{thread_rng, Rng};
use std::fs::{File, OpenOptions};
use std::path::{Path, PathBuf};
use std::slice;

const ENABLE_EXPENSIVE_ASSERTS:bool = false;

pub struct PhotonMapBuilder {
    capacity: usize,
    file_path: PathBuf,
    file_rw: File,
    nodes: OwningRefMut<Box<MmapMut>, [Node]>,
    usage: usize,
    emitted_photon_count: usize
}

impl PhotonMapBuilder {
    pub fn create(photon_count: usize, file_path: &Path) -> PhotonMapBuilder {
        info!(
            "Creating new photon map at: {}",
            file_path.to_str().unwrap()
        );
        let file_size_in_bytes: usize = HEADER_SIZE_IN_BYTES + NODE_SIZE_IN_BYTES * photon_count;
        info!(
            "Size in bytes: header + {} * photons = {} + {} * {} = {}",
            photon_count,
            HEADER_SIZE_IN_BYTES,
            photon_count,
            NODE_SIZE_IN_BYTES,
            file_size_in_bytes
        );
        let file: File = OpenOptions::new()
            .read(true)
            .write(true)
            .create_new(true)
            .open(file_path)
            .unwrap();
        file.set_len(file_size_in_bytes as u64).unwrap();

        let mmap = unsafe { MmapOptions::new().map_mut(&file).unwrap() };
        let nodes = OwningRefMut::new(Box::new(mmap));
        let nodes = nodes.map_mut(|mm| {
            let nodes: &mut [Node] = unsafe {
                slice::from_raw_parts_mut(
                    mm.as_mut_ptr().offset(HEADER_SIZE_IN_BYTES as isize) as *mut Node,
                    photon_count,
                )
            };

            return nodes;
        });

        let result = PhotonMapBuilder {
            capacity: photon_count,
            file_path: file_path.to_path_buf(),
            file_rw: file,
            nodes: nodes,
            usage: 0,
            emitted_photon_count: 0
        };
        assert_eq!(result.nodes.len(), result.capacity);
        return result;
    }

    fn header(&mut self) -> &mut PhotonMapHeader {
        unsafe {
            &mut *((self.nodes.as_owner_mut().as_mut_ptr().offset(0)) as *mut PhotonMapHeader)
        }
    }

    fn nodes_slice(&mut self) -> &mut [Node] {
        return &mut *(self.nodes);
    }

    pub fn add_photon(&mut self, photon: Photon) -> () {
        assert!(self.has_capacity());
        let usage = self.usage;
        {
            let node: &mut Node = &mut self.nodes_slice()[usage];
            node.photon = photon;
        }
        self.usage += 1;
    }

    pub fn increment_emitted_photon_count(&mut self, count: usize) -> () {
        self.emitted_photon_count += count;
    }

    pub fn has_capacity(&self) -> bool {
        return self.usage < self.capacity;
    }

    fn do_sort(nodes: &mut [Node]) -> Bounds4 {
        let mut bounds: Bounds4 = Bounds4::new(nodes[0].photon.position, nodes[0].photon.position);
        for np in &nodes[..] {
            bounds.set_min(Vector4::mins(bounds.min(), np.photon.position));
            bounds.set_max(Vector4::maxs(bounds.max(), np.photon.position));
        }

        info!("do_sort {} bounds measurement done", nodes.len());

        PhotonMapBuilder::do_sort_internal(nodes, bounds);

        return bounds;
    }

    fn do_sort_internal(nodes: &mut [Node], bounds: Bounds4) -> () {
        let info_log = nodes.len() > 10000000;

        debug!("do_sort_internal {}", nodes.len());
        if info_log {
            info!("do_sort_internal {} start", nodes.len());
        }

        if nodes.len() <= 1 {
            return;
        }

        let pivot_index: i64 = (nodes.len() as i64) / 2;

        let split = PhotonMap::split_direction(bounds);

        PhotonMapBuilder::partition_by_axis(&mut nodes[..], split, pivot_index);
        if info_log {
            info!(
                "do_sort_internal {} partition({:?}) done",
                nodes.len(),
                split
            );
        }

        let pivot_point: Vector4 = nodes[pivot_index as usize].photon.position;
        {
            let pivot_value = pivot_point.get(split);
            for _i in 0..=pivot_index {
                assert!(nodes[_i as usize].photon.position.get(split) <= pivot_value);
            }
            for _i in pivot_index..(nodes.len() as i64) {
                assert!(nodes[_i as usize].photon.position.get(split) >= pivot_value);
            }
        }
        if info_log {
            info!("do_sort_internal {} assertions validated", nodes.len());
        }

        PhotonMapBuilder::do_sort_internal(
            &mut nodes[..(pivot_index as usize)],
            PhotonMap::split_left(bounds, split, pivot_point),
        );
        if info_log {
            info!("do_sort_internal {} left recursion done", nodes.len());
        }

        PhotonMapBuilder::do_sort_internal(
            &mut nodes[((pivot_index + 1) as usize)..],
            PhotonMap::split_right(bounds, split, pivot_point),
        );
        if info_log {
            info!("do_sort_internal {} right recursion done", nodes.len());
        }
    }

    /*
        Partition the nodes by the given dimension, so that all those prior to the pivotIndex
        come before all those after the pivotIndex.
    */
    fn partition_by_axis(nodes: &mut [Node], axis: Dimension, pivot_index: i64) -> () {
        debug!(
            "partition_by_axis {} {:?} {}",
            nodes.len(),
            axis,
            pivot_index
        );
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
        assert!(pivot_index < (nodes.len() as i64));

        {
            let random_axis_value_initial_index = thread_rng().gen_range(0, nodes.len());
            nodes.swap(0, random_axis_value_initial_index);
        }
        let random_axis_value = nodes[0].photon.position.get(axis);
        // Now partition into all items < this value, and those >= to it.
        debug!("{}", random_axis_value);

        let min_index: i64 = 1;
        let max_index: i64 = (nodes.len() as i64) - 1;
        let mut lower: i64 = min_index;
        let mut upper: i64 = max_index;

        loop {
            while lower <= max_index
                && nodes[lower as usize].photon.position.get(axis) <= random_axis_value
            {
                lower = lower + 1;
            }

            while upper >= min_index
                && nodes[upper as usize].photon.position.get(axis) >= random_axis_value
            {
                upper = upper - 1;
            }

            if lower > max_index {
                break;
            }

            if upper < min_index {
                break;
            }

            if lower < upper {
                assert!(
                    nodes[lower as usize].photon.position.get(axis)
                        > nodes[upper as usize].photon.position.get(axis),
                    "The photons about to be swapped should be in the wrong order"
                );

                nodes.swap(lower as usize, upper as usize);

                assert!(
                    nodes[lower as usize].photon.position.get(axis) < random_axis_value,
                    "Lower node should now be below the random axis value"
                );
                assert!(
                    nodes[upper as usize].photon.position.get(axis) > random_axis_value,
                    "Upper node should now be above the random axis value"
                );
            } else {
                // slice is now partitioned about the axis.
                break;
            }
        }

        // "lower" is the lowest index which has a photon above the pivot value. (or one-off the end if that does not exist.)
        // "upper" is the highest index which has a photon below the pivot value. (or one-off the end if that does not exist.)
        // When all pivot values are unique, normally "lower = upper + 1".

        // All photons in the [inclusive, exclusive) range should be <= the pivot axis (and safe to swap
        // to index zero).
        assert!(upper < lower);
        if ENABLE_EXPENSIVE_ASSERTS {
            for _i in upper..lower {
                if _i >= min_index {
                    assert!(nodes[_i as usize].photon.position.get(axis) <= random_axis_value);
                }
            }
            for _i in (upper + 1)..=lower {
                if _i <= max_index {
                    assert!(nodes[_i as usize].photon.position.get(axis) >= random_axis_value)
                }
            }
        }

        let random_axis_value_eventual_index = (upper + lower) / 2;
        assert!(random_axis_value_eventual_index >= upper);
        assert!(random_axis_value_eventual_index < lower);

        if random_axis_value_eventual_index > 0 {
            nodes.swap(0, random_axis_value_eventual_index as usize);
        }

        if ENABLE_EXPENSIVE_ASSERTS {
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
                pivot_index,
            );
        } else if pivot_index > random_axis_value_eventual_index {
            PhotonMapBuilder::partition_by_axis(
                &mut nodes[((random_axis_value_eventual_index + 1) as usize)..],
                axis,
                pivot_index - (random_axis_value_eventual_index + 1),
            );
        } else {
            // All done!
            return;
        }
    }

    pub fn finish(mut self) -> PhotonMap {
        info!("Sorting and closing the photon map.");
        assert_eq!(self.usage, self.capacity);
        let bounds = PhotonMapBuilder::do_sort(self.nodes_slice());
        *(self.header()) = PhotonMapHeader {
            emitted_photon_count: self.emitted_photon_count,
            collected_photon_count: self.capacity,
            bounds,
        };

        {
            // Close the memory mapping.
            let _mmap: OwningRefMut<Box<MmapMut>, [Node]> = self.nodes;
        }
        {
            // Close the file.
            let _file = self.file_rw;
        }

        return PhotonMap::open_existing_map(&self.file_path);
    }
}
