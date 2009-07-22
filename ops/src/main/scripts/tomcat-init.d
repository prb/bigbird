#/bin/sh

export CATALINA_HOME=/opt/apache-tomcat-6.0.20
export BASEDIR=/opt/apache-tomcat-6.0.20
export JAVA_OPTS='-Xmx1000m'

start() {
	$CATALINA_HOME/bin/catalina.sh start
}

stop() {
    $CATALINA_HOME/bin/catalina.sh stop
}

status() {
    echo 'foo'
}

case $1 in 
  'status')
    status
	;;

  'start')
	start       	
	;;

  'stop')
    stop
    ;;

  'restart')
    stop
    start
    ;;
esac
