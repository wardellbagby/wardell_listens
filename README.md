# [Wardell Listens](https://twitter.com/wardell_listens)

It's true. I'm Wardell, and listen is what I do.

## What is this?

This is a repository containing the code that runs
the [@wardell_listens](https://twitter.com/wardell_listens) Twitter account.

## How does it work?

It uses my [ListenBrainz listens](https://listenbrainz.org/user/wardellbagby/)
to tweet out a new track every week, based on what I listened to the past week. It uses a GitHub
action, scheduled to run every Monday at 9:30am PDT, to comb through the weekly listens and
find a new track to tweet out.

## Why does it exist?

Music is meant to be shared, and good music doubly so. Plus, now everyone can see exactly how bad my
taste in music is!

## Can I fork this and use it myself?

You sure can! Fork this project into your own repository and set up these secrets for your new repo.

1. LISTENBRAINZ_USER: The username of the ListenBrainz account you want to fetch
   the listens for.
2. TWITTER_CONSUMER_KEY: The consumer key (aka API key) of
   the [Twitter application](https://developer.twitter.com/en/docs/authentication/oauth-1-0a/api-key-and-secret)
   you have to create to manage your own Twitter account to post tweets.
3. TWITTER_CONSUMER_SECRET: The consumer secret (aka API secret) of your Twitter application.
4. TWITTER_ACCESS_TOKEN: The OAuth1 access token for your Twitter account. You can get this by
   manually [authenticating with Twitter](https://developer.twitter.com/en/docs/authentication/oauth-1-0a/obtaining-user-access-tokens)
   using the OAuth1 PIN-based flow.
5. TWITTER_ACCESS_TOKEN_SECRET: Can be retrieved from the same flow as the TWITTER_ACCESS_TOKEN.

When run locally, to ease with development, you can instead add these values to a `secrets.env` file
in [src/main/resources](src/main/resources) in the format of `NAME=VALUE`, with a newline
separating every key-value pair.
