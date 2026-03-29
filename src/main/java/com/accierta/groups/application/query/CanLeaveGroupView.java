package com.accierta.groups.application.query;

import java.util.List;
import java.util.UUID;

public record CanLeaveGroupView(
        boolean canLeave,
        List<BlockingPorraView> blockingPorras) {

    public record BlockingPorraView(
            UUID porraId,
            String title,
            String status) {
    }
}
