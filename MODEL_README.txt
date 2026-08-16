This release intentionally ships without a pretend pre-trained neural network.
The app uses a deterministic chart-vision + candlestick expert ensemble plus an on-device adaptive model.
A future TFLite/LiteRT model can be placed here after leakage-safe training on labeled chart images.
Do not market the adaptive score as a guaranteed probability; calibrate it against held-out data first.
