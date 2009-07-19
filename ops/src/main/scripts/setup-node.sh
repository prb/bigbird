#!/bin/bash

# Put V configuration files in place
mkdir -p /opt/voldemort-home/config
for i in cluster.xml server.properties stores.xml
do
    cp $i /opt/voldemort-home/config
done

# Make sure V behaves like a reasonable OS citizen.
chmod +x voldemort-init.d
rm -f /etc/init.d/voldemort
ln -s `pwd`/voldemort-init.d /etc/init.d/voldemort
rm -f /etc/rc4.d/S98voldemort
ln -s `pwd`/voldemort-init.d /etc/rc4.d/S98voldemort

# Unpack V
mkdir -p /opt/voldemort
tar xjf voldemort-__v.version__.tbz2 -C /opt/voldemort
