package dev.sayaya.handbook.client.log;

import com.google.gwt.core.client.EntryPoint;
import dev.sayaya.rx.Observable;
import elemental2.dom.CSSProperties;
import elemental2.dom.DomGlobal;

import static elemental2.dom.DomGlobal.setTimeout;
import static org.jboss.elemento.Elements.body;

public class Application implements EntryPoint {
    private final Component components = DaggerComponent.create();
    @Override
    public void onModuleLoad() {
        body().add(components.console().style("height: 0rem;"));
        setTimeout(e-> components.console().element().style.height = CSSProperties.HeightUnionType.of("20rem"), 0);
        var logs = LOGS.split("\n");
        Observable.timer(0, 1000).map(i->{
            DomGlobal.console.log(logs[i]);
            return logs[i];
        }).take(logs.length).subscribe(components.log()::next);

    }

    private final static String LOGS = """
            오전 2:32:29: 실행 중 'gwtDevMode'…
            
            > Task :login-ui:checkKotlinGradlePluginConfigurationErrors SKIPPED
            > Task :login-ui:copyResources UP-TO-DATE
            > Task :login-ui:processResources
            > Task :activity:compileJava UP-TO-DATE
            > Task :activity:processResources UP-TO-DATE
            > Task :activity:classes UP-TO-DATE
            > Task :activity:jar UP-TO-DATE
            > Task :login-ui:compileKotlin NO-SOURCE 
            > Task :login-ui:compileJava           
            > Task :login-ui:classes
            > Task :login-ui:jar SKIPPED
            > Task :login-ui:compileTestKotlin NO-SOURCE 
            > Task :login-ui:compileTestJava
            > Task :login-ui:gwtDevMode
            Super Dev Mode starting up
            2025-04-16 02:32:44.893:INFO::main: Logging initialized @4910ms to org.eclipse.jetty.util.log.StdErrLog
               Loading Java files in dev.sayaya.handbook.LoginTest.
               Module setup completed in 77502 ms
               Loading Java files in dev.sayaya.handbook.LogTest.
               Module setup completed in 2059 ms
            2025-04-16 02:34:02.360:INFO:oejs.Server:main: jetty-9.4.44.v20210927; built: 2021-09-27T23:02:44.612Z; git: 8da83308eeca865e495e53ef315a249d63ba9332; jvm 21.0.2+13-58
            2025-04-16 02:34:02.400:INFO:oejsh.ContextHandler:main: Started o.e.j.s.ServletContextHandler@64b9fe30{/,null,AVAILABLE}
            2025-04-16 02:34:02.502:INFO:oejs.AbstractConnector:main: Started ServerConnector@253311f3{HTTP/1.1, (http/1.1)}{127.0.0.1:9876}
            2025-04-16 02:34:02.504:INFO:oejs.Server:main: Started @82521ms
            
            The code server is ready at http://127.0.0.1:9876/
            """;
}
