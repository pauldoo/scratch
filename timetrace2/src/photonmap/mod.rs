use std::fs::File;
use std::fs::OpenOptions;
use memmap::MmapOptions;
use memmap::MmapMut;
use memmap::Mmap;
use photon::Photon;
use std::slice;
use std::mem::size_of;
use math::vector::Vector4;

#[derive(Clone)]
pub struct PhotonMapHeader {
    capacity: u64,
    min: Vector4,
    max: Vector4
}

pub struct PhotonMapBuilder {
    capacity: u64,
    file_rw: File,
    mmap_rw: MmapMut,
    usage: u64
}

const HEADER_SIZE_IN_BYTES: u64= size_of::<PhotonMapHeader>() as u64;
const PHOTON_SIZE_IN_BYTES: u64= size_of::<Photon>() as u64;

impl PhotonMapBuilder {

    pub fn create(photon_count: u64, file_name: &str) -> std::io::Result<PhotonMapBuilder> {
        let file_size_in_bytes : u64 = HEADER_SIZE_IN_BYTES + PHOTON_SIZE_IN_BYTES * photon_count;
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

    fn photons_slice(&mut self) -> &mut[Photon] {
        unsafe { slice::from_raw_parts_mut(
            (self.mmap_rw.as_mut_ptr().offset(HEADER_SIZE_IN_BYTES as isize) as *mut Photon),
            self.capacity as usize
        )}
    }

    pub fn add_photons(&mut self, new_photons: &[Photon]) -> () {
        let mut capacity = self.capacity;
        let mut usage = self.usage;
        {
            let ptr: &mut [Photon] = self.photons_slice();

            for np in new_photons {
                assert!(usage < capacity);
                ptr[usage as usize] = np.clone();
                usage += 1;
            }
        }

        self.usage = usage;
    }

    pub fn finish(&mut self) -> () {
        assert!(self.usage == self.capacity);

        let mut header: PhotonMapHeader = PhotonMapHeader {
            capacity: self.capacity,
            min: Vector4::zero(),
            max: Vector4::zero()
        };

        {
            let ptr: &mut [Photon] = self.photons_slice();

            header.min = ptr[0].position.clone();
            header.max = ptr[0].position.clone();

            for p in ptr {
                header.min = Vector4::mins(&header.min, &p.position);
                header.max = Vector4::maxs(&header.max, &p.position);
            }
        }

        *(self.header()) = header;
    }
}
