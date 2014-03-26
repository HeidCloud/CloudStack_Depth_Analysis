CloudStack深度剖析
==============

#存储
- Fuse http://fuse.sourceforge.net/
- 主存储无法删除解决方法:到数据库找volumes表，找到相关volume 处于expunging 状态，表示已经删除，但remove字段是NULL，说明有问题，在删除后，removed应该有删除的时间，而不是为null.
```
mysql>use cloud;
mysql>update storage_pool set removed=now() where pod_id=5;
mysql>update volumes set removed=now() where pod_id=5;
```
4.3版本已经解决此bug   
https://issues.apache.org/jira/browse/CLOUDSTACK-4697   
https://issues.apache.org/jira/browse/CLOUDSTACK-4402   
#网络

#模板

#区域

#提供点

#集群

#主机

#虚拟机
- KVM 做虚拟化程序，全局参数： kvm.snapshot.enabled  设置为true后 才能做快照，且不支持实例的快照，只支持卷的快照 
- 修改虚拟机操作系统用户名，通过修改数据库来修改实例的名字：比如把uuid修改为 test

#系统路由

#vpc
  通过域来实现资源的隔离，操作顺序是:先建立多个域，然后再创建用户的时候，选择不同的域。使之用户级别不同（admin/user）
  
#高可用
  1.全局变量 ha.tag
  2.计算方案，ha标签
  3.主机ha 标签
