# Release validation — 1.0.0

Compiled the bundle and applied all 14 patches to original Boost 1.14.3: register-safe hooks, native mappings and runtime references passed, along with wrapper regressions and 30 host thread-boundary checks. Applied together with Patcheddit 2.1.0-dev.4 in both orders.

Measured on a phone during development, on mobile data through a VPN: the home feed finished loading 1.1 s after launch, down from 5.7 s before the loading work.

## Known gaps

- This exact build was not run on a device. It matches the last tested build, except that logging is removed and the version changed.
- A saved login was seen restoring on a device, but the background login that follows it was not confirmed. If it does not run, account details, the inbox count and moderator status can be missing, and a saved token Reddit rejects is not replaced until it expires.
- If Reddit rejects a saved token, the first load can fail until the background login replaces it.
- On one mobile VPN setup, Boost occasionally showed "Something went wrong". The cause was not found.
