# Restfest vimeo export

This projects uses Scala as the development language. You will need to have Java installed.

This is licensed as Apache 2.0.

## Usage

If you want to use this you will need to create a vimeo "app", and get your clientId, and clientSecret.

Add these values into `secrets.properties`. This file is automatically ignored by git.

The `secrets.properties` file looks like this

```
id=clientId
secret=clientSecret
```

We will be able to export a channel as a Json file. the json file name will be the same as the `channelId`.

## Running on *nix systems and osx

`./sbt "run <channelid>"`

replace <channelid> with for instance `restfest2015`
