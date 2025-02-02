package com.mars.deimos.platform.services;

import java.nio.file.Path;

public interface IPlatformHelper {
    Path getConfigDirectory();

    boolean isClientEnv();
}
