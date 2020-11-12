package com.alibaba.csp.sentinel.dashboard.rule.nacos;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.DegradeRuleEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.FlowRuleEntity;
import com.alibaba.csp.sentinel.util.AssertUtil;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.api.config.ConfigType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 代理nacos规则数据源处理
 */
@Component
public class DelegateRuleNacosStore {
    
    private final Map<Class, String> NACOS_DATA_ID_POSTFIX_MAP = new HashMap<>();
    
    @Autowired
    private NacosConfigCustomService configService;
    
    public <T> List<T> getRules(String appName, Class<T> tClass) throws Exception {
        String rules = configService.getConfig(appName + NACOS_DATA_ID_POSTFIX_MAP.get(tClass),
                NacosConfigUtil.GROUP_ID, 3000);
        if (StringUtil.isEmpty(rules)) {
            return new ArrayList<>();
        }
        return ruleDecoder(rules, tClass);
    }
    
    public <T> void publish(String app, List<T> rules, Class<T> tClass) throws Exception {
        AssertUtil.notEmpty(app, "app name cannot be empty");
        if (rules == null) {
            return;
        }
        configService.publishConfig(app + NACOS_DATA_ID_POSTFIX_MAP.get(tClass),
                NacosConfigUtil.GROUP_ID, ruleEncoder(rules, tClass), ConfigType.JSON);
    }
    
    private <T> String ruleEncoder(List<T> ruleList, Class<T> tClass) {
        return JSON.toJSONString(ruleList);
    }
    
    private <T> List<T> ruleDecoder(String ruleStr, Class<T> tClass) {
        return JSON.parseArray(ruleStr, tClass);
    }
    
    @PostConstruct
    public void init() {
        NACOS_DATA_ID_POSTFIX_MAP.put(FlowRuleEntity.class, "-flow-rules.json");
        NACOS_DATA_ID_POSTFIX_MAP.put(DegradeRuleEntity.class, "-degrade-rules.json");
    }
}
