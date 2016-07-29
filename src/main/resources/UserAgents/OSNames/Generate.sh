#!/bin/bash
# Yet Another UserAgent Analyzer
# Copyright (C) 2013-2016 Niels Basjes
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.

(
echo "# ============================================="
echo "# THIS FILE WAS GENERATED; DO NOT EDIT MANUALLY"
echo "# ============================================="
echo "config:"

echo "  - lookup:"
echo "      name: 'OperatingSystemName'"
echo "      map:"
cat "OperatingSystemNames.csv" | grep . | fgrep -v '#' | while read line ; \
do
    tag=$(   echo ${line} | cut -d'|' -f1)
    osname=$( echo ${line} | cut -d'|' -f2)
    osversion=$(  echo ${line} | cut -d'|' -f3)
    echo "       \"${tag}\" : \"${osname}\""
done

echo "  - lookup:"
echo "      name: 'OperatingSystemVersion'"
echo "      map:"
cat "OperatingSystemNames.csv" | grep . | fgrep -v '#' | while read line ; \
do
    tag=$(   echo ${line} | cut -d'|' -f1)
    osname=$( echo ${line} | cut -d'|' -f2)
    osversion=$(  echo ${line} | cut -d'|' -f3)
    echo "       \"${tag}\" : \"${osversion}\""
done

) > ../OperatingSystemNames.yaml
