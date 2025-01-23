


## TODO:

- Set maintenance mode during backup/restore. GlobalLock may not be enough due to lock timeouts?
- Improve `nameExcludes` to filter Resources, either use regex and/or make them configurable
- Filter out service xml files (e.g. wms.xml, wfs.xml, etc.)
- header LocalDateTime correct encoding and parsing
- Ignore default styles when exporting, they can't be modified?
- Reload() after restore