# JUKWA Phase 2 Roadmap
This document outlines the strategic priorities for the next phase of the Jukwa platform development.

## 1. Privacy & Security (Hardened)
- [x] **Citizen Vault Relay**: NGO-aligned infrastructure for anonymous reporting.
- [x] **Tor Routing**: SOCKS5 integration for IP obfuscation.
- [ ] **Onion Service**: Expose the Jukwa API as a `.onion` address for end-to-end encryption and metadata protection.
- [ ] **Zero-Knowledge Proofs (ZKP)**: For anonymous reward claiming (Gamification).

## 2. Intelligence & Automation
- [x] **AI Triage Engine**: Gemini 3.1 Pro integration for incident classification.
- [ ] **Automated Emergency Dispatch**: Logic to automatically alert NARS/Police based on AI severity confidence > 90%.
- [ ] **Predictive Traffic Routing**: Using time-series data to predict congestion before it happens.

## 3. Civic Engagement & BARAZA
- [ ] **M-Pesa Integration**: Payment gateway for tolls, fees, and micro-donations to community projects.
- [ ] **Gamification Engine**: Reward system for verified civic actions (e.g., reporting potholes that get fixed).
- [ ] **Evidence Immutable Ledger**: Storing hashes of commitment evidence on a public ledger for total transparency.

## 4. Operational Infrastructure
- [ ] **TUI Command Center (Hardened)**: Interactive terminal dashboard for TMC operators.
- [ ] **Kong API Gateway**: Transition from Nginx to Kong for advanced rate limiting, plugins, and key management.
- [ ] **High-Availability (HA) DB**: Multi-region PostgreSQL replication for disaster recovery.

---
*Created: 2026-04-28*
