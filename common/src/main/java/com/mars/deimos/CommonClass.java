package com.mars.deimos;

import com.mars.deimos.platform.Services;
import com.mars.deimos.platform.services.IPlatformHelper;

public class CommonClass {
    public static final IPlatformHelper PLATFORM = Services.load(IPlatformHelper.class);
    public static void init() {}
}
