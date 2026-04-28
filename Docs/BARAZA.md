# BARAZA: Public Accountability Module
This document describes the BARAZA module for government commitment tracking.

## Lifecycle of a Commitment
1. **Capture**: A public promise is recorded (from speech, news, or session).
2. **Assignment**: The promise is linked to a `government_agency` and `affected_ward`.
3. **Evidence**: Citizens or agencies upload evidence (photos/docs) of progress.
4. **Verification**: Peer-to-peer or admin verification of the evidence.
5. **Scorecard**: Real-time fulfillment rates calculated in the `agency_scorecards` view.

## Status FSM
Official states and transitions are defined in `shared/constants/commitment-status.ts`.
- `PROPOSED`
- `VERIFIED_CLAIM`
- `IN_PROGRESS`
- `STALLED`
- `COMPLETED`
- `VERIFIED_RESOLUTION`

---
*Reference: [JUKWA_Technical_Build_Bible.md](Start Build/JUKWA_Technical_Build_Bible.md)*
