/*这段代码解决当windows使用other pv时，始终没有指定为rtc导致windows变慢的问题
plugins/hypervisors/kvm/src/com/cloud/hypervisor/kvm/resource/LibvirtComputingResource.java

        ClockDef clock = new ClockDef();
        if (vmTO.getOs().startsWith("Windows")) {
            clock.setClockOffset(ClockDef.ClockOffset.LOCALTIME);
            clock.setTimer("rtc", "catchup", null);
        } else{
            clock.setTimer("rtc", "catchup", null);
        }
这段代码解决vm挂载nfs时，读写速率只有10MB/s的问题，也就是取消了kvm的vnet限速。
plugins/hypervisors/kvm/src/com/cloud/hypervisor/kvm/resource/LibvirtVMDef.java

        public void defBridgeNet(String brName, String targetBrName,
                String macAddr, nicModel model, Integer networkRateKBps) {
            _netType = guestNetType.BRIDGE;
            _sourceName = brName;
            _networkName = targetBrName;
            _macAddr = macAddr;
            _model = model;
            _networkRateKBps = 0;
        public void defPrivateNet(String networkName, String targetName,
                String macAddr, nicModel model, Integer networkRateKBps) {
            _netType = guestNetType.NETWORK;
            _sourceName = networkName;
            _networkName = targetName;
            _macAddr = macAddr;
            _model = model;
            _networkRateKBps = 0;

这段代码解决了当host当机以后，server无法侦测到，导致ha失败的问题
备注，这段代码修改后，当host当机以后，server检测到host当机会跑出一个异常。
异常大意是说，不能获取一个状态为down的host的agent的状态。事实上，当检测到
host状态为down时，就应该放弃检测agent状态，因此这个异常并不影响ha的正常工作。只是处理这个异常，代码修改量就大了，本身就方案这样的多层次的调用的代码，看不懂啊。相关的类太多。都找不到哪里调用。
修改了一下ha机制，当主机down掉的时候，挂起ha进程，连续测试4次
，如果4次都测试失败，则认为主机down了。
cloudstack/server/src/com/cloud/ha/AbstractInvestigatorImpl.java
*/

// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.
package com.cloud.ha;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;


import javax.inject.Inject;
import javax.naming.ConfigurationException;

import org.apache.log4j.Logger;

import com.cloud.agent.AgentManager;
import com.cloud.agent.api.Answer;
import com.cloud.agent.api.PingTestCommand;
import com.cloud.exception.AgentUnavailableException;
import com.cloud.exception.OperationTimedoutException;
import com.cloud.host.Host.Type;
import com.cloud.host.HostVO;
import com.cloud.host.Status;
import com.cloud.host.dao.HostDao;
import com.cloud.resource.ResourceManager;
import com.cloud.utils.component.AdapterBase;
import com.cloud.utils.db.SearchCriteria2;
import com.cloud.utils.db.SearchCriteria.Op;
import com.cloud.utils.db.SearchCriteriaService;
import java.util.logging.Level;

public abstract class AbstractInvestigatorImpl extends AdapterBase implements Investigator {
    private static final Logger s_logger = Logger.getLogger(AbstractInvestigatorImpl.class);

    @Inject private HostDao _hostDao = null;
    @Inject private AgentManager _agentMgr = null;
    @Inject private ResourceManager _resourceMgr = null;


    @Override
    public boolean configure(String name, Map<String, Object> params) throws ConfigurationException {

        return true;
    }

    @Override
    public boolean start() {
        return true;
    }

    @Override
    public boolean stop() {
        return true;
    }
    
    // Host.status is up and Host.type is routing
    protected List<Long> findHostByPod(long podId, Long excludeHostId) {
    	SearchCriteriaService<HostVO, HostVO> sc = SearchCriteria2.create(HostVO.class);
        sc.addAnd(sc.getEntity().getType(), Op.EQ, Type.Routing);
        sc.addAnd(sc.getEntity().getPodId(), Op.EQ, podId);
        sc.addAnd(sc.getEntity().getStatus(), Op.EQ, Status.Up);
        List<HostVO> hosts = sc.list();
        
        List<Long> hostIds = new ArrayList<Long>(hosts.size());
        for (HostVO h : hosts) {
        	hostIds.add(h.getId());
        }
        
        if (excludeHostId != null) {
            hostIds.remove(excludeHostId);
        }
        
        return hostIds;
    }

       protected Status testIpAddress(Long hostId, String testHostIp) {
       int testCount=30;
 
       for(int i=0;i<testCount;i++){
         switch(testHostIpTest(hostId,testHostIp)){
             case 0:{
                        return null;
                    }
               
             case 1:{
                        return Status.Up;
                    }
             case 2: {
                 if(i==testCount-1){
                         return Status.Down;
                     }
                }             
             }           
          }      
        return null;
    }
    
    protected int testHostIpTest(Long hostId,String testHostIp){
           try {
            Answer pingTestAnswer = _agentMgr.send(hostId, new PingTestCommand(testHostIp));
            if(pingTestAnswer == null) {
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("host (" + testHostIp + ") returns null answer");
                }
            	return 0;
            }
            
            if (pingTestAnswer.getResult()) {
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("host (" + testHostIp + ") has been successfully pinged, returning that host is up");
                }
                // computing host is available, but could not reach agent, return false
                return 1;
            } else {
                if (s_logger.isDebugEnabled()) {
                    s_logger.debug("host (" + testHostIp + ") cannot be pinged, returning null ('I don't know')");
                }
                return 2;
            }
        } catch (AgentUnavailableException e) {
            return 0;
        } catch (OperationTimedoutException e) {
            return 0;
        }
    }
}
