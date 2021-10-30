#!/bin/bash
SCRIPTDIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null && pwd )"
TARGETDIR="${SCRIPTDIR}/static"

declare -A redirects

redirects["README-Output.html"]="/expect/fieldvalues/"
redirects["README-Performance.html"]="/expect/performance/"
redirects["TryIt.html"]="/expect/tryit/"
redirects["Building.html"]="/developer/building/"
redirects["Internals-BaseDesign.html"]="/developer/basedesign/"
redirects["Internals-MakingNewRules.html"]="/developer/makingnewrules/"
redirects["NOTES-shading-dependencies.html"]="/developer/shadingdependencies/"
redirects["LICENSE.html"]="/using/license"
redirects["README-Usage.html"]="/using/"
redirects["README-Limitations.html"]="/using/limitations/"
redirects["README-MemoryUsage.html"]="/using/memoryusage/"
redirects["README-WebServlet.html"]="/using/webservlet"
redirects["RelatedProjects.html"]="/other/relatedprojects/"
redirects["README-Commandline.html"]="/udf/commandline/"
redirects["UDF-ApacheBeam.html"]="/udf/apache-beam/"
redirects["UDF-ApacheBeamSql.html"]="/udf/apache-beam-sql/"
redirects["UDF-ApacheDrill.html"]="/udf/apache-drill/"
redirects["UDF-ApacheFlink.html"]="/udf/apache-flink/"
redirects["UDF-ApacheFlinkTable.html"]="/udf/apache-flink-table/"
redirects["UDF-ApacheHive.html"]="/udf/apache-hive/"
redirects["UDF-ApacheNifi.html"]="/udf/apache-nifi/"
redirects["UDF-ApachePig.html"]="/udf/apache-pig/"
redirects["UDF-ElasticSearch.html"]="/udf/elastic-search/"
redirects["UDF-LogParser.html"]="/udf/logparser/"
redirects["UDF-Logstash.html"]="/udf/elastic-logstash/"
redirects["UDFs.html"]="/udf/"

# For every key in the associative array..
for OldUrl in "${!redirects[@]}"; do
  RedirectTo=${redirects[$OldUrl]}
cat > ${TARGETDIR}/$OldUrl <<RedirectScript
<!DOCTYPE HTML>
<html lang="en" >
    <head>
        <meta charset="UTF-8">
        <meta content="text/html; charset=utf-8" http-equiv="Content-Type">
        <meta name="robots" content="noindex" />
        <meta http-equiv="refresh" content="0; URL=${RedirectTo}" />
    </head>
<body>
    <p>This page has been moved to <a href="${RedirectTo}">here</a>.</p>
</body>
RedirectScript

done

