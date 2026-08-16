# Training the reader to become genuinely data-driven

The app can learn from manually labeled outcomes without an internet connection.

For each analyzed chart:
- capture the feature vector
- wait the chosen horizon
- label UP or DOWN
- update the local adaptive model

For a serious model, build a labeled dataset of chart images with:
- instrument/timeframe
- exact candle coordinates
- market regime
- future outcome at 1, 2, 3 and 5 candles
- MAE/MFE if available
- no look-ahead leakage

Train/validate/test chronologically. Calibrate predicted probabilities on a held-out segment. Only then replace/augment the expert with a TFLite/LiteRT image model.
