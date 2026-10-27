package com.cloudpulse.dto;

import jakarta.validation.constraints.NotBlank;

public class IncidentActionDto {

    @NotBlank(message = "Operator/User name is required")
    private String user;

    private String rootCauseNotes;

    public IncidentActionDto() {
    }

    public IncidentActionDto(String user, String rootCauseNotes) {
        this.user = user;
        this.rootCauseNotes = rootCauseNotes;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getRootCauseNotes() {
        return rootCauseNotes;
    }

    public void setRootCauseNotes(String rootCauseNotes) {
        this.rootCauseNotes = rootCauseNotes;
    }
}
