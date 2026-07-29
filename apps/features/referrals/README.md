# Referrals Placeholder

Reserved ownership placeholder for future `:features:referrals`.

Planned ownership:

- Invite code and referral-link creation.
- Android system Share Sheet entry.
- Referral attribution, status, and history.
- Eligibility and qualifying-conversion presentation.
- Referral terms and reward progress.

Planned structure when runtime implementation begins:

```text
features/referrals/
├── navigation/
├── presentation/
│   ├── invite/
│   ├── referral_status/
│   ├── referral_history/
│   └── terms/
├── domain/
│   ├── model/
│   ├── repository/
│   └── usecase/
├── data/
└── res-icons/
    └── drawable/
```

The backend must verify attribution, eligibility, qualifying conversion,
anti-fraud rules, and idempotency. Sharing a link alone must not grant a plan.

`:features:billing` owns subscription entitlement duration and activation.
`:features:rewards` may present the earned benefit, but it does not grant it.
`:features:community` does not own referral attribution or entitlement.

This folder is not wired in `settings.gradle.kts` or `app/build.gradle.kts` yet.
Add the feature module and register `src/main/res-icons` as a resource root when
runtime implementation begins.
