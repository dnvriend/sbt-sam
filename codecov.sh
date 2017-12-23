#!/bin/bash
export CODECOV_TOKEN=$SBT_SAM_CODECOV_TOKEN
sbt clean coverage test coverageReport
bash <(curl -s https://codecov.io/bash)