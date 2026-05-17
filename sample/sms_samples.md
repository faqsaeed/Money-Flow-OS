# Sample SMS dataset (for parser + dashboard)

These examples are synthetic but modeled after common JazzCash/EasyPaisa/bank notification patterns.

Trusted sender example (`8558`):

1) `You have received Rs. 2,000 from Ahmed. Bal: Rs 50,000`
2) `You have sent Rs. 1,250 to Ali. Fee Rs 10. Bal: Rs 50,000`

Other channels:

3) `IBFT Transfer of PKR 15,000 to HBL-1234 from YourAccount. Fee PKR 25. Bal PKR 100,000`
4) `RAAST received Rs 5,000 from Bilal. Bal: Rs 30,000`
5) `POS purchase of PKR 2,199 at FOODPANDA. Bal PKR 45,000`
6) `ATM cash withdrawal Rs 10,000. Fee Rs 23. Balance: Rs 12,000`
7) `Mobile Load Rs 200 to 03001234567. Bal: Rs 1,000`
8) `Loan amount PKR 3,000 credited to your wallet. Bal PKR 9,000`

Expected structured outputs are in `sample/parsed_outputs.json`.

