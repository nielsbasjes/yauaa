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

echo "# Yet Another UserAgent Analyzer"
echo "# Copyright (C) 2013-2016 Niels Basjes"
echo "#"
echo "# This program is free software: you can redistribute it and/or modify"
echo "# it under the terms of the GNU General Public License as published by"
echo "# the Free Software Foundation, either version 3 of the License, or"
echo "# (at your option) any later version."
echo "#"
echo "# This program is distributed in the hope that it will be useful,"
echo "# but WITHOUT ANY WARRANTY; without even the implied warranty of"
echo "# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the"
echo "# GNU General Public License for more details."
echo "#"
echo "# You should have received a copy of the GNU General Public License"
echo "# along with this program.  If not, see <http://www.gnu.org/licenses/>."


echo "config:"
echo "- matcher:"
echo "    extract:"
echo "    - 'DeviceBrand :  200:LookUp[WindowsOEMCodes;agent.(1)product.comments.entry.text]'"

echo "- lookup:"
echo "    name: 'WindowsOEMCodes'"
echo "    map:"
cat "WindowsOEMCodes.csv" | grep . | fgrep -v '#' | while read line ; \
do
    code=$(echo ${line} | cut -d' ' -f1)
    value=$(echo ${line} | cut -d' ' -f2- | sed 's/^ *//')
    echo "      \"${code}\" : \"${value}\""
done

) > ../WindowsOEMCodes.yaml
