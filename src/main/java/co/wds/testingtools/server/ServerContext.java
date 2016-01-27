package co.wds.testingtools.server;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

//TODO this is a copy-paste from demo-wds. Put it into testing-tools and reuse
public class ServerContext {
    private File baseDir;
    Map<String, String> params = new HashMap<String, String>();
    
    public File getBaseDir() {
        return baseDir;
    }

    public void setBaseDir(File baseDir) {
        System.out.println("setBaseDir(" + baseDir + ")");
        if (!baseDir.exists() || !baseDir.isDirectory()) {
            throw new IllegalArgumentException("Base directory for test resources: '" + baseDir + "' does not exist or is not a directory!");
        }
        this.baseDir = baseDir;
    }
    
    public void setParam(String name, String value) {
        this.params.put(name, value);
    }
    
    public Map<String, String> getParams() {
        return params;
    }
}
