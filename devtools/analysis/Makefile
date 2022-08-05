#
# Yet Another UserAgent Analyzer
# Copyright (C) 2013-2022 Niels Basjes
#
# Licensed under the Apache License, Version 2.0 (the \"License\");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
RAWLOGS=$(wildcard *.txt)
ESJSON=$(subst .txt,.ndjson,$(RAWLOGS))
LOADEDES=$(subst .ndjson,.ndjson.loaded,$(ESJSON))

#$(info    RAWLOGS is $(RAWLOGS))
#$(info    ESJSON  is $(ESJSON))
#$(info    LOADEDES  is $(LOADEDES))

.PHONY: all
all:: createindex load

# File format: <year>\t<month>\t<day>\t<count>\t<useragent>\n
# Example: 2022	6	1	4	Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html
# I.e. 4 hits by Googlebot on June 1st 2022

%.ndjson: %.txt
	@echo "Making $@ : Start"
	@cat $< \
	| sed 's#^\([^\t]*\)\t\([^\t]*\)\t\([^\t]*\)\t\([^\t]*\)\t\(.*\)$$#{ "index" : { "_index" : "useragents" }}\n{ "@timestamp": "\1-\2-\3T00:00:00.000Z", "count": \4, "ua": "\5" }#g;s@-\([0-9]\)-@-0\1-@g;s@-\([0-9]\)T@-0\1T@g' \
	> $@;
	@echo "Making $@ : Done"

fullclean::
	-@rm $(ESJSON)

load:: $(LOADEDES)

%.ndjson.loaded: %.ndjson
	curl -H 'Content-Type: application/x-ndjson' -XPOST 'localhost:9200/useragents/_bulk' --data-binary @$<
	date > $@

.PHONY: wipeindex

wipeindex:
	@-@rm $(LOADEDES)
	@echo -n "Delete index: "
	@curl -s -H 'Content-Type: application/json' -X DELETE 'localhost:9200/useragents'

.PHONY: createindex

# Recreate the empty index in ES with all the settings
createindex:
	@echo -n "Define pipeline: "
	@curl -s -H 'Content-Type: application/json' -X PUT 'localhost:9200/_ingest/pipeline/yauaa' -d '{"description": "A pipeline parse the useragents","processors": [{"yauaa" : {"field_to_header_mapping" : {"ua": "User-Agent"},"target_field"  : "parsed","cacheSize" : 10000}}]}'
	@echo ""
	@echo -n "Create index: "
	@curl -s -H 'Content-Type: application/json' -X PUT 'localhost:9200/useragents'
	@echo ""
	@echo -n "Set default pipeline: "
	@curl -s -H 'Content-Type: application/json' -X PUT 'localhost:9200/useragents/_settings' -d '{"index" : {"default_pipeline" : "yauaa"}}'
	@echo ""

# Do a rough first pass over the input files (stored in a different directory) and recreate the base input files from source.
bootstrap:
	ls | grep ^ua | xargs -r rm
	split -l100000 -x -a6 --additional-suffix=.txt ../ua2022*.txt ua2022-
