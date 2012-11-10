#!/bin/bash
sudo addgroup hadoop
sudo adduser --ingroup hadoop hduser
sudo mkdir -p /app/hadoop/tmp
sudo chown hduser:hadoop /app/hadoop/tmp
sudo chmod 750 /app/hadoop/tmp
su - hduser
ssh-keygen -t rsa -P ""
cat $HOME/.ssh/id_rsa.pub >> $HOME/.ssh/authorized_keys
net.ipv6.conf.all.disable_ipv6 = 1
net.ipv6.conf.default.disable_ipv6 = 1
net.ipv6.conf.lo.disable_ipv6 = 1
cp ../hadoop-1.0.3.tar.gz /usr/local/
cd /usr/local
sudo tar xzf hadoop-1.0.3.tar.gz
sudo mv hadoop-1.0.3 hadoop
sudo chown -R hduser:hadoop hadoop
echo "export HADOOP_HOME=/usr/local/hadoop\nunalias fs &> /dev/null\nalias fs=\"hadoop fs\"\nunalias hls &> /dev/null\nalias hls=\"fs -ls\"\nexport PATH=$PATH:$HADOOP_HOME/bin" >> /home/hduser/.bashrc
/usr/local/hadoop/bin/hadoop namenode -format
/usr/local/hadoop/bin/start-all.sh
