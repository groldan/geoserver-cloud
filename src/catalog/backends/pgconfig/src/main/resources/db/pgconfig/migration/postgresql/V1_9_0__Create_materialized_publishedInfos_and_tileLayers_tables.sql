/*
 * Create tables publishedinfos_mat and tilelayers_mat
 */
DROP TABLE IF EXISTS publishedinfos_mat;
DROP TABLE IF EXISTS tilelayers_mat;

SELECT * INTO publishedinfos_mat FROM publishedinfos;
SELECT * INTO tilelayers_mat FROM tilelayers;

CREATE INDEX publishedinfos_mat_id_idx ON publishedinfos_mat ("id");
CREATE INDEX publishedinfos_mat_infotype_idx ON publishedinfos_mat ("@type"); 
CREATE INDEX publishedinfos_mat_name_idx ON publishedinfos_mat ("name"); 
CREATE INDEX publishedinfos_mat_prefixedname_idx ON publishedinfos_mat ("prefixedName"); 
CREATE INDEX publishedinfos_mat_title_idx ON publishedinfos_mat ("title"); 
CREATE INDEX publishedinfos_mat_enabled_idx ON publishedinfos_mat ("enabled"); 
CREATE INDEX publishedinfos_mat_advertised_idx ON publishedinfos_mat ("advertised"); 
CREATE INDEX publishedinfos_mat_type_idx ON publishedinfos_mat ("type"); 
CREATE INDEX publishedinfos_mat_resource_id_idx ON publishedinfos_mat ("resource.id"); 
CREATE INDEX publishedinfos_mat_resource_name_idx ON publishedinfos_mat ("resource.name"); 
CREATE INDEX publishedinfos_mat_resource_enabled_idx ON publishedinfos_mat ("resource.enabled"); 
CREATE INDEX publishedinfos_mat_resource_advertised_idx ON publishedinfos_mat ("resource.advertised"); 
CREATE INDEX publishedinfos_mat_resource_srs_idx ON publishedinfos_mat ("resource.SRS"); 
CREATE INDEX publishedinfos_mat_resource_store_id_idx ON publishedinfos_mat ("resource.store.id"); 
CREATE INDEX publishedinfos_mat_resource_store_name_idx ON publishedinfos_mat ("resource.store.name"); 
CREATE INDEX publishedinfos_mat_resource_store_enabled_idx ON publishedinfos_mat ("resource.store.enabled"); 
CREATE INDEX publishedinfos_mat_resource_store_type_idx ON publishedinfos_mat ("resource.store.type"); 
CREATE INDEX publishedinfos_mat_resource_store_workspace_id_idx ON publishedinfos_mat ("resource.store.workspace.id"); 
CREATE INDEX publishedinfos_mat_resource_store_workspace_name_idx ON publishedinfos_mat ("resource.store.workspace.name"); 
CREATE INDEX publishedinfos_mat_resource_namespace_id_idx ON publishedinfos_mat ("resource.namespace.id"); 
CREATE INDEX publishedinfos_mat_resource_namespace_prefix_idx ON publishedinfos_mat ("resource.namespace.prefix"); 
CREATE INDEX publishedinfos_mat_defaultstyle_id_idx ON publishedinfos_mat ("defaultStyle.id"); 
CREATE INDEX publishedinfos_mat_defaultstyle_name_idx ON publishedinfos_mat ("defaultStyle.name"); 
CREATE INDEX publishedinfos_mat_defaultstyle_filename_idx ON publishedinfos_mat ("defaultStyle.filename"); 
CREATE INDEX publishedinfos_mat_defaultstyle_format_idx ON publishedinfos_mat ("defaultStyle.format"); 
CREATE INDEX publishedinfos_mat_mode_idx ON publishedinfos_mat ("mode"); 
CREATE INDEX publishedinfos_mat_workspace_id_idx ON publishedinfos_mat ("workspace.id"); 
CREATE INDEX publishedinfos_mat_workspace_name_idx ON publishedinfos_mat ("workspace.name"); 
CREATE INDEX publishedinfos_mat_styles_id_idx ON publishedinfos_mat USING GIN ("styles.id"); 
CREATE INDEX publishedinfos_mat_layers_id_idx ON publishedinfos_mat USING GIN ("layers.id");


CREATE INDEX tilelayers_mat_id_idx ON tilelayers_mat ("id");
CREATE INDEX tilelayers_mat_infotype_idx ON tilelayers_mat ("@type");
CREATE INDEX tilelayers_mat_name_idx ON tilelayers_mat ("name");
CREATE INDEX tilelayers_mat_enabled_idx ON tilelayers_mat ("enabled");
CREATE INDEX tilelayers_mat_advertised_idx ON tilelayers_mat ("advertised");
CREATE INDEX tilelayers_mat_type_idx ON tilelayers_mat ("type");
CREATE INDEX tilelayers_mat_workspace_name_idx ON tilelayers_mat ("workspace.name");
CREATE INDEX tilelayers_mat_published_name_idx ON tilelayers_mat ("published.name");