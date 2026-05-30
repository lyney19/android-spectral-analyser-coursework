package com.mirea.kt.ribo.notescope.network.model;

import com.mirea.kt.ribo.notescope.network.body.ContentType;

public record ApiBody(
        ContentType bodyType,
        Object body
) {
}
