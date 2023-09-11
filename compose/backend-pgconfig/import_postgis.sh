#!/bin/bash

baseurl="http://localhost:9090/geoserver/cloud"
resturl="$baseurl/rest"
credentials="admin:geoserver"

main() {
	import_data

	workspace=ne
	create_workspace $workspace
	create_datastore $workspace

	create_layer $workspace postgis countries countries
	create_layer $workspace postgis boundary_lines_land boundary_lines
	create_layer $workspace postgis coastlines coastline
	create_layer $workspace postgis disputed_areas disputed
	create_layer $workspace postgis populated_places populated_places
	
	declare -a published=(countries boundary_lines_land coastlines disputed_areas populated_places)
	create_layergroup $workspace world "${published[@]}"
}

import_data() {
	echo ">>>>>>>>>>>>>>>> Import NaturalEarth sample data so PostGIS <<<<<<<<<<<<<<<<<"

	docker compose run --rm gdal ogr2ogr \
	 -f PostgreSQL "PG:host=postgis user=postgis password=postgis dbname=postgis port=5432" \
	 -lco SCHEMA=public \
	 /sample_data/ne/natural_earth.gpkg

	docker compose exec postgis psql -U postgis -c \
	"SELECT tablename FROM pg_catalog.pg_tables WHERE schemaname = 'public' and tablename <> 'spatial_ref_sys';"

}

create_workspace() {
	workspace=$1
	echo ">>>>>>>>>>>>>>>> Delete and re-create workspace $workspace <<<<<<<<<<<<<<<<<"
	curl --silent -u $credentials -XDELETE "$resturl/workspaces/$workspace?recurse=true"
	curl -i -u $credentials -XPOST \
		-H "Content-type: text/xml" \
		-d "<workspace><name>$workspace</name></workspace>" \
		"$resturl/workspaces"
}

create_datastore(){
 	workspace=$1
 	xml="
<dataStore>
  <name>postgis</name>
  <type>PostGIS (JNDI)</type>
  <enabled>true</enabled>
  <workspace><name>$workspace</name></workspace>
  <connectionParameters>
    <entry key=\"dbtype\">postgis</entry>
    <entry key=\"jndiReferenceName\">java:comp/env/jdbc/postgis</entry>
    <entry key=\"schema\">public</entry>
    <entry key=\"Estimated extends\">true</entry>
    <entry key=\"fetch size\">1000</entry>
    <entry key=\"encode functions\">true</entry>
    <entry key=\"Expose primary keys\">false</entry>
    <entry key=\"Support on the fly geometry simplification\">true</entry>
    <entry key=\"Batch insert size\">1</entry>
    <entry key=\"preparedStatements\">false</entry>
    <entry key=\"Method used to simplify geometries\">FAST</entry>
    <entry key=\"namespace\">http://cog</entry>
    <entry key=\"Loose bbox\">true</entry>
  </connectionParameters>
</dataStore>
"

	echo ">>>>>>>>>>>>>>>> Creating datastore $workspace:postgis <<<<<<<<<<<<<<<<<"
	curl -v -u $credentials \
	 -H "Content-type: text/xml" \
	 -d "$xml" \
	 "$resturl/workspaces/$workspace/datastores"
}

create_style () {
	workspace=$1
	sldfile=$2
	stylename=$3
	
	if [ ! -f $sldfile ]
	then
    	echo "SLD does not exist $sldfile"
    	exit 1
	fi

	curl -v -u $credentials \
	 -H "Content-type: application/vnd.ogc.sld+xml" \
	 -d@$sldfile \
	 "$resturl/workspaces/$workspace/styles?name=$stylename"
}

create_layer () {
	workspace=$1
	datastore=$2
	layername=$3
	sldname=$4
	echo ">>>>>>>>>>>>>>>> creating layer $workspace:$layername with style $style <<<<<<<<<<<<<<<<<"
  
    create_style $workspace sample_data/ne/styles/$sldname.sld $sldname
    
	curl -v -u $credentials \
	 -H "Content-type: text/xml" \
	 -d "<featureType><name>$layername</name><title>$layername</title></featureType>" \
	 "$resturl/workspaces/$workspace/datastores/$datastore/featuretypes"


	curl -v -u $credentials "$resturl/workspaces/$workspace/styles/$sldname"

	curl -v -u $credentials \
	 -XPUT -H "Content-type: text/xml" \
	 -d "<layer><defaultStyle><name>$sldname</name></defaultStyle></layer>" \
	 "$resturl/layers/$workspace:$layername"

}

create_layergroup() {
	workspace=$1
	shift
	name=$1
	shift
	layers=("$@")
	
echo Creating layergroup $workspace:$name with layers ["${layers[@]}]"
	lg="<layerGroup>
	  <name>world</name>
	  <title>World</title>
	  <mode>SINGLE</mode>
	  <enabled>true</enabled>
	  <advertised>true</advertised>
	  <publishables>"
  
	for layer in "${layers[@]}"
	do 
	  lg="$lg
	    <published type=\"layer\">$workspace:$layer</published>"
	done

	lg="$lg
	   	</publishables>
   		<styles>
   		</styles>
  		<bounds><minx>-180</minx><maxx>180</maxx><miny>-90</miny><maxy>90</maxy><crs>EPSG:4326</crs></bounds>
  	  </layerGroup>"

	curl --silent -u $credentials \
	 "$resturl/layergroups" \
	 -H "Content-Type: application/xml" \
	 -d "$lg"
}
main "$@"
