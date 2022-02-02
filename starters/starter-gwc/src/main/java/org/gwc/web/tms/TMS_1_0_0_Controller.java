/*
 * (c) 2022 Open Source Geospatial Foundation - all rights reserved This code is licensed under the
 * GPL 2.0 license, available at the root application directory.
 */
package org.gwc.web.tms;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import javax.servlet.http.HttpServletRequest;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.geoserver.ows.URLMangler.URLType;
import org.geoserver.ows.util.ResponseUtils;
import org.geotools.gml.producer.CoordinateFormatter;
import org.geowebcache.grid.BoundingBox;
import org.gwc.service.tms.TileMap;
import org.gwc.service.tms.TileMapInfo;
import org.gwc.service.tms.TileMapService;
import org.gwc.service.tms.TileSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

/** @since 2.0 */
@RestController
@RequestMapping(path = "/service/tms", produces = MediaType.TEXT_XML_VALUE)
public class TMS_1_0_0_Controller {

    private @Autowired TileMapService service;

    private @Autowired HttpServletRequest currentRequest;

    /**
     * The root resource describes the available versions of the {@literal <TileMapService>} (and
     * possibly other services as well).
     *
     * <p>For example:
     *
     * <pre>{@code
     * <?xml version="1.0" encoding="UTF-8" ?>
     *  <Services>
     *    <TileMapService title="Example Tile Map Service"
     *      version="1.0.0" href="http://tms.osgeo.org/1.0.0/" />
     *    <TileMapService title="New Example Tile Map Service"
     *      version="1.1.0" href="http://tms.osgeo.org/1.1.0/" />
     *    <FancyFeatureService title="Features!"
     *      version="0.9" href="http://ffs.osgeo.org/0.9/" />
     *  </Services>
     * }</pre>
     *
     * @param request
     * @see https://wiki.osgeo.org/wiki/Tile_Map_Service_Specification#Root_Resource
     */
    @GetMapping(path = "/", produces = "text/xml")
    public ResponseEntity<StreamingResponseBody> getRootResource() throws Exception {
        return ResponseEntity.status(OK)
                .contentType(MediaType.TEXT_XML)
                .body(
                        outputStream -> {
                            try {
                                writeRootResource(outputStream);
                            } catch (XMLStreamException | FactoryConfigurationError e) {
                                throw new RuntimeException(e);
                            }
                        });
    }

    /**
     * The {@literal <TileMapService>} resource provides description metadata about the service and
     * lists the available <TileMaps>.
     *
     * <p>Example:
     *
     * <pre>{@code
     * <?xml version="1.0" encoding="UTF-8" ?>
     * <TileMapService version="1.0.0" services="http://tms.osgeo.org">
     *   <Title>Example Tile Map Service</Title>
     *   <Abstract>This is a longer description of the example tiling map service.</Abstract>
     *   <TileMaps>
     *     <TileMap
     *       title="VMAP0 World Map"
     *       srs="EPSG:4326"
     *       profile="global-geodetic"
     *       href="http://tms.osgeo.org/1.0.0/vmap0" />
     *     <TileMap
     *       title="British Columbia Landsat Imagery (2000)"
     *       srs="EPSG:3005"
     *       profile="local"
     *       href="http://tms.osgeo.org/1.0.0/landsat2000" />
     *   </TileMaps>
     * </TileMapService>
     * }</pre>
     *
     * @see https://wiki.osgeo.org/wiki/Tile_Map_Service_Specification#TileMapService_Resource
     */
    @GetMapping(path = "/1.0.0")
    public ResponseEntity<StreamingResponseBody> getTileMapServiceResource() throws Exception {
        return ResponseEntity.status(OK)
                .contentType(MediaType.TEXT_XML)
                .body(
                        outputStream -> {
                            try {
                                writeMapServiceResource(outputStream);
                            } catch (XMLStreamException | FactoryConfigurationError e) {
                                throw new RuntimeException(e);
                            }
                        });
    }

