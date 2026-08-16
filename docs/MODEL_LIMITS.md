# Model limits

The phone camera cannot recover hidden OHLC, spread, order flow, or higher-timeframe information that is not visible in the image.

This version deliberately avoids claiming a fabricated win rate. For a serious learned model, collect chart crops with exact timestamps and future outcomes, split data chronologically, train only on the past, and validate on held-out periods.
