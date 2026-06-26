package com.restaurant.menu.service;

import com.restaurant.menu.dto.OrderWindowConfigReq;
import com.restaurant.menu.dto.OrderWindowConfigResp;
import com.restaurant.menu.entity.OrderWindowConfig;
import com.restaurant.menu.repository.OrderWindowConfigRepository;
import com.restaurant.shared.exception.OrderWindowClosedException;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderWindowService {

    private final OrderWindowConfigRepository configRepository;
    private final String defaultStart;
    private final String defaultEnd;
    private final String defaultZone;

    // volatile — read by every menu request, written only on admin save
    private volatile LocalTime start1;
    private volatile LocalTime end1;
    private volatile LocalTime start2;
    private volatile LocalTime end2;
    private volatile ZoneId   zoneId;
    private volatile boolean  enforced;

    public OrderWindowService(
            OrderWindowConfigRepository configRepository,
            @Value("${canteen.order.start:11:00}") String start,
            @Value("${canteen.order.end:14:30}")   String end,
            @Value("${canteen.order.zone:Asia/Hong_Kong}") String zone) {
        this.configRepository = configRepository;
        this.defaultStart = start;
        this.defaultEnd   = end;
        this.defaultZone  = zone;
        applyDefaults();
    }

    @PostConstruct
    public void loadFromDb() {
        configRepository.findById(1L).ifPresent(this::applyEntity);
    }

    public boolean isOpen() {
        if (!enforced) return true;
        LocalTime now = LocalTime.now(zoneId);
        boolean inSlot1 = !now.isBefore(start1) && !now.isAfter(end1);
        boolean inSlot2 = start2 != null && end2 != null
                && !now.isBefore(start2) && !now.isAfter(end2);
        return inSlot1 || inSlot2;
    }

    public void assertOpen() {
        if (!isOpen()) throw new OrderWindowClosedException(
                "訂購時段已關閉，開放時段：" + start1 + "–" + end1);
    }

    @Transactional
    public OrderWindowConfigResp updateConfig(OrderWindowConfigReq req) {
        OrderWindowConfigReq.SlotReq s1 = req.slots().get(0);
        OrderWindowConfigReq.SlotReq s2 = req.slots().size() > 1 ? req.slots().get(1) : null;

        OrderWindowConfig entity = configRepository.findById(1L)
                .orElse(new OrderWindowConfig());
        entity.setId(1L);
        entity.setSlot1Start(s1.start());
        entity.setSlot1End(s1.end());
        entity.setSlot2Start(s2 != null ? s2.start() : null);
        entity.setSlot2End(s2 != null ? s2.end()   : null);
        entity.setZoneId(hasValue(req.zone()) ? req.zone() : defaultZone);
        entity.setEnforced(req.enforced());
        configRepository.save(entity);
        applyEntity(entity);
        return toResp(entity);
    }

    public OrderWindowConfigResp getConfig() {
        return configRepository.findById(1L)
                .map(this::toResp)
                .orElseGet(this::defaultResp);
    }

    public ZoneId getZoneId() { return zoneId; }

    // ── private helpers ──────────────────────────────

    private void applyDefaults() {
        start1   = LocalTime.parse(defaultStart);
        end1     = LocalTime.parse(defaultEnd);
        start2   = null;
        end2     = null;
        zoneId   = ZoneId.of(defaultZone);
        enforced = true;
    }

    private void applyEntity(OrderWindowConfig c) {
        start1   = LocalTime.parse(c.getSlot1Start());
        end1     = LocalTime.parse(c.getSlot1End());
        start2   = hasValue(c.getSlot2Start()) ? LocalTime.parse(c.getSlot2Start()) : null;
        end2     = hasValue(c.getSlot2End())   ? LocalTime.parse(c.getSlot2End())   : null;
        zoneId   = ZoneId.of(hasValue(c.getZoneId()) ? c.getZoneId() : defaultZone);
        enforced = c.isEnforced();
    }

    private OrderWindowConfigResp toResp(OrderWindowConfig c) {
        List<OrderWindowConfigResp.SlotResp> slots = new ArrayList<>();
        slots.add(new OrderWindowConfigResp.SlotResp(c.getSlot1Start(), c.getSlot1End()));
        if (hasValue(c.getSlot2Start()) && hasValue(c.getSlot2End())) {
            slots.add(new OrderWindowConfigResp.SlotResp(c.getSlot2Start(), c.getSlot2End()));
        }
        return new OrderWindowConfigResp(slots, c.getZoneId(), c.isEnforced());
    }

    private OrderWindowConfigResp defaultResp() {
        return new OrderWindowConfigResp(
                List.of(new OrderWindowConfigResp.SlotResp(defaultStart, defaultEnd)),
                defaultZone, true);
    }

    private static boolean hasValue(String s) {
        return s != null && !s.isBlank();
    }
}
