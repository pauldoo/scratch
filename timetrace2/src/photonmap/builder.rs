use crate::geometry::bounds::Bounds4;
use crate::geometry::vector::Vector4;
use crate::geometry::Dimension;
use crate::photon::Photon;
use crate::photonmap::{
    Node, PhotonMap, PhotonMapHeader, HEADER_SIZE_IN_BYTES, NODE_SIZE_IN_BYTES,
};
use log::info;
use memmap::{MmapMut, MmapOptions};
use owning_ref::OwningRefMut;
use rand::{thread_rng, Rng};
use std::fs::{File, OpenOptions};
use std::path::{Path, PathBuf};
use std::slice;
use crate::constants::ENABLE_EXPENSIVE_ASSERTS;

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
        if ENABLE_EXPENSIVE_ASSERTS {
            let pivot_value = pivot_point.get(split);
            for _i in 0..=pivot_index {
                assert!(nodes[_i as usize].photon.position.get(split) <= pivot_value);
            }
            for _i in pivot_index..(nodes.len() as i64) {
                assert!(nodes[_i as usize].photon.position.get(split) >= pivot_value);
            }
            if info_log {
                info!("do_sort_internal {} assertions validated", nodes.len());
            }
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
    pub(crate)
    fn partition_by_axis(nodes: &mut [Node], axis: Dimension, pivot_index: i64) -> () {
        let info_log = nodes.len() > 10000;

        if info_log {
            info!(
                "partition_by_axis {} {:?} {}",
                nodes.len(),
                axis,
                pivot_index
            );
        }

        assert!(0 <= pivot_index);
        assert!(pivot_index < (nodes.len() as i64));

        if nodes.len() <= 1 {
            return;
        }

        let extract = |nodes : &[Node], i:usize| {
            nodes[i].photon.position.get(axis)
        };

        if nodes.len() == 2 {
            if extract(nodes, 0) > extract(nodes, 1) {
                nodes.swap(0, 1);
            }
            return;
        }


        let random_axis_value = {
            let random_axis_value_initial = thread_rng().gen_range(0, nodes.len());
            extract(nodes, random_axis_value_initial)
        };

        // Now partition into all items < this value, those == to it, and those > than it.
        if info_log {
            info!("random_axis_value: {}", random_axis_value);
        }

        // 1st pass, put all nodes < on the bottom
        let mut lower: usize = 0;
        {
            let mut upper: usize = nodes.len() - 1;
            loop {
                while lower < upper && extract(nodes, lower) < random_axis_value {
                    lower = lower + 1;
                }

                while lower < upper && extract(nodes, upper) >= random_axis_value {
                    upper = upper - 1;
                }

                if upper > lower {
                    assert!(extract(nodes, lower) >= random_axis_value);
                    assert!(extract(nodes, upper) < random_axis_value);
                    nodes.swap(lower, upper);
                } else {
                    break;
                }
            }
        }
        // Now the nodes in positions [0, lower) are < axis value.
        if ENABLE_EXPENSIVE_ASSERTS {
            for i in 0..lower {
                assert!(extract(nodes, i) < random_axis_value);
            }
            for i in lower..(nodes.len()) {
                assert!(extract(nodes, i) >= random_axis_value);
            }
        }

        // 2nd pass, put all nodes > on the top
        let mut upper: usize = nodes.len() - 1;
        {
            let mut lower = lower;
            loop {
                while lower < upper && extract(nodes, upper) > random_axis_value {
                    upper = upper - 1;
                }
                while lower < upper && extract(nodes, lower) <= random_axis_value {
                    lower = lower + 1;
                }

                if upper > lower {
                    assert!(extract(nodes, lower) > random_axis_value);
                    assert!(extract(nodes, upper) <= random_axis_value);
                    nodes.swap(lower, upper);
                } else {
                    break;
                }
            }
        }
        upper = upper + 1;

        // Now the nodes in positions [0, lower) are < axis value.
        // AND nodes in the positions [lower, upper) are == axis value,
        // AND nodes in the positions [upper, max) are > axis value.
        if ENABLE_EXPENSIVE_ASSERTS {
            for i in 0..lower {
                assert!(extract(nodes, i) < random_axis_value);
            }
            for i in lower..upper {
                assert_eq!(extract(nodes, i), random_axis_value);
            }
            for i in upper..(nodes.len()) {
                assert!(extract(nodes, i) > random_axis_value);
            }
        }

        let random_axis_value_eventual_index = ((lower + upper) / 2) as i64;
        assert!(lower as i64 <= random_axis_value_eventual_index);
        assert!(random_axis_value_eventual_index < upper as i64);

        if pivot_index == random_axis_value_eventual_index {
            return;
        } else if pivot_index < random_axis_value_eventual_index {
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
            panic!("Should be unreachable.");
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
