package com.accierta.groups.application.query;

public record InvitationInfoView(
        String groupName,
        String invitedBy,
        boolean expired,
        boolean used) {
}
