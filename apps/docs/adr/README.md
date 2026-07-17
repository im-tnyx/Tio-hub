# Architecture Decision Records

This directory records durable architecture decisions for Tio-hub.

ADRs should be short, explicit, and linked from the relevant canonical docs when the decision affects implementation.

## ADR Index

- [ADR-0001: Android Modular Clean Architecture](0001-android-modular-clean-architecture.md)
- [ADR-0002: Profile Settings Progress Ownership](0002-profile-settings-progress-ownership.md)
- [ADR-0003: Type-Safe Navigation And Chrome Policy](0003-type-safe-navigation-and-chrome-policy.md)
- [ADR-0004: Incremental Supabase Setup](0004-incremental-supabase-setup.md)
- [ADR-0005: User-Configurable Navigation And Home Layout](0005-user-configurable-navigation-and-home-layout.md)

## ADR Status Values

- Proposed: decision is drafted but not yet accepted.
- Accepted: decision is active.
- Superseded: decision has been replaced by a later ADR.

## When To Add Or Update An ADR

Add or update an ADR when a change affects:

- Module boundaries.
- Feature ownership.
- Navigation architecture.
- Data ownership.
- Supabase/RLS strategy.
- Shared contracts.
- Wear OS reuse.
- Long-term engineering workflow.
