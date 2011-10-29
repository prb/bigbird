#/bin/sh

start() {
	pids=`ps xwww | grep voldemort.server.VoldemortServe[r] | awk '{print $1}'`

	if [ "$pids" != "" ]
	then
        	echo $(hostname)': Voldemort is already running under '${pids}'...'
	else
               /opt/voldemort/bin/voldemort-server.sh /opt/voldemort-home >> /var/log/voldemort.log 2>&1 &
        fi
}

stop() {
      	pids=`ps xwww | grep voldemort.server.VoldemortServe[r] | awk '{print $1}'`
       	if [ "$pids" != "" ]
       	then
		/opt/voldemort/bin/voldemort-stop.sh
	else
		echo $(hostname):' Voldemort is not running.'
	fi
}

status() {
       	pids=`ps xwww | grep voldemort.server.VoldemortServe[r] | awk '{print $1}'`
       	if [ "$pids" != "" ]
       	then
		echo $(hostname)': Voldemort is running under '${pids}'...'
	else
		echo $(hostname)': Voldemort is not running.'
	fi

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
