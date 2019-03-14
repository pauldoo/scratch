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
        if nodes.is_empty() {
            return None;
        }

        let mut header: PhotonMapHeader = PhotonMapHeader {
            capacity: nodes.len() as u64,
            min: nodes.first().unwrap().photon.position,
            max: nodes.first().unwrap().photon.position
        };

        if (nodes.len() >= 2) {
            let pivotIndex = (nodes.len() as u64) / 2;

            for np in nodes {
                header.min = Vector4::mins(&header.min, &np.photon.position);
                header.max = Vector4::maxs(&header.max, &np.photon.position);
            }

            let split = max_index(&(header.max - header.min));

            PhotonMapBuilder::partition_by_axis(nodes, split, pivotIndex);


        }

        return Some(header);
    }

    fn partition_by_axis(nodes: &mut [Node], axis: Dimension, pivotIndex: u64) -> () {


        std::mem::swap(&mut(nodes[0]), &mut(nodes[1]));

    }

    pub fn finish(&mut self) -> () {
        assert_eq!(self.usage, self.capacity);
        let header = PhotonMapBuilder::do_sort(self.nodes_slice());
        *(self.header()) = header.unwrap();
    }
}
