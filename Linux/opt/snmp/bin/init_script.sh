#! /bin/sh
### BEGIN INIT INFO
# Provides:	mediators
# Required-Start: $all
# Required-Stop: $all
# Default-Start: 3 4 5
# Default-Stop:	0 1 2 6
# Short-Description: Start Mediators at boot time
# Description: Starting all mediators which have the run file in their folders
### END INIT INFO


do_start () {
  sh $NETCONF2SNMP_HOME/bin/start_all.sh
}

do_stop () {
  sh $NETCONF2SNMP_HOME/bin/stop_all.sh
}

do_status () {
  echo "nothing to show"
}

case "$1" in
  start|"")
	do_start
	;;
  restart|reload|force-reload)
	echo "Error: argument '$1' not supported" >&2
	exit 3
	;;
  stop)
	do_stop
	;;
  status)
	do_status
	exit $?
	;;
  *)
	echo "Usage: init_script.sh [start|stop]" >&2
	exit 3
	;;
esac

:
