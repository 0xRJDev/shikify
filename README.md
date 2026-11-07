# Shikify

Material 3 and Material You patches for **Boost for Reddit 1.14.3** (`com.rubenmayayo.reddit`), for use with [Morphe](https://github.com/MorpheApp). The design takes a lot from Sync for Reddit: wallpaper colours, rounded surfaces, subtle glass and quick loading.

Shikify is an independent project. It is not affiliated with Morphe, Boost for Reddit, Sync for Reddit or Reddit.

## Install

1. Download `shikify-1.0.0.mpp` from this repository's releases.
2. Import it as a local source in Morphe Manager.
3. Patch your own Boost 1.14.3 APK and pick the features you want.

No Boost APK, Reddit credentials or OAuth client IDs are included. Keep any account or API patches you already use, such as Patcheddit, selected when you patch.

## Features

- **Material You throughout:** wallpaper colours for surfaces, side panel, settings, dialogs and empty screens. Upvotes and downvotes keep their Reddit colours.
- **Sync-style search:** a rebuilt search page with Material 3 styling.
- **Comments:** header with the subreddit and post title, compact metadata, rounded thread groups and remembered thread position.
- **Gestures:** distance-based swipe actions on comments and posts, hold to peek at media and predictive back.
- **Media:** image and gallery action pills at the bottom, and a quicker swipe to close.
- **Glass:** blurred floating pills that stay subtle and pause their extra effects during motion.
- **System font:** an optional separate patch that follows your phone's font for posts, comments and names.
- **Faster loading:** no one-second request queue at launch, valid saved logins reused, feeds and comments loaded in parallel, and HTTP/2.

See [all 14 patches](docs/PATCHES.md), [settings](docs/SETTINGS.md) and the [changelog](changelog.md). Android 7.0 is the minimum. Wallpaper colours and blur need Android 12 or later.

## Build

On x86_64 Linux, install Bash, Git, Python 3.12+ and a Java launcher, then run:

```bash
./build.sh
```

The script downloads a pinned JDK 21, the Android SDK and official Morphe 2.1.0 sources into the ignored `.work/` folder, so no GitHub Packages credentials are needed.

To also apply every patch and validate the result, supply your own original APK:

```bash
BOOST_APK=/absolute/path/to/boost-1.14.3.apk ./build.sh
```

The bundle is written to `output/shikify-1.0.0.mpp`. See [validation](docs/VALIDATION.md) and [releasing](docs/RELEASING.md).

## Credits and licence

GPL-3.0; see [LICENSE](LICENSE). The adapted Material loading renderer keeps its Apache-2.0 notices; see [NOTICE](NOTICE) and the [third-party licence](third-party/licenses/Apache-2.0.txt).

Built with the official [Morphe patches template](https://github.com/MorpheApp/morphe-patches-template).
