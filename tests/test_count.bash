#!/usr/bin/env bash

URI_BASE='http://localhost:9009/api/count'
WRK_CMD="wrk -t4 -c450 -d600s $URI_BASE/35"

exit_error()
{
  echo "Error: $1"
  exit 1
}

test_correct()
{
  for up_to in {1..500}; do
    local URI=$URI_BASE/$up_to
    local RESP=$( curl -sw "\n%{http_code}" $URI ) 2>/dev/null

    local CODE=$(echo "$RESP" | tail -n1)
    local BODY=$(echo "$RESP" | head -n-1)

    if [[ "200" != $CODE ]]; then
      exit_error "$CODE GET $URI"
    else
      local RIGHT=$( eval echo {1..$up_to} )
      if [[ $BODY != $RIGHT ]]; then
        exit_error "$URI failed. Body should have been: <$RIGHT>; was: <$BODY>"
      fi
    fi
  done
}

echo "Testing $URI_BASE for correctness..."
test_correct
echo "Testing complete, starting benchmark"
$WRK_CMD