    /**
     * A {@literal <TileMap>} is a (usually) cartographically complete map representation. Sometimes
     * {@literal <TileMap>}s are built to be used in conjunction, as a set of stacked layers, but
     * they are generally visually complete on their own.
     *
     * <p>{@literal <TileMap>}s are composed of a set of scale-appropriate cartographic renderings,
     * each divided up into regularly spaced image tiles, called <TileSet>s. Small-scale (eg,
     * {@literal 1:10000000}) tile sets may only contain a handful of tiles. Large-scale tile sets
     * (eg, {@literal 1:10000}) may contain millions of tiles.
     *
     * <p>At a particular scale, and in a particular cartographic projection, a {@literal <TileMap>}
     * is represented by a {@literal <TileSet>}, a coverage of regularly sized and spaced images
     * that taken together form a complete visual representation of the entire area of coverage of
     * the {@literal <TileMap>}.
     *
     * <p>The {@code TileMap} identifiers generated by GWC are of the form {@code
     * <layerName>@<SRS>@<format>/<zoomLevel>}. For example: {@code roads@EPSG:3857@png}, {@code
     * roads@EPSG:3857@jpeg}, and so on.
     *
     * @see https://wiki.osgeo.org/wiki/Tile_Map_Service_Specification#TileMap_Resource
     */
    @GetMapping(path = "/1.0.0/{layerName}@{gridsetId}@{format}")
    public ResponseEntity<StreamingResponseBody> getTileMapResource(
            @PathVariable("layerName") String layerName,
            @PathVariable("gridsetId") String gridsetId,
            @PathVariable("format") String format) {

        Optional<TileMap> info = this.service.findTileMap(layerName, gridsetId, format);

        return ResponseEntity.status(info.map(i -> OK).orElse(NOT_FOUND))
                .contentType(MediaType.TEXT_XML)
                .body(
                        out -> {
                            try {
                                writeTileMapResource(info, out);
                            } catch (XMLStreamException | FactoryConfigurationError e) {
                                throw new RuntimeException(e);
                            }
                        });
    }

    /**
     * The origin of a {@literal <TileMap>} is defined in the coordinates of the spatial reference
     * system of the {@literal <TileMap>}. The x-coordinate of the tile numbers increases with the
     * x-coordinate of the spatial reference system, and the y-coordinate of the tile numbers also
     * increases with the y-coordinate of the spatial reference system.
     *
     * <p>Tiles are addressed under the "href" specified in the {@literal <TileSet>} appending the
     * "x" tile coordinate as a directory name and using the "y" tile coordinate as the file name,
     * with the file "extension" from the {@literal <TileFormat>}.
     *
     * <p>
     *
     * <pre>{@code
     * Example:
     *  <?xml version="1.0" encoding="UTF-8" ?>
     *   <TileMap version="1.0.0" tilemapservice="http://tms.osgeo.org/1.0.0">
     *    <Title>VMAP0 World Map</Title>
     *    <Abstract>A map of the world built from the NGA VMAP0 vector data set.</Abstract>
     *    <SRS>EPSG:4326</SRS>
     *    <BoundingBox minx="-180" miny="-90" maxx="180" maxy="90" />
     *    <Origin x="-180" y="-90" />
     *    <TileFormat width="256" height="256" mime-type="image/jpeg" extension="jpg" />
     *    <TileSets profile=global-geodetic">
     *      <TileSet href="http://tms.osgeo.org/1.0.0/vmap0/0" units-per-pixel="0.703125" order=
     * "0" />
     *      <TileSet href="http://tms.osgeo.org/1.0.0/vmap0/1" units-per-pixel="0.3515625" order=
     * "1" />
     *      <TileSet href="http://tms.osgeo.org/1.0.0/vmap0/2" units-per-pixel="0.17578125" order=
     * "2" />
     *      <TileSet href="http://tms.osgeo.org/1.0.0/vmap0/3" units-per-pixel="0.08789063" order=
     * "3" />
     *    </TileSets>
     *  </TileMap>
     *
     * }</pre>
     *
     * <p>The {@code TileSet} identifiers generated by GWC are of the form {@code
     * <layerName>@<SRS>@<format>/<zoomLevel>}. For example: {@code roads@EPSG:3857@png/0}, {@code
     * roads@EPSG:3857@png/1}, and so on.
     *
     * @param tileMap
     * @see https://wiki.osgeo.org/wiki/Tile_Map_Service_Specification#Tile_Resources
     */
    @GetMapping(
        path = "/1.0.0/{layerName}@{gridsetId}@{format}/{zoomLevel}/{x}/{y}.{formatExtension}"
    )
    public ResponseEntity<org.springframework.core.io.Resource> getTileResource(
            @PathVariable("layerName") String layerName,
            @PathVariable("gridsetId") String gridsetId,
            @PathVariable("format") String format,
            @PathVariable("zoomLevel") int zoomLevel,
            @PathVariable("x") long x,
            @PathVariable("y") long y,
            @PathVariable("formatExtension") String formatExtension) {

        // TileRequest req = new TileRequest(layerName, srs, format, zoomLevel, x, y);
        // Mono<TileResponse> response = service.find(req);
        // return response.map(this.mapper::toResponseEntity);
        // tmsService.getConveyor(null, null)
        throw new UnsupportedOperationException();
    }

