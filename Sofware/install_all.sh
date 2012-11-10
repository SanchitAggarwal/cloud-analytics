#!/bin/bash
export VAR="$(ifconfig br0 | sed -n '/inet /{s/.*addr://;s/ .*//;p}')"
sudo chmod 777 -R /usr/local
cp framework_zips/* /usr/local
install_scripts/*.sh
DIR="$( cd -P "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
sudo /bin/rm /usr/local/make
sudo echo -e "$DIR" > /usr/local/make
#add other details of tomcat server etc
tar xzvf $PWD/apache-tomcat-7.0.30.tar.gz
sudo mv $PWD/apache-tomcat-7.0.30 /usr/local/tomcat
export CATALINA_HOME=/usr/local/tomcat
sudo iptables -t nat -A OUTPUT -d localhost -p tcp --dport 80 -j REDIRECT --to-ports 8080
sudo iptables -t nat -A OUTPUT -d $VAR -p tcp --dport 80 -j REDIRECT --to-ports 8080
cp $PWD/*.jar /usr/local/tomcat/lib/
cp $PWD/*.war /usr/local/tomcat/webapps/
/usr/local/tomcat/bin/startup.sh
