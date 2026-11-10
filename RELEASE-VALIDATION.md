# Release validation — 1.1.2-dev.8

Compiled the bundle and applied all 25 patches to original Boost 1.14.3: register-safe hooks, native mappings and runtime references passed, along with wrapper regressions and 30 host thread-boundary checks. Applied together with Patcheddit 2.1.0 in both orders.

Measured on a phone during development, on mobile data: the home feed finished loading 1.1 s after launch, down from 5.7 s before the loading work.

## Known gaps

- This beta was tested on a phone with matching builds for its flair, image preview, search page, bottom bar, tab and inbox changes. Those phone builds were patched together with Patcheddit 2.1.0, so the feed crash fix also ran on a device. The feed-end loader space was checked by the bundle's own checks only.
- A saved login was seen restoring on a device, but the background login that follows it was not confirmed. If it does not run, account details, the inbox count and moderator status can be missing, and a saved token Reddit rejects is not replaced until it expires. Turning off **Instant login** under Loading settings avoids this.
- If Reddit rejects a saved token, the first load can fail until the background login replaces it.
