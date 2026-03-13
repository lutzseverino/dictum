package dev.dictum.api.content.model.vo;

public record PostPatchFields(
    boolean title,
    boolean excerpt,
    boolean template,
    boolean tags,
    boolean body,
    boolean stylesheet,
    boolean removeStylesheet) {}
