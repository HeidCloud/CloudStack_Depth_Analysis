-- vcenter 表
create table T_HD_CLOUD_VMWARE_DATA_CENTER
(
  vmware_data_center_id NUMBER(10) not null,
  data_center_name      VARCHAR2(100),
  enable_flg            VARCHAR2(1),
  cloudplatform_id      NUMBER(10)
)
tablespace DATAHEIDCLOUD
  pctfree 10
  initrans 1
  maxtrans 255;
-- Create/Recreate primary, unique and foreign key constraints 
alter table T_HD_CLOUD_VMWARE_DATA_CENTER
  add constraint PK_CLOUD_VMWARE_DATA_CENTER primary key (VMWARE_DATA_CENTER_ID)
  using index 
  tablespace DATAHEIDCLOUD
  pctfree 10
  initrans 2
  maxtrans 255;
alter table T_HD_CLOUD_VMWARE_DATA_CENTER
  add constraint FK_CLOUD_VMWARE_DATA_CENTER foreign key (CLOUDPLATFORM_ID)
  references T_HD_CLOUDPLATFORM (CLOUDPLATFORM_ID);
  
-- vcenter 与 cloudstack 集群关系映射表
create table T_HD_VMWARE_CS_CLUSTER_MAP
(
  vmware_cs_cluster_map_id NUMBER(10) not null,
  vmware_data_center_id    NUMBER(10),
  pod_id                   NUMBER(10)
)
tablespace DATAHEIDCLOUD
  pctfree 10
  initrans 1
  maxtrans 255;
-- Create/Recreate primary, unique and foreign key constraints 
alter table T_HD_VMWARE_CS_CLUSTER_MAP
  add constraint PK_VMWARE_CS_CLUSTER_MAP primary key (VMWARE_CS_CLUSTER_MAP_ID)
  using index 
  tablespace DATAHEIDCLOUD
  pctfree 10
  initrans 2
  maxtrans 255;
alter table T_HD_VMWARE_CS_CLUSTER_MAP
  add constraint FK_VMWARE_CS_CLUSTER_POD foreign key (POD_ID)
  references T_HD_CLOUD_POD (POD_ID);
  
  
  
-- vcenter 与 cloudstack 数据中心关系映射表
create table T_HD_VMWARE_CS_DC_MAP
(
  vmware_cs_dc_map_id   NUMBER(10) not null,
  zone_id               NUMBER(10),
  vmware_data_center_id NUMBER(10),
  cloudplatform_id      NUMBER(10)
)
tablespace DATAHEIDCLOUD
  pctfree 10
  initrans 1
  maxtrans 255;
-- Create/Recreate primary, unique and foreign key constraints 
alter table T_HD_VMWARE_CS_DC_MAP
  add constraint PK_VMWARE_CS_DC_MAP primary key (VMWARE_CS_DC_MAP_ID)
  using index 
  tablespace DATAHEIDCLOUD
  pctfree 10
  initrans 2
  maxtrans 255;
alter table T_HD_VMWARE_CS_DC_MAP
  add constraint FK_VMWARE_CS_DC_MAP_PLATFORM foreign key (CLOUDPLATFORM_ID)
  references T_HD_CLOUDPLATFORM (CLOUDPLATFORM_ID);
alter table T_HD_VMWARE_CS_DC_MAP
  add constraint FK_VMWARE_CS_DC_MAP_ZONE foreign key (ZONE_ID)
  references T_HD_CLOUD_ZONE (ZONE_ID);
  
--快照表
create table T_CC_VM_SNAPSHOT
(
  vm_snapshot_id    NUMBER(10) not null,
  vm_snapshot_name  VARCHAR2(100),
  vm_snapshot_date  DATE,
  vm_snapshot_desc  VARCHAR2(100),
  apply_resource_id NUMBER(10),
  enable_flg        VARCHAR2(1),
  vm_snapshot_uuid  VARCHAR2(100)
)
tablespace DATACLOUDPOC
  pctfree 10
  initrans 1
  maxtrans 255;
-- Create/Recreate primary, unique and foreign key constraints 
alter table T_CC_VM_SNAPSHOT
  add constraint PK_VM_SNAPSHOT_ID primary key (VM_SNAPSHOT_ID)
  using index 
  tablespace DATACLOUDPOC
  pctfree 10
  initrans 2
  maxtrans 255;
alter table T_CC_VM_SNAPSHOT
  add constraint FK_APPLY_RESOURCE_ID foreign key (APPLY_RESOURCE_ID)
  references T_CC_APPLYED_HOSTINFO (APPLY_RESOURCE_ID);
