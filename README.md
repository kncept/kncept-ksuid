# kncept-ksuid

A lightweight, flexable and dependency free implementation of the Ksuid

# what is a Ksuid?

A Ksuid is a particular match of Unique Identifier generation and Binary Encoding.
It has a 4 byte timestamp, and 16 bytes of entropy, and encodes to a 27 character string.

The string is URL safe, and will be naturally sorted my date order.


# Origin

I *think* the concept came from https://github.com/segmentio/ksuid.
When I started using them, the java implementation I was using had some encoding / length bugs, so I wrote a partial implementation.
Finally, I broke this out into its own open source project and finished it.

Disclaimer - there are probably implementations that work just fine, and don't have the encoding/decoding bugs.
This is still being open sourced, as it's an extremely light implementation and includes support for multiple ways to use them.

