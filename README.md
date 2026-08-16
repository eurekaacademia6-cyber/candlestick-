# CandleVision Expert 1.0

A completely offline Android candlestick chart reader. No MT5, broker, bridge, API, login, or internet connection is required.

## What it does
- Live CameraX chart capture
- Automatic chart ROI framing
- Visual reconstruction of candidate candles from camera pixels
- Candlestick expert ensemble: body/wick geometry, engulfing-like bodies, hammer/shooting-star style rejection, momentum, compression/expansion, rising/falling closes
- Market-structure proxy: higher/lower closing sequence
- Regime hint: momentum vs reversal/test
- Confidence and chart-quality scores
- On-device adaptive learning from user labels (UP/DOWN)
- No trade execution and no broker connection

## Important
The app is deliberately honest: camera pixels alone cannot provide exact OHLC, spread, order flow, or hidden higher-timeframe data. The reader therefore outputs a directional bias, not a guaranteed next-candle prediction.

For a truly learned vision model, collect labeled screenshots and train a leak-safe image model; the app has an explicit model slot for that future artifact.

## Build
Use Android Studio with JDK 17 or GitHub Actions. CameraX 1.6.1 is used and compileSdk 36 is required by current CameraX metadata. See .github/workflows/main.yml.
