#!/bin/bash

args=`find dataset -type f | xargs`

time bash java/cuncorrent/run.sh $args
