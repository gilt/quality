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

First setup the (apidoc CLI)[https://github.com/gilt/apidoc-cli]. Make
sure your local environment has access to the gilt organization. If
you need an API token, visit http://www.apidoc.me/tokens/

Configuration data for apidoc is stored in the .apidoc file.

To sync all code (uploads currenty api.json file, then downloads code):

  /web/apidoc-cli/bin/apidoc update

To upload latest API to apidoc (without also downloading the generated code):

  /web/apidoc-cli/bin/apidoc upload gilt quality ./api/api.json --version 0.1.13


Deploying schema
================

Create a schema tarball:
  cd schema
  sem-dist
  scp -F /web/metadata-gilt-architecture/ssh/config dist/schema-0.0.36.tar.gz <private IP>:~/

Private IP will look like: 172.16.16.183 and can be obtained from

  https://console.aws.amazon.com/ec2/v2/home?region=us-east-1#Instances:search=i-530d35bd;sort=desc:launchTime

  sudo apt-get install postgresql-client git ruby

  git clone git://github.com/gilt/schema-evolution-manager.git
  cd schema-evolution-manager
  git checkout 0.9.16
  ruby ./configure.rb
  sudo ruby ./install.rb

  echo "quality.cwuud87mporv.us-east-1.rds.amazonaws.com:5432:quality:web:PASSWORD" > ~/.pgpass
  chmod 0600 ~/.pgpass

  sem-apply --host quality.cwuud87mporv.us-east-1.rds.amazonaws.com --name quality --user web

All credentials are stored in ionroller:

  http://ionroller.tools.gilt.com/services/quality-api/config
  http://ionroller.tools.gilt.com/services/quality-www/config

Releasing
=========
ionroller release quality-api 0.1.13
ionroller release quality-www 0.1.13
