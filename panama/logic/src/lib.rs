use std::ptr::{self, NonNull};

#[derive(Debug)]
#[repr(C)]
pub struct Thing {
    x: i32,
    y: i64
}



#[unsafe(no_mangle)]
pub extern "C" fn hello_from_rust(p: Option<NonNull<Thing>>, c: i32) {
    println!("Hello from Rust!");

    let data:&[Thing]  = unsafe {
        std::slice::from_raw_parts(p.unwrap().as_ptr(), usize::try_from(c).unwrap())
    };
    println!("Count: {}", data.len());

    for (idx, t) in data.iter().enumerate() {
        println!("{idx}: {t:?}");
    }
}

