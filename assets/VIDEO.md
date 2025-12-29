Recorded video using obs and lossless recording quality (maybe unnecessary but
better compression and so) Used lossless cut to cut it and then converted to av1
with

```bash
ffmpeg -i input.avi -vf "scale=960:-2,fps=24" -c:v
libsvtav1 -crf 40 -b:v 0 -preset 6 -movflags +faststart output.mp4
```
