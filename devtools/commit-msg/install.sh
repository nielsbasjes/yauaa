#!/bin/bash
SCRIPTDIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null && pwd )"

HOOKDIR="${SCRIPTDIR}/../../.git/hooks"

if [ ! -f "${HOOKDIR}/commit-msg.sample" ];
then
  echo "Cannot find ${HOOKDIR}/commit-msg.sample"
  exit 1
fi

cp -a commit-msg* ${HOOKDIR}/
chmod 755 ${HOOKDIR}/commit-msg
echo "Installation completed"
