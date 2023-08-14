# [Wardell Listens](https://listens.wardell.dev)

It's true. I'm Wardell, and listen is what I do.

## What is this?

This is a repository containing the code that updates
the [@listens@wardell.dev Mastodon-compatible account](https://listens.wardell.dev/)
and [@wardell_listens Twitter account](https://twitter.com/wardell_listens).


## How does it work?

It uses my [ListenBrainz listens](https://listenbrainz.org/user/wardellbagby/)
to send out a new track every week, based on what I listened to the past month. It uses a GitHub
action, scheduled to run every Monday morning, to comb through the monthly listens and
find a new track to post.

In order to avoid duplicates, it will also post a new commit to this repo on
the [ignored-tracks branch](https://github.com/wardellbagby/wardell_listens/blob/ignored-tracks/ignored.txt)
, updating a file with the Spotify URL of the song it chose to post.

## What services does it support posting to?

Currently, it supports websites that use the [Micropub API](https://micropub.net/) and Twitter.

## Why does it exist?

Music is meant to be shared, and good music doubly so. Plus, now everyone can see exactly how bad my
taste in music is!

## Can I fork this and use it myself?

You sure can! Fork this project into your own repository and set up these secrets for your new repo.

1. LISTENBRAINZ_USER: The username of the ListenBrainz account you want to fetch
   the listens for.
2. RELATIVE_START_IN_DAYS: (Optional) How far back to fetch listens for in order to suggest a track.
   Defaults to 30.
3. DRY_RUN: (Optional) When true, the track will not be posted to any targets. Defaults to true when
   not running in CI. MUST be set to exactly `true` to be valid; any other value will result in it
   being false.
4. MICROPUB_ENDPOINT: (Optional) The endpoint of your Micropub site.
5. MICROPUB_ACCESS_TOKEN: (Optional) The access token for your Micropub site.
6. TWITTER_CONSUMER_KEY: (Optional) The consumer key (aka API key) of
   the [Twitter application](https://developer.twitter.com/en/docs/authentication/oauth-1-0a/api-key-and-secret)
   you have to create to manage your own Twitter account to post tweets.
7. TWITTER_CONSUMER_SECRET: (Optional) The consumer secret (aka API secret) of your Twitter
   application.
8. TWITTER_ACCESS_TOKEN: (Optional) The OAuth1 access token for your Twitter account. You can get
   this by
   manually [authenticating with Twitter](https://developer.twitter.com/en/docs/authentication/oauth-1-0a/obtaining-user-access-tokens)
   using the OAuth1 PIN-based flow.
9. TWITTER_ACCESS_TOKEN_SECRET: (Optional) Can be retrieved from the same flow as the
   TWITTER_ACCESS_TOKEN.

While the Micropub and Twitter tokens are individually optional, at least one full set is required
or this will throw an error as it won't have anything to do.

You'll also want to remove all the text in
this [file on the ignored-tracks branch](https://github.com/wardellbagby/wardell_listens/blob/ignored-tracks/ignored.txt)
so that your bot can post songs mine might already have. Make sure to keep the branch and the
file though, as it's expected to exist by the Github Action.

When run locally, to ease with development, you can instead add these values to a `secrets.env` file
in [src/main/resources](src/main/resources) in the format of `NAME=VALUE`, with a newline
separating every key-value pair.
