package dev.sayaya.handbook.client.usecase;

import dev.sayaya.handbook.client.domain.Menu;
import dev.sayaya.rx.subject.BehaviorSubject;
import lombok.experimental.Delegate;

import javax.inject.Inject;
import javax.inject.Singleton;

import static dev.sayaya.rx.subject.BehaviorSubject.behavior;

// 메뉴에 툴이 한개밖에 없으면 자동선택한다
@Singleton
public class MenuSelected {
    @Delegate private final BehaviorSubject<Menu> _this = behavior(null);
    @Inject MenuSelected(ToolSelected tool) {
        _this.distinctUntilChanged().subscribe(selected->{
            if(selected!=null && selected.tools()!=null && selected.tools().length==1) tool.next(selected.tools()[0]);
        });
    }
}
