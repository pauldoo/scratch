
## Build and run

### Test

```
RUST_LOG=timetrace2=debug RUST_BACKTRACE=1 cargo test
```

### Debug

```
RUST_LOG=timetrace2=debug RUST_BACKTRACE=1 chrt --idle 0 cargo run
```

### Release

```
RUST_LOG=timetrace2=info RUST_BACKTRACE=1 chrt --idle 0 cargo run --release
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
  -c:v libx264 -profile:v baseline -preset slow -crf 20 \
  -f mp4 \
  render.mp4
```
