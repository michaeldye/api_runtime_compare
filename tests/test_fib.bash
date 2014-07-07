#!/usr/bin/env bash

URI_BASE='http://localhost:9009/api/fib'
WRK_CMD="wrk -t2 -c400 -d600s -s ${BASH_SOURCE%/*}/fibs.lua $URI_BASE/"
FIB_4000_PATH=${BASH_SOURCE%/*}/fib_4000.dump

# TODO: consolidate with other scripts
exit_error()
{
  echo "Error: $1"
  exit 1
}

test_correct()
{
  local URI=$URI_BASE/4000
  local RESP=$( curl -sw "\n%{http_code}" $URI ) 2>/dev/null

  local CODE=$(echo "$RESP" | tail -n1)
  local BODY=$(echo "$RESP" | head -n-1)

  if [[ "200" != $CODE ]]; then
    exit_error "$CODE GET $URI"
  else
    local RIGHT=$(<$FIB_4000_PATH)
    if [[ $BODY != $RIGHT ]]; then
      exit_error "$URI failed. Body should have matched contents of $FIB_4000_PATH but didn't"
    fi
  fi
}

echo "Testing $URI_BASE for correctness..."
test_correct
echo "Testing complete, starting benchmark"
$WRK_CMD