    public void writeRootResource(OutputStream out)
            throws XMLStreamException, FactoryConfigurationError, IOException {
        String serviceUrl = tms1_0_0_Url();
        XMLStreamWriter writer = newWriter(out);
        try {
            writer.writeStartDocument();
            startElement(writer, "Services");
            {
                String value = null;
                element(
                        writer,
                        "TileMapService",
                        value,
                        "version",
                        "1.0.0",
                        "title",
                        "GeoServer Tile Map Service",
                        "href",
                        serviceUrl);
            }
            writer.writeEndElement();
            writer.writeEndDocument();
        } finally {
            writer.flush();
        }
    }

    public void writeMapServiceResource(OutputStream out)
            throws XMLStreamException, FactoryConfigurationError, IOException {
        XMLStreamWriter writer = newWriter(out);

        try (Stream<TileMapInfo> tileMapInfos = service.getTileMapInfos()) {
            writer.writeStartDocument();
            startElement(writer, "TileMapService", "version", "1.0.0", "services", baseTmsUrl());
            {
                element(writer, "Title", "Tile Map Service");
                element(writer, "Abstract", "A Tile Map Service served by GeoWebCache");
            }
            {
                writer.writeStartElement("TileMaps");
                tileMapInfos.forEach(info -> writeTileMap(writer, info));
                writer.writeEndElement();
            }
            writer.writeEndElement(); // TileMapService
            writer.writeEndDocument();
        } finally {
            writer.flush();
        }
    }

    public void writeTileMapResource(Optional<TileMap> info, OutputStream out)
            throws XMLStreamException, FactoryConfigurationError, IOException {
        XMLStreamWriter writer = newWriter(out);
        if (!info.isPresent()) {
            writeError(writer, "The requested TileMap does not exist");
            return;
        }
        TileMap tileMap = info.get();
        writer.writeStartDocument();
        startElement(writer, "TileMap", "version", "1.0.0", "tilemapservice", tms1_0_0_Url());
        {
            element(writer, "Title", tileMap.getTitle());
            element(writer, "Abstract", tileMap.getAbstract());
            element(writer, "SRS", tileMap.getSrs());
            BoundingBox b = tileMap.getBoundingBox();
            CoordinateFormatter cw = new CoordinateFormatter(15);
            element(
                    writer,
                    "BoundingBox",
                    "minx",
                    cw.format(b.getMinX()),
                    "miny",
                    cw.format(b.getMinY()),
                    "maxx",
                    cw.format(b.getMaxX()),
                    "maxy",
                    cw.format(b.getMaxY()));
            element(
                    writer,
                    "Origin",
                    "x",
                    cw.format(tileMap.getOriginX()),
                    "y",
                    cw.format(tileMap.getOriginY()));
            element(
                    writer,
                    "TileFormat",
                    "width",
                    String.valueOf(tileMap.getTileWidth()),
                    "height",
                    String.valueOf(tileMap.getTileHeight()),
                    "mime-type",
                    tileMap.getMimeType(),
                    "extension",
                    tileMap.getFileNameExtension());

            writeTileSets(writer, tileMap);
        }
        writer.writeEndElement();
        writer.writeEndDocument();
    }

