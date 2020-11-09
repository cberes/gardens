package com.ohboywerecamping.domain;

import java.time.ZonedDateTime;

public class Gardener {
    private String id;
    private String email;
    private ZonedDateTime joined;

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    public ZonedDateTime getJoined() {
        return joined;
    }

    public void setJoined(final ZonedDateTime joined) {
        this.joined = joined;
    }
}
