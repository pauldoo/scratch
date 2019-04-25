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
use core::borrow::Borrow;
use owning_ref::OwningRef;

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
    file_name: String,
    file_rw: File,
    mmap_rw: MmapMut,
    usage: u64
}

pub struct PhotonMap {
    file_ro: File,
    data: OwningRef<Box<Mmap>, [Node]>,
    header: PhotonMapHeader
}

const HEADER_SIZE_IN_BYTES: u64= size_of::<PhotonMapHeader>() as u64;
const NODE_SIZE_IN_BYTES: u64= size_of::<Node>() as u64;

impl PhotonMapBuilder {

    pub fn create(photon_count: u64, file_name: &str) -> PhotonMapBuilder {
        let file_size_in_bytes : u64 = HEADER_SIZE_IN_BYTES + NODE_SIZE_IN_BYTES * photon_count;
        let file: File = OpenOptions::new()
            .read(true)
            .write(true)
            .create_new(true)
            .open(file_name)
            .unwrap();
        file.set_len(file_size_in_bytes)
            .unwrap();

        let mut mmap = unsafe { MmapOptions::new().map_mut(&file).unwrap() };

        return PhotonMapBuilder {
            capacity: photon_count,
            file_name: file_name.to_string(),
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
        if nodes.len() <= 1 {
            return;
        }

        if nodes.len() == 2 {
            if nodes[0].photon.position.get(axis) > nodes[1].photon.position.get(axis) {
                nodes.swap(0, 1);
            }
            return;
        }

        assert!(0 <= pivotIndex);
        assert!( pivotIndex < (nodes.len() as i64));

        {
            let randomAxisValueInitialIndex = thread_rng().gen_range(0, nodes.len());
            nodes.swap(0, randomAxisValueInitialIndex);
        }
        let randomAxisValue = nodes[0].photon.position.get(axis);
        // Now partition into all items < this value, and those >= to it.
        info!("{}", randomAxisValue);

        let minIndex: i64 = 1;
        let maxIndex: i64 = (nodes.len() as i64) - 1;
        info!("D: {} {}", minIndex, maxIndex);
        let mut lower: i64 = minIndex;
        let mut upper: i64 = maxIndex;


        loop {
            info!("E: {} {}", lower, upper);
            while lower <= maxIndex &&
                nodes[lower as usize].photon.position.get(axis) <= randomAxisValue {
                lower = lower + 1;
            }

            while upper >= minIndex &&
                nodes[upper as usize].photon.position.get(axis) >= randomAxisValue {
                upper = upper - 1;
            }

            info!("F: {} {}", lower, upper);

            if lower > maxIndex {
                break;
            }

            if upper < minIndex {
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

        let randomAxisValueEventualIndex = upper;

        if randomAxisValueEventualIndex > 0 {
            nodes.swap(0, upper as usize);
        }

        {
            for _i in 0..=randomAxisValueEventualIndex {
                assert!(nodes[_i as usize].photon.position.get(axis) <= randomAxisValue);
            }
            // These two loops overlap by one index, namely randomAxisValueEventualIndex
            for _i in randomAxisValueEventualIndex..(nodes.len() as i64) {
                assert!(nodes[_i as usize].photon.position.get(axis) >= randomAxisValue);
            }
        }


        //assert!(false);

        if pivotIndex < randomAxisValueEventualIndex {
            // Recurse down the bottom half
            PhotonMapBuilder::partition_by_axis(
                &mut nodes[..(randomAxisValueEventualIndex as usize)],
                axis,
                pivotIndex);
        } else if pivotIndex > randomAxisValueEventualIndex {
            PhotonMapBuilder::partition_by_axis(
                &mut nodes[((randomAxisValueEventualIndex+1) as usize)..],
                axis,
                pivotIndex - (randomAxisValueEventualIndex+1));
        } else {
            // All done!
            return;
        }
    }

    pub fn finish(mut self) -> PhotonMap {
        assert_eq!(self.usage, self.capacity);
        let header = PhotonMapBuilder::do_sort(self.nodes_slice());
        *(self.header()) = header.unwrap();

        {
            let mmap = self.mmap_rw;
        }
        {
            let file = self.file_rw;
        }

        return PhotonMap::openExistingMap(&self.file_name);
    }
}

impl PhotonMap {
    pub fn openExistingMap(file_name: &str) -> PhotonMap {
        let file: File = OpenOptions::new()
            .read(true)
            .write(false)
            .create_new(false)
            .open(file_name)
            .unwrap();

        let mmap: Mmap = unsafe { MmapOptions::new().map(&file).unwrap() };

        let header: PhotonMapHeader = unsafe { & *((mmap.as_ptr().offset(0)) as *const PhotonMapHeader) } .clone();

        let data = OwningRef::new(Box::new(mmap));
        let data: OwningRef<Box<Mmap>, [Node]> = data.map(|mm| unsafe { slice::from_raw_parts(
            (mm.as_ptr().offset(HEADER_SIZE_IN_BYTES as isize) as *const Node),
            header.capacity as usize
        )} );

        let result = PhotonMap {
            file_ro: file,
            data,
            header
        };

        result.validate();

        return result;
    }


    fn validate(&self) -> () {

    }
}