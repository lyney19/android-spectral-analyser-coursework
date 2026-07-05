package io.lyney.notescope.network.model;

import io.lyney.notescope.network.body.ContentType;

public record ApiBody(
        ContentType bodyType,
        Object body
) {
}
