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

Deploying schema
================

Create a schema tarball:
  cd schema
  sem-dist
  scp -F /web/metadata-gilt-architecture/ssh/config dist/schema-0.0.36.tar.gz <private IP>:~/

Private IP will look like: 172.16.16.183 and can be obtained from

  https://console.aws.amazon.com/ec2/v2/home?region=us-east-1#Instances:search=i-530d35bd;sort=desc:launchTime

  sudo apt-get install postgresql-client
  sudo apt-get install git
  sudo apt-get install ruby

  git clone git://github.com/gilt/schema-evolution-manager.git
  cd schema-evolution-manager
  git checkout 0.9.12
  ruby ./configure.rb
  sudo ruby ./install.rb

  echo "master.db.quality.architecture.giltaws.com:5432:quality:web:PASSWORD" > ~/.pgpass
  chmod 0600 ~/.pgpass

  sem-apply --host master.db.quality.architecture.giltaws.com --name quality --user web

To get the postgresql password:

  aws --profile gilt-architecture s3 cp s3://metadata-gilt-architecture/ionblaster/quality.json .

Releasing code
==============
Install ionblaster:
  curl -s https://s3.amazonaws.com/ionblaster/install | sh

Release to ec2:
  /web/metadata-gilt-architecture/bin/exec /web/apidoc/script/release-and-deploy [--tag x.y.z] api www
