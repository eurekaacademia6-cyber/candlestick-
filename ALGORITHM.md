# Candlestick intelligence stack

1. Vision quality gate: reject insufficient frames/candle counts.
2. Candle geometry: body, range, upper wick, lower wick.
3. Pattern evidence: engulfing-like relationships, pin/rejection geometry, large-body momentum.
4. Sequence structure: rising/falling closes and short-term expansion/compression.
5. Regime: momentum vs reversal/test.
6. Expert ensemble score: combines independent signals instead of relying on one pattern.
7. Adaptive model: small on-device logistic learner updates from user-labeled UP/DOWN examples.
8. Confidence is a quality score, not a guaranteed probability.

The most important limitation is the camera itself: a photograph does not expose exact OHLC, instrument, timeframe, spread, volume, or higher-timeframe context unless those are visibly present. The app therefore refuses to overclaim certainty.
