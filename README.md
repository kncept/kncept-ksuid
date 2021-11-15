# kncept-ksuid

A lightweight, flexable and dependency free implementation of the Ksuid

# what is a Ksuid?

A Ksuid is a particular match of Unique Identifier generation and Binary Encoding.
It has a 4 byte timestamp, and 16 bytes of entropy, and encodes to a 27 character string.

The string is URL safe, and will be naturally sorted my date order.

# Using

This project is release on Maven Central.
You are welcome to clone and build, but please open PR's if there are bugs.
    `implementation com.kncept.ksuid:ksuid:1.0.0`
    
Just import the main class and off you go.
You may need to write a little encoder/decoder function for a custom type if you wish to use this some sort of ORM, but 
this works excellently all the way from passing in on a URL, to saving in a database, and back again.


     import com.kncept.ksuid.Ksuid;
     ...
     System.out.println(new Ksuid());


# Origin

I *think* the concept came from https://github.com/segmentio/ksuid.
When I started using them, the java implementation I was using had some encoding / length bugs, so I wrote a partial implementation.
Finally, I broke this out into its own open source project and finished it.

Disclaimer - there are probably implementations that work just fine, and don't have the encoding/decoding bugs.
This is still being open sourced, as it's an extremely light implementation and includes support for multiple ways to use them.

# The Future

If the library ever needs to be extended, submit a PR!