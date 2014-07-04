#!/bin/sh

wrk -t3 -c600 -d40s http://localhost:9009/api/count/50
