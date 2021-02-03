# reboot

A small personal project to "reboot" my Clojure(Script) foo after a short break from it.

Provides a ClojureScript UI using re-frame to Xerts API
Xert is a (mainly) Cycling training platform to manage fitness and training using Power data from appropriate sources. There is an extensive existing UI for the service (which does not use this API), but this project is just to play with the available API.

This is a ClojureScript only project and as such there are issues with CORS. The Xert API is not configured for any origin so I have had to use a Chrome plugin to fudge the CORS restrictions. It's been a while since I've tackled CORS so I may be barking up the wrong tree.

CORS plugin used is: Moesif CORS
Setup was: Access Control Allow Origin to http://dobbie.home:9500 which is an arbitarly configured local host name

I need to contact Xert (BaronBiosys) to progress this further if this is to be made publicly usable without providing a backend.


## Usage

To start development environment:
clojure -M:build-dev

An active user login is required to the Xert service to use this.

## License

Copyright © 2013 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.