#!/bin/sh

# TODO: add some tests before benchmark to ensure that API path is implemented
# and returns legitimate results

# TODO: determine that wrk doesn't allow errors

wrk -t3 -c500 -d300s http://localhost:9009/api/count/35
