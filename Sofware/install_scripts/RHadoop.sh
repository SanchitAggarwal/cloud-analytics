#!/bin/bash
apt-get install libboost-dev libevent-dev libtool flex bison g++ automake pkg-config
apt-get install libboost-test-dev
apt-get install libmono-dev ruby1.8-dev libcommons-lang-java php5-dev
apt-get r-base
echo 'options(repos=structure(c(CRAN="http://cran.fr.r-project.org")))' >> /etc/R/Rprofile.site
apt-get install r-cran-rjava
apt-get install r-cran-rcpp
Rscript -e 'install.packages("RJSONIO");'
Rscript -e 'install.packages("itertools");'
Rscript -e 'install.packages("digest");'
cd /tmp
R CMD INSTALL rmr rmr_1.3.1.tar.gz
rm -rf rmr_1.3.1.tar.gz
cd /tmp
R CMD INSTALL rhdfs rhdfs_1.0.4.tar.gz
rm -rf rhdfs_1.0.4.tar.gz
cd /tmp
cp /usr/local/lib/libthrift-0.8.0.so /usr/lib/
R CMD INSTALL rhbase rhbase_1.0.4.tar.gz
rm -rf rhbase_1.0.4.tar.gz
