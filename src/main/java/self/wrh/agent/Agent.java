package self.wrh.agent;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.agent.builder.ResettableClassFileTransformer;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.utility.JavaModule;
import self.wrh.agent.interceptors.CostInterceptor;
import self.wrh.agent.metric.Metric;

import java.lang.instrument.Instrumentation;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Agent {


    public static void premain(String args, Instrumentation instrumentation){
        System.err.println("wrh agent args string: " + args);
        AgentArgs agentArgs = AgentArgs.parseOf(args);
        AgentBuilder.Transformer transformer = new AgentBuilder.Transformer() {
            @Override
            public DynamicType.Builder<?> transform(DynamicType.Builder<?> builder,
                                                    TypeDescription typeDescription,
                                                    ClassLoader classLoader) {
                return builder
                        // 拦截任意方法
                        .method(ElementMatchers.<MethodDescription>any())
                        // 指定方法拦截器，此拦截器中做具体的操作
                        .intercept(MethodDelegation.to(CostInterceptor.class));
            }
        };

        AgentBuilder.Listener listener = new AgentBuilder.Listener() {
            @Override
            public void onTransformation(TypeDescription typeDescription, ClassLoader classLoader, JavaModule module, DynamicType dynamicType) {}

            @Override
            public void onIgnored(TypeDescription typeDescription, ClassLoader classLoader, JavaModule module) { }

            @Override
            public void onError(String typeName, ClassLoader classLoader, JavaModule module, Throwable throwable) { }

            @Override
            public void onComplete(String typeName, ClassLoader classLoader, JavaModule module) { }
        };

        ResettableClassFileTransformer resettableClassFileTransformer = new AgentBuilder
                .Default()
                // 指定需要拦截的类
                .type(ElementMatchers.nameStartsWith(agentArgs.getClassNameStart()))
                .transform(transformer)
                .with(listener)
                .installOn(instrumentation);

        if (agentArgs.isMetricConsoleOut()){
            //每隔5秒打印JVM内存和GC信息
            Executors.newScheduledThreadPool(1).scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    Metric.printMemoryInfo();
                    Metric.printGCInfo();
                }
            }, 0, 5000, TimeUnit.MILLISECONDS);
        }
    }
}
