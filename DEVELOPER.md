You'll need to get the quality schema into your dev postgresql database; to do that:

$ psql
psql> CREATE DATABASE quality;

Then you'll need to create the database schema using [schema evolution manager](https://github.com/gilt/schema-evolution-manager#installation)

$ cd /web/quality/schema
$ ./dev.rb

The application consists of an API service and a www application.

One way to do this is to run a screen session, and in one screen do:

  $ sbt
  sbt> project api
  sbt> run 8001

...then in another screen, do:

  $ sbt
  sbt> project www
  sbt> run 8000

Goto http://localhost:8000 in your browser

Now both should be running and able to talk to each other, and should recompile
in situ for a nice development experience.

Updating generated code
=======================
script/update-generated-code

Releasing code
==============

Install ionblaster:
  curl -s https://s3.amazonaws.com/ionblaster/install | sh

Release to ec2:
  /web/metadata-gilt-architecture/bin/exec /web/apidoc/script/release-and-deploy [--tag x.y.z] api www
