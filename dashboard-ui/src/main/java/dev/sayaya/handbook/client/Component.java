package dev.sayaya.handbook.client;

import dev.sayaya.handbook.client.interfaces.api.ApiModule;
import dev.sayaya.handbook.client.interfaces.ui.DashboardElement;
import dev.sayaya.handbook.client.usecase.AgentActivityList;
import dev.sayaya.handbook.client.usecase.DashboardRepository;
import dev.sayaya.handbook.client.usecase.QualityIssueList;
import dev.sayaya.handbook.client.usecase.StatsProvider;

import javax.inject.Singleton;

@Singleton
@dagger.Component(modules = { DashboardModule.class, ApiModule.class })
public interface Component {
    DashboardElement dashboard();
    StatsProvider statsProvider();
    QualityIssueList qualityIssueList();
    AgentActivityList agentActivityList();
    DashboardRepository dashboardRepository();
}
