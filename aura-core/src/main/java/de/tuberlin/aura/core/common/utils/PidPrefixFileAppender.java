package de.tuberlin.aura.core.common.utils;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;

import org.apache.log4j.FileAppender;

public class PidPrefixFileAppender extends FileAppender {

    @Override
    public void setFile(String file) {
        RuntimeMXBean rt = ManagementFactory.getRuntimeMXBean();
        String pid = rt.getName().substring(0, rt.getName().indexOf("@"));

        super.setFile(file + "-" + pid);
    }

}
