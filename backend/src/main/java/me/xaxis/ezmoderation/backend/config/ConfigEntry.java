package me.xaxis.ezmoderation.backend.config;

public record ConfigEntry<T>(String path, Class<T> value) {}