    private void writeTileSets(XMLStreamWriter writer, TileMap tileMap) throws XMLStreamException {
        startElement(writer, "TileSets", "profile", "local");
        {
            final String tileMapUrl =
                    String.format("%s/%s", tms1_0_0_Url(), tileMap.getIdentifier());
            final Comparator<? super TileSet> comparator =
                    (ts1, ts2) -> Integer.compare(ts1.getOrder(), ts2.getOrder());
            try (Stream<TileSet> tilesets = this.service.findTileSets(tileMap).sorted(comparator)) {
                tilesets.forEach(
                        ts -> {
                            try {
                                this.writeTileSet(writer, tileMapUrl, ts);
                            } catch (XMLStreamException e) {
                                throw new RuntimeException(e);
                            }
                        });
            }
        }
        writer.writeEndElement();
    }

    private void writeTileSet(XMLStreamWriter writer, String tileMapUrl, TileSet ts)
            throws XMLStreamException {
        String href = String.format("%s/%s", tileMapUrl, ts.getName());
        String unitsPerPixel = Double.toString(ts.getUnitsPerPixel());
        String order = Integer.toString(ts.getOrder());
        String elementValue = null;
        element(
                writer,
                "TileSet",
                elementValue,
                "href",
                href,
                "units-per-pixel",
                unitsPerPixel,
                "order",
                order);
    }

    private void writeError(XMLStreamWriter writer, String message) throws XMLStreamException {
        writer.writeStartDocument();
        writer.writeStartElement("TileMapServerErrror");
        {
            element(writer, "Message", message);
        }
        writer.writeEndElement();
        writer.writeEndDocument();
    }

    private XMLStreamWriter newWriter(OutputStream out)
            throws XMLStreamException, FactoryConfigurationError, IOException {
        return XMLOutputFactory.newInstance().createXMLStreamWriter(out, "UTF-8");
    }

    private void writeTileMap(XMLStreamWriter writer, TileMapInfo info) {
        try {
            final String value = null;
            String title = info.getTitle();
            String srs = info.getSrs();
            String profile = info.getProfile();
            String href = tileMapUrl(info);
            element(
                    writer, "TileMap", value, "title", title, "srs", srs, "profile", profile,
                    "href", href);
        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
    }

    private void startElement(XMLStreamWriter writer, String name, String... attributesKvp)
            throws XMLStreamException {
        writer.writeStartElement(name);
        attribtues(writer, attributesKvp);
    }

    private void element(XMLStreamWriter writer, String name, String value, String... attributesKvp)
            throws XMLStreamException {
        startElement(writer, name, attributesKvp);
        if (value != null) {
            writer.writeCharacters(value);
        }
        writer.writeEndElement();
    }

    protected void attribtues(XMLStreamWriter writer, String... attributesKvp)
            throws XMLStreamException {
        if (null != attributesKvp) {
            for (int i = 0; i < attributesKvp.length - 1; i += 2) {
                String att = attributesKvp[i];
                String attValue = attributesKvp[i + 1];
                writer.writeAttribute(att, attValue);
            }
        }
    }

    private String baseTmsUrl() {
        HttpServletRequest request = this.currentRequest;
        String baseUrl = ResponseUtils.baseURL(request);
        String path = "/service/tms";
        Map<String, String> kvp = null;
        return ResponseUtils.buildURL(baseUrl, path, kvp, URLType.SERVICE);
    }

    private String tms1_0_0_Url() {
        return String.format("%s/%s", baseTmsUrl(), "1.0.0");
    }

    private String tileMapUrl(TileMapInfo info) {
        String baseTmsUrl = tms1_0_0_Url();
        String tileMapId = info.getIdentifier();
        return String.format("%s/%s", baseTmsUrl, tileMapId);
    }
}
