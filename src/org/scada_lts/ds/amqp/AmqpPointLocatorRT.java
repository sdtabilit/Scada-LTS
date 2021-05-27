package org.scada_lts.ds.amqp;

import com.serotonin.mango.rt.dataImage.types.MangoValue;
import com.serotonin.mango.rt.dataSource.PointLocatorRT;

public class AmqpPointLocatorRT extends PointLocatorRT {
    private final AmqpPointLocatorVO vo;
    private String queueName;
    private String exchangeName;
    private String routingKey;
    private MangoValue currentValue;

    public AmqpPointLocatorRT(AmqpPointLocatorVO vo){
        this.vo = vo;
        this.queueName = vo.getQueueName();
        this.exchangeName = vo.getExchangeName();
        this.routingKey = vo.getRoutingKey();
    }

    public AmqpPointLocatorVO getVO() {return vo;}

    @Override
    public boolean isSettable() {
        return vo.isSettable();
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    public String getQueueName() {
        return this.queueName;
    }

    public String getExchangeName() {
        return exchangeName;
    }

    public void setExchangeName(String exchangeName) {
        this.exchangeName = exchangeName;
    }

    public String getRoutingKey() {
        return routingKey;
    }

    public void setRoutingKey(String routingKey) {
        this.routingKey = routingKey;
    }

    public void setCurrentValue(MangoValue currentValue) {
        this.currentValue = currentValue;
    }

    public MangoValue getCurrentValue() {
        return this.currentValue;
    }
}
