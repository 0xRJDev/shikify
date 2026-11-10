# Unreleased

Boost for Reddit:
- Fixed the feed crashing when patched together with Patcheddit 2.1.0.
- The end of the feed leaves room for the loading indicator, so it no longer covers the last post.
- Post flairs sit on their own line under the subreddit, user and time, instead of in front of them, after any NSFW or spoiler tag.
- Moving your finger while holding an image to preview it no longer scrolls the list or closes the preview. It closes when you let go.
- The search page is Material 3: Feeds, Subscriptions and results sit in rounded sections, results scroll under a blurred header, the search bar is glass, and the back and filter buttons get glass circles when Glass header buttons is on.
- New Subscriptions button option in Shikify Settings hides the bottom bar's Subscriptions button, and the bar shrinks to fit the other four. The side panel still lists your subscriptions.
- Post flairs use each community's own colours again. Turn off Reddit flair colours in Shikify Settings for the Material You colours instead.

# Version 1.1.2-dev.7

Boost for Reddit:
- While more posts load as you scroll, the loading indicator sits at the bottom of the screen instead of the middle.

# Version 1.1.2-dev.6

Boost for Reddit:
- The floating bottom navigation bar keeps to its own height instead of adding extra space for the system bar.
- Page loading and the remaining circular loaders (subreddit search, albums, video buffering and progress dialogs) use the expressive Material shape animation.
- Glass header buttons stay round on every screen, and the three-dot menu icon sits in the middle of its circle.

# Version 1.1.2-dev.5

Boost for Reddit:
- Swiping a post to vote or reply now also works when you start on its image.
- New Blurred header option in Shikify Settings blurs what scrolls under the top bar, easing off towards its bottom edge.
- New Glass header buttons option puts each top bar icon in a frosted glass circle.
- Glass surfaces use a lighter frost, so they look like clear glass over dark posts.
- The top bar shade now stops at the side menu's edge and dims with the rest of the screen, instead of disappearing.
- Posts and comments behind an open side menu can no longer be swiped.
- New Subreddit colours option turns off the community colour tint on the top bar. The tint is always off with Blurred header.

# Version 1.1.2-dev.4

Boost for Reddit:
- Flair text now sits in the middle of its pill instead of near the bottom.
- Shikify Settings uses Material 3 switches.
- Changing a Loading setting offers to restart Boost so it applies straight away.

# Version 1.1.2-dev.3

Boost for Reddit:
- Videos stop playing as soon as you leave the video viewer, by Back or by swiping.
- Glass sliders dim when glass effects are off.
- The bottom navigation bar no longer flickers between tabs.

# Version 1.1.2-dev.2

Boost for Reddit:
- Patches are now grouped under Appearance, Comments, Media, Navigation and Performance headers in Morphe Manager.

# Version 1.1.2-dev.1

Boost for Reddit:
- Fixed the refresh circle sometimes staying stuck near the status bar.
- Fixed a gallery going black when reopened straight after closing it.

# Version 1.1.1

Boost for Reddit:
- Renamed two patches to Material You and Material search. If they show as unselected in Morphe Manager, select them again.

# Version 1.1.0

Boost for Reddit:
- Split "Material You" into smaller patches, for 25 in total. Pick only what you want in Morphe Manager.
- New patches: HTTP/2 Reddit connection, Parallel content loading, Remove startup request queue, Instant saved login, Smaller first page, Improved online check, Material notices, Material dialogs, Inline flairs, Material search and Sliding screen transitions.
- New Loading page in Shikify Settings, and more switches under Glass & appearance. Switches only appear for patches you selected.

# Version 1.0.0

First public release, for Boost for Reddit 1.14.3.

Boost for Reddit:
- Material You colours across surfaces, the side panel, settings, dialogs and empty screens.
- Material search page, comments header, compact metadata and rounded thread groups.
- Swipe actions, hold to peek at media, predictive back and glass floating pills.
- Optional "Use system font" patch.
- Faster loading: no one-second request queue at launch, saved logins reused, parallel feeds and comments, and HTTP/2.
- Applies alongside Patcheddit 2.1.0-dev.4 in either order.
