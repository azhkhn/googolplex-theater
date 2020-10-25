# googolplex-theater
by yuzawa-san

![Icon](src/main/resources/favicon.png)

[![Build Status](https://travis-ci.org/yuzawa-san/googolplex-theater.svg?branch=master)](https://travis-ci.org/yuzawa-san/googolplex-theater)
[![codecov](https://codecov.io/gh/yuzawa-san/googolplex-theater/branch/master/graph/badge.svg)](https://codecov.io/gh/yuzawa-san/googolplex-theater)
![GitHub top language](https://img.shields.io/github/languages/top/yuzawa-san/googolplex-theater)
![GitHub release (latest by date)](https://img.shields.io/github/v/release/yuzawa-san/googolplex-theater)
![GitHub All Releases](https://img.shields.io/github/downloads/yuzawa-san/googolplex-theater/total)

Persistently maintain multiple Chromecast devices on you local network without using your browser.
Ideal for digital signage applications.
Originally developed to display statistics dashboards.

![Example](docs/example.jpg)

There are several tools and libraries out there (see below), but this project is intended to be very minimalist.
There is a simple web UI to check device info and trigger refreshes.

![Screenshot](docs/screenshot.png)

There is no backing database or database dependencies, rather there is a simple JSON config file which is watched for changes.
The JSON configuration is conveyed to the receiver application, which by default accepts url to display in an IFRAME.
The receiver application can be customized easily to suit your needs.
The application will try to reconnect if a session is ended for whatever reason.

## Requirements

This application has very minimal runtime requirements:

* Java runtime version 8 or later.
* Linux or MacOS is preferred. Windows is not currently tested.

There are certain requirements for networking which are beyond the realm of this project, but should be noted:

* This application must run on the same network as your Chromecasts.
* Multicast DNS must work on your network and on the machine you run the application on. This is how the devices and the application discover each other.
* IMPORTANT: URLs must be HTTPS and must not [deny framing](https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/X-Frame-Options) This is a limit of using an IFRAME to display content.

Development requirements:

* JDK 8 or later. The [gradle wrapper](https://docs.gradle.org/current/userguide/gradle_wrapper.html) is used to build this application and is already included.

NOTE: The Java 8 is the minimum target version to support some older versions of 
Raspberry Pi OS (Raspbian). This may be subject to change.

## Installation

Download a [release version](https://github.com/yuzawa-san/googolplex-theater/releases) ZIP archive.

Alternatively, clone/download this repo, and run:
```
./gradlew build
```

This will generate the application ZIP archive in `./build/distributions/googolplex-theater-VERSION.zip`

Once you have the ZIP archive, expand it in the desired destination location and `cd` into directory.

To show all options:
```
./bin/googolplex-theater --help
```

To run the application with default settings:
```
./bin/googolplex-theater
```

### Configuration

The cast configuration is defined in `./conf/cast_config.json`.
The location of your configuration can be customized using a command line argument.
The file is automatically watched for changes.
Some example use cases involve using cron and putting your config under version control and pulling from origin periodically, or downloading from S3/web, or updating using rsync/scp.

### Running as Daemon

To provide resiliency, it is recommended to run the application as a daemon.
See service descriptor files for upstart, systemd, and launchd in the `./service/` directory. They should work with minor modifications. Please refer to their respective installation guides to enable on your system.

### Case Study: Grafana Dashboards

The maintainer has used this to show statistics dashboards in a software engineering context.

* Configure and name your Chromecasts.
* Create one Grafana playlist per device.
* Figure out how to use proper Grafana auth (proxy, token, etc).
* Make your cast config file with each playlist url per device.
* Place the cast config file under version control (git) or store it someplace accessible (http/s3/gcs).
* Download application on Raspberry Pi.
* Install Java runtime.
* Add a cron job to pull the cast config file from wherever you stored it (alternatively configure something to push the file to the Raspberry Pi).
* Run the application as a daemon using systemd or upstart or whatever you want.
* Config is updated periodically as our dashboard needs change. The updates are automatically picked up.
* If a screen needs to be refreshed, one can do so by accessing the web UI and hitting a few buttons.

### Using a Custom Receiver

If you wish to customize the behavior of the receiver from just displaying a single URL in an IFRAME, see the example custom receiver in `receiver/custom.html`.

For custom receivers, you will be required to [sign up as a Chromecast developer](https://developers.google.com/cast/docs/registration#RegisterApp) and also configure [devices](https://cast.google.com/publish) for development.

Currently the device name and settings are printed to the screen. Customize the listener handler to do as you wish.

Host your modified file via HTTPS on your hosting provider of choice. Then point your new custom receiver application towards that page's URL.

Pass your APP_ID in as a command line argument when you run, and your receiver will be loaded up.

## Contributing

_NOTE: due to COVID-19 the maintainer does not have regular access to the hardware to test this application._

See [CONTRIBUTING.md](contributing.md) for more details.

This is intended to be minimalist and easy to set up, so advanced features are not the goal here. Some other projects listed below may be more suited for your use case.

### TODO

* Split screen layouts
* Dockerfile? (may not work with mdns)
* Framing proxy (may not be feasible or allowed under HTTPS)

## Related Projects

This application overlaps in functionality with some of these fine projects:

### Protocol implementations
* [node-castv2](https://github.com/thibauts/node-castv2) - nodejs library
* [nodecastor](https://github.com/vincentbernat/nodecastor) - nodejs library
* [chromecast-java-api-v2](https://github.com/vitalidze/chromecast-java-api-v2) - java library
* [pychromecast](https://github.com/balloob/pychromecast) - python library

Foundational work has been done to research how the Chromecast protocol works and these protocol libraries have been developed in a variety of languages. A lot of the headless senders are built off of these.

### Browser Senders
* [dashcast](https://github.com/stestagg/dashcast) - simple dashboard display application 
* [chromecast-dashboard](https://github.com/boombatower/chromecast-dashboard) - similar to dashcast

These applications cast directly from your browser. You may need to have your browser up and running all of the time.

### Headless Senders
* [greenscreen](https://github.com/groupon/greenscreen) - original digital signage implementation
* [multicast](https://github.com/superhawk610/multicast) - a fork/refactor of greenscreen
* [Chromecast-Kiosk](https://github.com/mrothenbuecher/Chromecast-Kiosk) - similar to greenscreen or multicast

These applications cast without a Chrome browser running, rather they utilize the Chromecast protocol to establish a communication session with the devices directly.

This application is most similar to the headless sender projects. It does not use a protocol implementation library.

## Name

It is designed for multiple Chromecasts, rather than a [googol](https://en.wikipedia.org/wiki/Googol) or [googolplex](https://en.wikipedia.org/wiki/Googolplex).
It is from [The Simpsons](https://simpsons.fandom.com/wiki/Springfield_Googolplex_Theatres). The developer made it singular and decided to use the American spelling.
Googol sure does sound like the manufacturer of the Chromecast.