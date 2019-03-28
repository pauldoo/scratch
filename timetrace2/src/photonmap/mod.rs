use std::fs::File;
use std::fs::OpenOptions;
use memmap::MmapOptions;
use memmap::MmapMut;
use memmap::Mmap;
use photon::Photon;
use std::slice;
use std::mem::size_of;
use math::vector::{Vector4, max_index};
use math::Dimension;
use rand::{thread_rng, Rng};

#[derive(Clone)]
struct PhotonMapHeader {
    capacity: u64,
    min: Vector4,
    max: Vector4
}

#[derive(Clone)]
struct Node {
    split_direction: Dimension,
    photon: Photon
}

pub struct PhotonMapBuilder {
    capacity: u64,
    file_rw: File,
    mmap_rw: MmapMut,
    usage: u64
}

const HEADER_SIZE_IN_BYTES: u64= size_of::<PhotonMapHeader>() as u64;
const NODE_SIZE_IN_BYTES: u64= size_of::<Node>() as u64;

impl PhotonMapBuilder {

    pub fn create(photon_count: u64, file_name: &str) -> std::io::Result<PhotonMapBuilder> {
        let file_size_in_bytes : u64 = HEADER_SIZE_IN_BYTES + NODE_SIZE_IN_BYTES * photon_count;
        let file: File = OpenOptions::new()
            .read(true)
            .write(true)
            .create_new(true)
            .open(file_name)?;
        file.set_len(file_size_in_bytes)?;

        let mut mmap = unsafe { MmapOptions::new().map_mut(&file)? };

        Ok(PhotonMapBuilder {
            capacity: photon_count,
            file_rw: file,
            mmap_rw: mmap,
            usage: 0
        })
    }

    fn header(&mut self) -> &mut PhotonMapHeader {
        unsafe { &mut *((self.mmap_rw.as_mut_ptr().offset(0)) as *mut PhotonMapHeader) }
    }

    fn nodes_slice(&mut self) -> &mut[Node] {
        let result = unsafe { slice::from_raw_parts_mut(
            (self.mmap_rw.as_mut_ptr().offset(HEADER_SIZE_IN_BYTES as isize) as *mut Node),
            self.capacity as usize
        )};

        assert_eq!(result.len() as u64, self.capacity);

        return result;
    }

    pub fn add_photons(&mut self, new_photons: &[Photon]) -> () {
        let mut capacity = self.capacity;
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
        info!("do_sort {}", nodes.len());
        if nodes.is_empty() {
            return None;
        }

        let mut header: PhotonMapHeader = PhotonMapHeader {
            capacity: nodes.len() as u64,
            min: nodes.first().unwrap().photon.position,
            max: nodes.first().unwrap().photon.position
        };

        if (nodes.len() >= 2) {
            let pivotIndex: i64 = (nodes.len() as i64) / 2;

            for np in &nodes[..] {
                header.min = Vector4::mins(header.min, np.photon.position);
                header.max = Vector4::maxs(header.max, np.photon.position);
            }

            let split = max_index(header.max - header.min);

            PhotonMapBuilder::partition_by_axis(&mut nodes[..], split, pivotIndex);
            nodes[pivotIndex as usize].split_direction = split;

            {
                let pivotValue = nodes[pivotIndex as usize].photon.position.get(split);
                for _i in 0..=pivotIndex {
                    assert!(nodes[_i as usize].photon.position.get(split) <= pivotValue);
                }
                for _i in pivotIndex..(nodes.len() as i64) {
                    assert!(nodes[_i as usize].photon.position.get(split) >= pivotValue);
                }
            }

            PhotonMapBuilder::do_sort(&mut nodes[..(pivotIndex as usize)]);
            PhotonMapBuilder::do_sort(&mut nodes[((pivotIndex+1) as usize)..]);
        }

        return Some(header);
    }

    /*
        Partition the nodes by the given dimension, so that all those prior to the pivotIndex
        come before all those after the pivotIndex.
    */
    fn partition_by_axis(nodes: &mut [Node], axis: Dimension, pivotIndex: i64) -> () {
        info!("partition_by_axis {} {:?} {}", nodes.len(), axis, pivotIndex);

        assert!(0 <= pivotIndex);
        assert!( pivotIndex < (nodes.len() as i64));

        let randomAxisIndex = thread_rng().gen_range(0, nodes.len());
        nodes.swap(0, randomAxisIndex);
        let randomAxisValue = nodes[0].photon.position.get(axis);
        // Now partition into all items < this value, and those >= to it.
        info!("{}", randomAxisValue);

        let mut lower: i64 = 1;
        let mut upper: i64 = (nodes.len() as i64) - 1;

        loop {
            info!("E: {} {}", lower, upper);
            while lower < (nodes.len() as i64) &&
                nodes[lower as usize].photon.position.get(axis) <= randomAxisValue {
                lower = lower + 1;
            }

            while upper > 0 &&
                nodes[upper as usize].photon.position.get(axis) >= randomAxisValue {
                upper = upper - 1;
            }

            info!("F: {} {}", lower, upper);

            if lower >= (nodes.len() as i64) {
                break;
            }

            if upper <= 0 {
                break;
            }

            if lower < upper {
                assert!(nodes[lower as usize].photon.position.get(axis) > nodes[upper as usize].photon.position.get(axis),
                        "The photons about to be swapped should be in the wrong order");

                nodes.swap(lower as usize, upper as usize);

                assert!(nodes[lower as usize].photon.position.get(axis) < randomAxisValue,
                        "Lower node should now be below the random axis value");
                assert!(nodes[upper as usize].photon.position.get(axis) > randomAxisValue,
                        "Upper node should now be above the random axis value");
            } else {
                // slice is now partitioned about the axis.
                break;
            }
        }


        if (upper > 0) {
            nodes.swap(0, upper as usize);
            upper = upper - 1;
        }
        // Items in [0, upper) are < randomAxisValue
        // Items in [lower, nodes.len()) are > randomAxisValue

        info!("{} {}", upper, lower);
        info!("{}", randomAxisValue);

        for _i in (upper - 2)..(lower+2) {
            info!("  {} {}", _i, nodes[_i as usize].photon.position.get(axis));
        }

        assert!(upper < lower);
        assert!((lower - upper) - 1 >= 1);

        {
            for _i in 0..=upper {
                assert!(nodes[_i as usize].photon.position.get(axis) < randomAxisValue);
            }
            for _i in (upper+1)..lower {
                assert!(nodes[_i as usize].photon.position.get(axis) == randomAxisValue);
            }
            for _i in lower..(nodes.len() as i64) {
                assert!(nodes[_i as usize].photon.position.get(axis) > randomAxisValue);
            }
        }


        //assert!(false);

        if pivotIndex <= upper {
            // Recurse down the bottom half
            PhotonMapBuilder::partition_by_axis(&mut nodes[..((upper+1) as usize)], axis, pivotIndex);
        } else if pivotIndex >= lower {
            PhotonMapBuilder::partition_by_axis(&mut nodes[(lower as usize)..], axis, pivotIndex - lower);
        } else {
            // All done!
            return;
        }
    }

    pub fn finish(&mut self) -> () {
        assert_eq!(self.usage, self.capacity);
        let header = PhotonMapBuilder::do_sort(self.nodes_slice());
        *(self.header()) = header.unwrap();
    }
}
