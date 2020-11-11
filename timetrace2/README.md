
## Build and run

### Test

```
RUST_LOG=timetrace2=debug RUST_BACKTRACE=1 cargo test
```

### Debug

```
RUST_LOG=timetrace2=debug RUST_BACKTRACE=1 cargo run
```

### Release

```
RUST_LOG=timetrace2=info RUST_BACKTRACE=1 cargo run --release
```

### Profile

From: https://gist.github.com/KodrAus/97c92c07a90b1fdd6853654357fd557a

```
cargo build --release && \
  RUST_LOG=timetrace2=info RUST_BACKTRACE=1 \
  perf record -g --call-graph dwarf --freq 100 \
  ./target/release/timetrace2 --quick
```

### ffmpeg

```
ffmpeg \
  -framerate 10 \
  -i ./output/frame_%06d.png \
  -pix_fmt yuv420p \
  -c:a none \
  -f webm \
  render.webm
```

### Clippy

```
cargo clippy
```

### Single test

```
RUST_LOG=timetrace2=debug RUST_BACKTRACE=1 cargo test --package timetrace2 --bin timetrace2 photonmap::tests::partition_by_axis_test::f -- --exact --nocapture 2>&1 | head -n 50
```
