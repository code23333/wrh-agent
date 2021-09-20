package self.wrh.agent;

public abstract class AgentArgs {

    private String classNameStart;

    private boolean metricConsoleOut;

    public boolean isMetricConsoleOut() {
        return metricConsoleOut;
    }

    public String getClassNameStart() {
        return classNameStart;
    }



    public static AgentArgs parseOf(String args){
        AgentArgs agentArgs = new AgentArgs() {
        };
        for (String kv : args.split("&")){
            String[] kv0 = kv.split("=");
            if (kv0.length != 2) continue;
            if ("classNameStart".equals(kv0[0]))
                agentArgs.classNameStart = kv0[1];
            else if ("metricConsoleOut".equals(kv0[0]))
                agentArgs.metricConsoleOut = Boolean.parseBoolean(kv0[1]);
        }
        return agentArgs;
    }
}
