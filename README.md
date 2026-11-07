<div align="center">

<img src=".github/assets/header.png" alt="Shikify" width="720">

<br>

```
~*~*~*~*~*~  W E L C O M E   T O   M Y   H O M E P A G E  ~*~*~*~*~*~
```

**★彡 Material 3 and Material You patches for Boost for Reddit 1.14.3 彡★**

<sub>`com.rubenmayayo.reddit` • for use with [Morphe](https://github.com/MorpheApp) • inspired by Sync for Reddit</sub>

<br>

<table>
<tr>
<td align="center">🏠<br><a href="#-install-"><b>INSTALL</b></a></td>
<td align="center">⭐<br><a href="#-cool-features-"><b>FEATURES</b></a></td>
<td align="center">🔨<br><a href="#-build-it-yourself-"><b>BUILD</b></a></td>
<td align="center">📜<br><a href="#-credits--licence-"><b>CREDITS</b></a></td>
<td align="center">📰<br><a href="changelog.md"><b>WHAT'S NEW</b></a></td>
</tr>
</table>

<br>

🚧 🚧 🚧 **THIS SITE IS ALWAYS UNDER CONSTRUCTION** 🚧 🚧 🚧

</div>

<br>

<table>
<tr><td>

⚠️ **ATTENTION WEB SURFERS!** ⚠️

**Only tested with Patcheddit.** Shikify is developed and used alongside Patcheddit 2.1.0-dev.4. Other patch sources, or Boost without Patcheddit, are untested.

</td></tr>
</table>

<div align="center">

```
=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
```

</div>

## 💾 INSTALL 💾

### >>> [CLICK HERE to add Shikify to Morphe!!!](https://morphe.software/add-source?github=0xRJDev/shikify) <<<

Open that link on your phone, then patch your own Boost 1.14.3 APK in Morphe Manager. Morphe Manager will offer new Shikify versions as they are released.

**Prefer to do it by hand? No problem, webmaster:**

1. Download `shikify-1.1.0.mpp` from this repository's releases.
2. Import it as a local source in Morphe Manager.
3. Patch your own Boost 1.14.3 APK and pick the patches you want. Each one has a short description, and most can also be switched off later in **Settings → Shikify Settings**.

No Boost APK, Reddit credentials or OAuth client IDs are included. Keep any account or API patches you already use, such as Patcheddit, selected when you patch.

<div align="center">

```
=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
```

</div>

## ⭐ COOL FEATURES ⭐

<table>
<tr><th>WHAT</th><th>WOW!</th></tr>
<tr><td>🎨 <b>Material You throughout</b></td><td>Wallpaper colours for surfaces, side panel, settings, dialogs and empty screens. Upvotes and downvotes keep their Reddit colours.</td></tr>
<tr><td>🔍 <b>Sync-style search</b></td><td>A rebuilt search page with Material 3 styling.</td></tr>
<tr><td>💬 <b>Comments</b></td><td>Header with the subreddit and post title, compact metadata, rounded thread groups and remembered thread position.</td></tr>
<tr><td>👆 <b>Gestures</b></td><td>Distance-based swipe actions on comments and posts, hold to peek at media and predictive back.</td></tr>
<tr><td>🖼️ <b>Media</b></td><td>Image and gallery action pills at the bottom, and a quicker swipe to close.</td></tr>
<tr><td>🔮 <b>Glass</b></td><td>Blurred floating pills that stay subtle and pause their extra effects during motion.</td></tr>
<tr><td>🔤 <b>System font</b></td><td>An optional separate patch that follows your phone's font for posts, comments and names.</td></tr>
<tr><td>⚡ <b>Faster loading</b></td><td>No one-second request queue at launch, valid saved logins reused, feeds and comments loaded in parallel, and HTTP/2.</td></tr>
</table>

**MORE PAGES:** [All 25 patches](docs/PATCHES.md) • [Settings](docs/SETTINGS.md) • [Changelog](changelog.md)

**SYSTEM REQUIREMENTS:** Android 7.0 or later. Wallpaper colours and blur need Android 12 or later.

<div align="center">

```
=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
```

</div>

## 🔨 BUILD IT YOURSELF 🔨

On x86_64 Linux, install Bash, Git, Python 3.12+ and a Java launcher, then run:

```bash
./build.sh
```

The script downloads a pinned JDK 21, the Android SDK and official Morphe 2.1.0 sources into the ignored `.work/` folder, so no GitHub Packages credentials are needed. This may take a while on a 56k modem.

To also apply every patch and validate the result, supply your own original APK:

```bash
BOOST_APK=/absolute/path/to/boost-1.14.3.apk ./build.sh
```

The bundle is written to `output/shikify-1.1.0.mpp`. See [validation](docs/VALIDATION.md) and [releasing](docs/RELEASING.md).

<div align="center">

```
=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
```

</div>

## 📜 CREDITS & LICENCE 📜

GPL-3.0; see [LICENSE](LICENSE). The adapted Material loading renderer keeps its Apache-2.0 notices; see [NOTICE](NOTICE) and the [third-party licence](third-party/licenses/Apache-2.0.txt).

Built with the official [Morphe patches template](https://github.com/MorpheApp/morphe-patches-template).

Shikify is an independent project. It is not affiliated with Morphe, Boost for Reddit, Sync for Reddit or Reddit.

<div align="center">

<br>

```
+------------------------------------------------------+
|  Best viewed in Netscape Navigator 4.0 at 800x600    |
|  Thanks for stopping by! Please sign my guestbook:   |
+------------------------------------------------------+
```

📬 **[SIGN THE GUESTBOOK](https://github.com/0xRJDev/shikify/issues)** 📬

<sub>© 1997 2026 Shikify Web Productions. All rights reserved, except the ones GPL-3.0 gives you.</sub>

</div>
