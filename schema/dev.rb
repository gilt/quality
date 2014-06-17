#!/usr/bin/env ruby
# == Wrapper script to update a local postgrseql database
#
# == Usage
#  ./dev.rb
#

command = "sem-apply --host localhost --user web --name quality"
puts command
system(command)
