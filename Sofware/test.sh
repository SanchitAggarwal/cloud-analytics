#!/bin/bash
DIR="$( cd -P "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
/bin/rm /usr/local/make
echo -e "$DIR" > /usr/local/make
