#!/bin/bash

CATALINA_HOME=/opt/apache-tomcat-6.0.20

# Go get T
if [ ! -e apache-tomcat-6.0.20.zip ]
then
  wget -q http://bigbird-stage.s3.amazonaws.com/apache-tomcat-6.0.20.zip
fi

# Make sure V behaves like a reasonable OS citizen.
chmod +x tomcat-init.d
rm -f /etc/init.d/tomcat
ln -s `pwd`/tomcat-init.d /etc/init.d/tomcat
rm -f /etc/rc4.d/S98tomcat
ln -s `pwd`/tomcat-init.d /etc/rc4.d/S98tomcat

# Unpack T
mkdir -p /opt
if [ -e $CATALINA_HOME ]
then
  rm -fr /tmp/tomcat-old
  mv $CATALINA_HOME /tmp/tomcat-old
fi
unzip -q -o apache-tomcat-6.0.20.zip -d /opt

chmod +x $CATALINA_HOME/bin/*.sh

rm -rf $CATALINA_HOME/webapps/*
rm -rf $CATALINA_HOME/logs
mkdir -p /var/log/tomcat
ln -s /var/log/tomcat $CATALINA_HOME/logs

mkdir -p $CATALINA_HOME/webapps/ROOT

unzip -q -o bigbird/bigbird-web/0.1-SNAPSHOT/bigbird-web-0.1-SNAPSHOT.war -d ${CATALINA_HOME}/webapps/ROOT