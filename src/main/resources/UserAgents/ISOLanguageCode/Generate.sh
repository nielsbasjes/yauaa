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

echo ""
echo "config:"

echo "- matcher:"
echo "    extract:"
echo "    - 'AgentLanguageCode :  5:LookUp[ISOLanguageCodes;agent.(1)product.(1)comments.entry.([1-2])text]'"
echo "    - 'AgentLanguage :  5:LookUp[ISOLanguageCodesName;agent.(1)product.(1)comments.entry.([1-2])text]'"
echo ""

echo "- matcher:"
echo "    extract:"
echo "    - 'AgentLanguageCode :  5:LookUp[ISOLanguageCodes;agent.([2-4])product.(1)comments.([1-5])entry.([1-2])text]'"
echo "    - 'AgentLanguage :  5:LookUp[ISOLanguageCodesName;agent.([2-4])product.(1)comments.([1-5])entry.([1-2])text]'"
echo ""

echo "- matcher:"
echo "    extract:"
echo "    - 'AgentLanguageCode :  5:LookUp[ISOLanguageCodes;agent.([1-2])product.(2)comments.([1-5])entry.([1-2])text]'"
echo "    - 'AgentLanguage :  5:LookUp[ISOLanguageCodesName;agent.([1-2])product.(2)comments.([1-5])entry.([1-2])text]'"
echo ""

echo "- lookup:"
echo "    name: 'ISOLanguageCodes'"
echo "    map:"
cat "ISOLanguageCodes.csv" | while read line ; \
do
    echo "      \"$(echo ${line} | cut -d' ' -f1)\" : \"$(echo ${line} | cut -d' ' -f1)\""
done

echo "- lookup:"
echo "    name: 'ISOLanguageCodesName'"
echo "    map:"
cat "ISOLanguageCodes.csv" | while read line ; \
do
    echo "      \"$(echo ${line} | cut -d' ' -f1)\" : \"$(echo ${line} | cut -d' ' -f2-)\""
done

) > ../ISOLanguageCode.yaml
