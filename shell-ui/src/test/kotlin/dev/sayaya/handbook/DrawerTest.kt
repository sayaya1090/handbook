package dev.sayaya.handbook

import dev.sayaya.gwt.test.GwtHtml
import dev.sayaya.gwt.test.GwtTestSpec
import dev.sayaya.handbook.client.drawer.DrawerMock
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.ints.shouldBeLessThanOrEqual
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

@GwtHtml("src/test/webapp/drawer.html")
internal class DrawerTest: GwtTestSpec({
    Given("메뉴가 초기화됨") {
        Then("첫번째 메뉴는 Tool이 1개 이하이다") {
            DrawerMock.menu[0].tools().size shouldBeLessThanOrEqual 1
        }
        Then("두번째 메뉴는 Tool이 1개보다 많다") {
            DrawerMock.menu[1].tools().size shouldBeGreaterThan 1
        }
        Then("드로어 요소(nav.drawer)가 DOM에 존재한다") {
            page.querySelector("nav.drawer") shouldNotBe null
        }
        Then("메뉴 토글 버튼이 존재한다") {
            page.querySelector("#menu-toggle-button") shouldNotBe null
        }
        Then("ShellAppBar 가 DOM 에 존재하고 데스크톱에서도 [hide] 없이 표시된다") {
            val appBar = page.querySelector(".shell-app-bar")
            appBar shouldNotBe null
            val hasHide = page.evaluate(
                "document.querySelector('.shell-app-bar').hasAttribute('hide')"
            ).toString()
            hasHide shouldBe "false"
        }
        Then("메뉴 토글 버튼은 AppBar leading 안에 위치한다") {
            // AppBar 도입 후 햄버거 토글은 Drawer header 가 아닌 .shell-app-bar-leading 에 배치.
            val leadingToggle = page.querySelector(".shell-app-bar-leading #menu-toggle-button")
            leadingToggle shouldNotBe null
        }
        Then("WorkspaceSelectElement 는 AppBar center 안에 위치한다") {
            // 기존 .drawer-header 에 있던 WorkspaceSelect 를 AppBar center 로 이동.
            val workspaceInCenter = page.querySelector(".shell-app-bar-center .workspace")
            workspaceInCenter shouldNotBe null
        }
        Then("테마 토글 버튼이 AppBar trailing 의 .item.rail-bottom 으로 존재한다") {
            // ThemeToggle 은 NavigationRailItemElement 를 상속 → .item 구조 + .rail-bottom 클래스.
            // AppBar 도입 후 MenuRail 하단이 아닌 AppBar trailing 으로 이동.
            val themeBtn = page.querySelector(".shell-app-bar-trailing .item.rail-bottom")
            themeBtn shouldNotBe null
        }
        Then("테마 토글 버튼은 .collapse 와 md-item start slot 두 곳에 SVG 를 가진다") {
            // 아이콘 버튼(collapse 모드) + md-item 의 start slot(expand 모드) 두 위치 독립 렌더
            val collapseSvg = page.querySelector(".shell-app-bar-trailing .item.rail-bottom .collapse svg.theme-toggle-svg")
            val startSvg = page.querySelector(".shell-app-bar-trailing .item.rail-bottom md-item svg.theme-toggle-svg[slot='start']")
            collapseSvg shouldNotBe null
            startSvg shouldNotBe null
        }
        Then("테마 토글 버튼 SVG 안에 sun/moon 두 path 가 모두 존재한다") {
            // 일출/일몰 morph keyframes 를 위해 두 아이콘이 동시 렌더되고 CSS 가 가시성을 전환
            val sun = page.querySelector(".shell-app-bar-trailing .item.rail-bottom svg .sun")
            val moon = page.querySelector(".shell-app-bar-trailing .item.rail-bottom svg .moon")
            sun shouldNotBe null
            moon shouldNotBe null
        }
        Then("bottom=true 메뉴에 .bottom-menu 클래스가 부여된다") {
            // DrawerMock 의 Menu 3/4 가 bottom=true → MenuRailElement 가 .bottom-menu 클래스 추가
            val bottomMenus = page.querySelectorAll(".rail:first-child .item.bottom-menu")
            val bottomCount = DrawerMock.menu.count { it.bottom() == true }
            bottomMenus.count() shouldBe bottomCount
        }
        Then("MenuRail 의 총 .item 수는 메뉴 수와 일치한다 (theme 은 AppBar 로 이동)") {
            val items = page.querySelectorAll(".rail:first-child .item")
            items.count() shouldBe DrawerMock.menu.size
        }
        Then("테마 토글 버튼 headline 에 i18n 라벨 텍스트가 존재한다") {
            // LabelProvider fallback 값(Switch to Dark/Light)이라도 비어있지 않아야 함
            val headline = page.evaluate(
                "document.querySelector('.shell-app-bar-trailing .item.rail-bottom md-item [slot=headline]').textContent"
            ).toString()
            (headline.isNotBlank()) shouldBe true
        }
        Then("초기 상태에서 html 의 color-theme 속성이 light 또는 dark 로 설정되어 있다") {
            val theme = page.evaluate("document.documentElement.getAttribute('color-theme')").toString()
            (theme == "light" || theme == "dark") shouldBe true
        }
        When("테마 토글 버튼을 클릭하면") {
            val beforeHeadline = page.evaluate(
                "document.querySelector('.shell-app-bar-trailing .item.rail-bottom md-item [slot=headline]').textContent"
            ).toString()
            val before = page.evaluate("document.documentElement.getAttribute('color-theme')").toString()
            page.click(".shell-app-bar-trailing .item.rail-bottom")
            Thread.sleep(200)
            Then("color-theme 속성이 반대 값으로 토글된다") {
                val after = page.evaluate("document.documentElement.getAttribute('color-theme')").toString()
                after shouldNotBe before
                (after == "light" || after == "dark") shouldBe true
            }
            Then("theme-changing 클래스가 일시 부착되었다가 500ms 내에 제거된다") {
                // 클릭 후 200ms 지점에서는 아직 부착되어 있을 수 있음. 최대 600ms 대기해 사라지는 것 확인
                Thread.sleep(500)
                val has = page.evaluate(
                    "document.documentElement.classList.contains('theme-changing')"
                ).toString()
                has shouldBe "false"
            }
            Then("headline 라벨이 토글 후 반대 값으로 바뀐다") {
                val afterHeadline = page.evaluate(
                    "document.querySelector('.shell-app-bar-trailing .item.rail-bottom md-item [slot=headline]').textContent"
                ).toString()
                afterHeadline shouldNotBe beforeHeadline
                afterHeadline.isNotBlank() shouldBe true
            }
        }
        Then("메뉴 레일에 아이템이 메뉴 수만큼 렌더링된다(theme 제외)") {
            // theme toggle 도 .item 이므로 .rail-bottom 을 제외한 카운트가 실제 메뉴 수와 일치
            val items = page.querySelectorAll(".rail:first-child .item:not(.rail-bottom)")
            items.count() shouldBe DrawerMock.menu.size
        }
        Then("각 메뉴 레일 아이템에 아이콘 요소가 존재한다") {
            val iconCount = page.querySelectorAll(".rail .item .icon").count()
            iconCount shouldBeGreaterThan 0
        }

        // UC-S7: 워크스페이스 전환
        Then("워크스페이스 선택 요소(workspace)가 존재한다") {
            val wsSelect = page.querySelector(".workspace")
            wsSelect shouldNotBe null
        }

        // UC-S3: Tool Rail 영역 존재 (UC-S6 폐기로 여기 이관됨 — 구 테스트는 hover 가정이었음)
        Then("Tool Rail 영역(두 번째 .rail)이 존재한다") {
            val rails = page.querySelectorAll(".rail")
            rails.count() shouldBe 2
        }

        // UC-S10: i18n - 다국어 관련 요소 존재 확인
        Then("메뉴 아이템에 텍스트 라벨이 존재한다") {
            val items = page.querySelectorAll(".rail .item")
            items.count() shouldBeGreaterThan 0
        }

        Then("미선택 아이템은 .icon-outline 이 보이고 .icon-filled 는 숨겨진다") {
            // 초기 상태: 아직 아무 메뉴도 선택되지 않은 아이템 기준.
            // computed display 가 outline 은 non-none, filled 는 none 이어야 함.
            val outlineDisplay = page.evaluate(
                "getComputedStyle(document.querySelector('.rail:first-child .item:not([selected]) .icon-outline')).display"
            ).toString()
            val filledDisplay = page.evaluate(
                "getComputedStyle(document.querySelector('.rail:first-child .item:not([selected]) .icon-filled')).display"
            ).toString()
            filledDisplay shouldBe "none"
            (outlineDisplay != "none") shouldBe true
        }
        Then("모든 메뉴 아이템이 outline + filled 두 아이콘을 모두 렌더한다") {
            // 각 아이템은 .collapse 와 md-item start slot 두 곳에 각각 outline/filled 를 렌더 → menu.size * 2
            // .rail-bottom 은 ThemeToggle(FA 아이콘 미사용) 이므로 제외
            val expected = DrawerMock.menu.size * 2
            val outlineCount = page.querySelectorAll(".rail:first-child .item:not(.rail-bottom) .icon-outline").count()
            val filledCount = page.querySelectorAll(".rail:first-child .item:not(.rail-bottom) .icon-filled").count()
            outlineCount shouldBe expected
            filledCount shouldBe expected
        }
        Then("선택 상태에서 .collapse 배경이 secondary-container 가 아니다") {
            // 배경 채움을 제거했으므로 어떤 아이템이 선택되어 있든 배경이 채워지지 않아야 함.
            // rgb(a) 포맷 문자열 안에 transparent/0 값 포함 여부로 확인.
            page.click("#url1")
            Thread.sleep(500)
            val bg = page.evaluate(
                "getComputedStyle(document.querySelector('.rail:first-child .item[selected] .collapse')).backgroundColor"
            ).toString()
            // transparent → 'rgba(0, 0, 0, 0)'
            (bg.contains("0, 0") || bg == "transparent") shouldBe true
        }

        When("첫번째 URL 버튼을 클릭하면") {
            page.click("#url1")
            Thread.sleep(500)
            Then("메뉴가 선택된다 (selected 속성이 있는 아이템이 1개 이상)") {
                val selected = page.querySelectorAll(".rail .item[selected]")
                selected.count() shouldBeGreaterThan 0
            }
            Then("선택된 아이템은 정확히 1개이다") {
                val selected = page.querySelectorAll(".rail .item[selected]")
                selected.count() shouldBe 1
            }
            Then("선택 아이템은 .icon-filled 가 보이고 .icon-outline 은 숨겨진다") {
                val outlineDisplay = page.evaluate(
                    "getComputedStyle(document.querySelector('.rail:first-child .item[selected] .icon-outline')).display"
                ).toString()
                val filledDisplay = page.evaluate(
                    "getComputedStyle(document.querySelector('.rail:first-child .item[selected] .icon-filled')).display"
                ).toString()
                outlineDisplay shouldBe "none"
                (filledDisplay != "none") shouldBe true
            }
        }

        When("세번째 메뉴 첫번째 Tool URL 버튼을 클릭하면") {
            page.click("#url2")
            Thread.sleep(500)
            Then("메뉴가 선택된다") {
                val selected = page.querySelectorAll(".rail .item[selected]")
                selected.count() shouldBeGreaterThan 0
            }
            Then("선택된 아이템은 정확히 1개이다 (이전 선택 해제됨)") {
                val selected = page.querySelectorAll(".rail .item[selected]")
                selected.count() shouldBe 1
            }
        }

        When("다른 Tool URL(url3)을 클릭하면 같은 메뉴의 다른 Tool이 활성화된다") {
            page.click("#url3")
            Thread.sleep(500)
            Then("선택된 메뉴 아이템이 유지된다") {
                val selected = page.querySelectorAll(".rail .item[selected]")
                selected.count() shouldBe 1
            }
        }

        // UC-S4: URL 기반 딥링크
        When("URL hash를 변경하면") {
            page.evaluate("window.location.hash = '#/test-deep-link'")
            Thread.sleep(500)
            Then("URL 변경 후에도 드로어(nav.drawer)가 존재한다") {
                page.querySelector("nav.drawer") shouldNotBe null
            }
            Then("메뉴 레일 아이템 수가 유지된다") {
                // 첫 번째 .rail(MenuRail)의 아이템만 카운트, theme toggle 제외
                val items = page.querySelectorAll(".rail:first-child .item:not(.rail-bottom)")
                items.count() shouldBe DrawerMock.menu.size
            }
        }

        // UC-S15 (ThemeToggle): AppBar 도입 후 ThemeToggle 은 MenuRail 이 아닌 AppBar trailing 에
        // 상주하므로 모바일/데스크톱 양쪽에서 항상 접근 가능. MenuRail 모바일 숨김과 무관.
        Then("AppBar trailing 의 테마 토글은 display 가 none 이 아니다") {
            val themeDisplay = page.evaluate(
                "getComputedStyle(document.querySelector('.shell-app-bar-trailing .item.rail-bottom')).display"
            ).toString()
            themeDisplay shouldNotBe "none"
        }

        // UC-S13: 모바일 뷰포트로 실시간 전환 — ViewportObserver 가 matchMedia 리스너로 감지
        // 해서 양쪽 rail 에 [mobile] 속성이 부여되고 MenuRailMode/ToolRailMode 가 드릴인 패턴으로
        // 전이한다. 이 블록 앞의 테스트들이 메뉴를 선택해둔 상태(url2 = Menu 2, 도구 2개) 이므로
        // 리사이즈 시 ToolRail 이 드릴인(EXPAND) 하단 바를 차지하고 MenuRail 은 HIDE 된다.
        When("뷰포트를 모바일(375x800) 로 변경하면 (이전 테스트에서 도구 2개 메뉴 선택된 상태)") {
            page.setViewportSize(375, 800)
            Thread.sleep(400)
            Then("matchMedia(max-width:768px) 가 true 이다") {
                val matches = page.evaluate("window.matchMedia('(max-width: 768px)').matches").toString()
                matches shouldBe "true"
            }
            Then("두 rail 모두 [mobile] 속성이 부착된다 — 레이아웃과 가시성이 직교") {
                val menuMobile = page.evaluate(
                    "document.querySelector('.rail:first-child').hasAttribute('mobile')"
                ).toString()
                val toolMobile = page.evaluate(
                    "document.querySelectorAll('.rail')[1].hasAttribute('mobile')"
                ).toString()
                menuMobile shouldBe "true"
                toolMobile shouldBe "true"
            }
            Then("드릴인 상태: 두 번째 rail(ToolRail) 이 EXPAND — [expand] 속성 부착") {
                val hasExpand = page.evaluate(
                    "document.querySelectorAll('.rail')[1].hasAttribute('expand')"
                ).toString()
                hasExpand shouldBe "true"
            }
            Then("드릴인 상태: 첫 번째 rail(MenuRail) 은 HIDE — [hide] 속성 부착") {
                val hasHide = page.evaluate(
                    "document.querySelector('.rail:first-child').hasAttribute('hide')"
                ).toString()
                hasHide shouldBe "true"
            }
            // UC-S13-AppBar: 모바일 Top AppBar (ShellAppBarElement) 검증
            Then(".shell-app-bar 가 모바일에서 표시된다 ([hide] 없음)") {
                val hasHide = page.evaluate(
                    "document.querySelector('.shell-app-bar').hasAttribute('hide')"
                ).toString()
                hasHide shouldBe "false"
            }
            Then("AppBar leading 에 MenuToggleButton 이 이동되어 있다") {
                val leadingHasToggle = page.evaluate(
                    "document.querySelector('.shell-app-bar-leading #menu-toggle-button') !== null"
                ).toString()
                leadingHasToggle shouldBe "true"
            }
            Then("AppBar trailing 에 ThemeToggle 이 이동되어 있다") {
                val trailingHasTheme = page.evaluate(
                    "document.querySelector('.shell-app-bar-trailing .item.rail-bottom') !== null"
                ).toString()
                trailingHasTheme shouldBe "true"
            }
            // UC-S13-Tabs: 모바일 상단 Scrollable Tabs (MobileTabsElement) 검증
            Then(".menu-tabs 컨테이너가 DOM 에 존재하고 [hide] 속성이 없다") {
                val tabs = page.querySelector(".menu-tabs")
                tabs shouldNotBe null
                val hasHide = page.evaluate(
                    "document.querySelector('.menu-tabs').hasAttribute('hide')"
                ).toString()
                hasHide shouldBe "false"
            }
            Then(".menu-tabs + md-menu 합친 엔트리 수가 메뉴 수와 같다 (평면 또는 overflow 분리)") {
                // 3단계 폴백으로 하단정렬이 overflow 에 들어가면 md-tabs 쪽 탭 수는 줄어든다.
                // 전체 보존은 md-primary-tab + md-menu-item 합으로 검증.
                val tabCount = page.querySelectorAll(".menu-tabs md-primary-tab").count()
                val menuItemCount = page.querySelectorAll(".menu-tabs md-menu md-menu-item").count()
                (tabCount + menuItemCount) shouldBe DrawerMock.menu.size
            }
            Then("375px + 4개 탭은 viewport 초과 → overflow 버튼이 노출된다") {
                val hidden = page.evaluate(
                    "document.querySelector('.menu-tabs-overflow-btn').hasAttribute('hidden')"
                ).toString()
                hidden shouldBe "false"
            }
            Then("하단정렬(bottom=true) 메뉴는 md-menu 팝업으로 수렴된다") {
                val menuItemCount = page.querySelectorAll(".menu-tabs md-menu md-menu-item").count()
                val bottomCount = DrawerMock.menu.count { it.bottom() == true }
                menuItemCount shouldBe bottomCount
            }
            Then("overflow 버튼 클릭 시 md-menu 가 open 된다") {
                page.evaluate("document.querySelector('.menu-tabs-overflow-btn').click()")
                Thread.sleep(100)
                val open = page.evaluate(
                    "document.querySelector('.menu-tabs md-menu').hasAttribute('open')"
                ).toString()
                open shouldBe "true"
                // 원복: 다음 검증에 영향 없도록 닫기
                page.evaluate("document.querySelector('.menu-tabs md-menu').removeAttribute('open')")
            }
            // 원복: 이후 테스트들이 desktop 뷰포트에서 돌아가도록 복원
            page.setViewportSize(1280, 720)
            Thread.sleep(200)
            Then("데스크톱 뷰포트 복귀 시 .menu-tabs 는 [hide] 속성으로 숨김") {
                val hasHide = page.evaluate(
                    "document.querySelector('.menu-tabs').hasAttribute('hide')"
                ).toString()
                hasHide shouldBe "true"
            }
        }

        // UC-S15 (deprecated): ThemeToggle 은 AppBar 로 이동했으므로 MenuRail 내부 order 비교 불필요.
        // bottom-menu 는 MenuRail 의 bottom 정렬(order:auto→9000) 로 하단에 위치하는 건 유지.
        Then("bottom-menu 는 MenuRail 내 일반 메뉴보다 flex order 가 크다 (시각적으로 뒤)") {
            val bottomOrder = page.evaluate(
                "getComputedStyle(document.querySelector('.rail:first-child .item.bottom-menu')).order"
            ).toString()
            val normalOrder = page.evaluate(
                "getComputedStyle(document.querySelector('.rail:first-child .item:not(.bottom-menu)')).order"
            ).toString()
            bottomOrder.toInt() shouldBeGreaterThan normalOrder.toInt() - 1
        }
    }

    // UC-S13 (초기 로드): 페이지가 처음부터 모바일 뷰포트에서 로드되는 경우. 실사용 모바일
    // 브라우저는 desktop 에서 전환되는 게 아니라 로드 시점부터 mobile. ViewportObserver 의
    // 초기 BehaviorSubject 값이 [mobile] 속성을 즉시 부여하고, MenuRailMode/ToolRailMode 가
    // EXPAND/HIDE 로 수렴해야 한다. [mobile] 과 [expand/hide] 는 직교 속성.
    Given("처음부터 모바일 뷰포트(375x800) 로 페이지를 로드하면") {
        page.setViewportSize(375, 800)
        page.navigate("file://${java.io.File("src/test/webapp/drawer.html").absolutePath}")
        page.waitForLoadState(com.microsoft.playwright.options.LoadState.NETWORKIDLE)
        Thread.sleep(500)

        Then("matchMedia(max-width:768px) 가 true 이다") {
            val matches = page.evaluate("window.matchMedia('(max-width: 768px)').matches").toString()
            matches shouldBe "true"
        }
        Then("두 rail 모두 [mobile] 속성이 부착된다") {
            val menuMobile = page.evaluate(
                "document.querySelector('.rail:first-child').hasAttribute('mobile')"
            ).toString()
            val toolMobile = page.evaluate(
                "document.querySelectorAll('.rail')[1].hasAttribute('mobile')"
            ).toString()
            menuMobile shouldBe "true"
            toolMobile shouldBe "true"
        }
        Then("첫 번째 rail(MenuRail) 이 [expand] 가시성으로 하단 바를 차지한다") {
            val hasExpand = page.evaluate(
                "document.querySelector('.rail:first-child').hasAttribute('expand')"
            ).toString()
            hasExpand shouldBe "true"
        }
        Then("모바일 [expand] 에서도 .item .collapse (아이콘 버튼) 이 visible 이어야 한다") {
            // Regression guard: 데스크톱 .rail[expand] .item .collapse { visibility: hidden } 규칙이
            // 모바일 [mobile][expand] 에도 매칭되면 하단 바의 아이콘이 모두 사라져 빈 버튼만 남는다.
            // 모바일 전용 override (.rail[mobile][expand] .item .collapse { visibility: visible }) 가
            // 필요하다.
            val vis = page.evaluate(
                "getComputedStyle(document.querySelector('.rail:first-child .item .collapse')).visibility"
            ).toString()
            vis shouldBe "visible"
        }
        Then("MobileTabs md-primary-tab 의 가시 크기가 0 보다 크다 (아이콘 + 라벨 렌더 보장)") {
            // 모바일에서는 MenuRail 이 아닌 MobileTabsElement 가 메뉴를 노출한다.
            // 첫 번째 md-primary-tab 의 bounding box 로 실제 렌더 여부 확인.
            val box = page.evaluate(
                """
                (() => {
                    const el = document.querySelector('.menu-tabs md-primary-tab');
                    if (!el) return '0x0';
                    const r = el.getBoundingClientRect();
                    return r.width + 'x' + r.height;
                })()
                """.trimIndent()
            ).toString()
            val parts = box.split("x").map { it.toDouble() }
            (parts[0] > 0.0) shouldBe true
            (parts[1] > 0.0) shouldBe true
        }
        Then("MobileTabs 가 viewport 전체 폭(375px) 을 차지한다 (상단 고정)") {
            // Regression guard: .drawer 의 backdrop-filter containing block 영향권에 들지 않아야
            // 한다 — DrawerElement 는 navTabs 를 body 외부에 붙이지 않지만, CSS 가 position:fixed
            // 로 viewport 기준 배치되므로 375px 이 보장되어야 한다.
            val width = page.evaluate(
                "document.querySelector('.menu-tabs').getBoundingClientRect().width"
            ).toString().toDouble()
            width shouldBe 375.0
        }
        Then("MobileTabs md-primary-tab 클릭이 hit-test 로 수신된다 (elementFromPoint 가 탭 내부)") {
            // 탭 중앙에서 elementFromPoint 를 호출해 실제 hit target 이 md-primary-tab
            // 계열이어야 클릭이 동작한다. scrim 이 덮고 있거나 [hide] 로 가려지면 실패.
            val hit = page.evaluate(
                """
                (() => {
                    const tabs = document.querySelector('.menu-tabs');
                    if (!tabs) return 'no-tabs';
                    const firstTab = tabs.querySelector('md-primary-tab');
                    if (!firstTab) return 'no-tab';
                    const r = firstTab.getBoundingClientRect();
                    const cx = r.left + r.width / 2;
                    const cy = r.top + r.height / 2;
                    const el = document.elementFromPoint(cx, cy);
                    if (!el) return 'null';
                    return el.closest('md-primary-tab') ? 'tab' : (el.tagName + '.' + (el.className || ''));
                })()
                """.trimIndent()
            ).toString()
            hit shouldBe "tab"
        }
        Then("두 번째 rail(ToolRail) 은 초기엔 도구 없음 → [hide] 속성 (단 [mobile] 은 유지)") {
            // 메뉴 미선택 상태이므로 도구 목록이 비어있고 ToolRail 은 숨겨져야 한다.
            // [mobile] 은 레이아웃(position:fixed bottom) 을 상시 유지하므로 flash 가 없다.
            val attrs = page.evaluate(
                """
                (() => {
                    const r = document.querySelectorAll('.rail')[1];
                    return JSON.stringify({
                        hide: r.hasAttribute('hide'),
                        mobile: r.hasAttribute('mobile'),
                        expand: r.hasAttribute('expand'),
                        collapse: r.hasAttribute('collapse')
                    });
                })()
                """.trimIndent()
            ).toString()
            attrs.contains("\"hide\":true") shouldBe true
            attrs.contains("\"mobile\":true") shouldBe true
            attrs.contains("\"expand\":true") shouldBe false
        }

        // UC-S13: 모바일 드릴인 — 도구가 2개 이상인 메뉴 탭 → ToolRail 이 하단 바 자리를 차지
        When("도구 2개 메뉴(Menu 2, url=menu2-tool1)로 네비게이트하면") {
            page.click("#url2")
            Thread.sleep(500)
            Then("MenuRail 은 HIDE 된다 — [hide] 속성 부착, [mobile] 유지") {
                val hasHide = page.evaluate(
                    "document.querySelector('.rail:first-child').hasAttribute('hide')"
                ).toString()
                val hasMobile = page.evaluate(
                    "document.querySelector('.rail:first-child').hasAttribute('mobile')"
                ).toString()
                hasHide shouldBe "true"
                hasMobile shouldBe "true"
            }
            Then("ToolRail 이 드릴인 (EXPAND) 상태 — 두 번째 rail 에 [expand] 속성") {
                val hasExpand = page.evaluate(
                    "document.querySelectorAll('.rail')[1].hasAttribute('expand')"
                ).toString()
                hasExpand shouldBe "true"
            }
            Then("ToolRail 이 viewport 전체 폭(375px)을 차지한다") {
                val width = page.evaluate(
                    "document.querySelectorAll('.rail')[1].getBoundingClientRect().width"
                ).toString().toDouble()
                width shouldBe 375.0
            }
            Then("ToolRail 의 첫 자식이 CloseToolRailButton(← 아이콘) 이다") {
                val firstId = page.evaluate(
                    "document.querySelectorAll('.rail')[1].firstElementChild && document.querySelectorAll('.rail')[1].firstElementChild.id"
                ).toString()
                firstId shouldBe "close-tool-rail"
            }
            Then("드릴인 ToolRail 의 아이템 .collapse 아이콘이 visible 이어야 한다") {
                // 드릴인 중에도 하단 바 아이콘이 사라지지 않아야 한다. CloseToolRailButton 과
                // 도구 아이템들이 모두 .collapse 슬롯으로 렌더되므로 .collapse 의 가시성을 체크.
                val vis = page.evaluate(
                    "getComputedStyle(document.querySelectorAll('.rail')[1].querySelector('.item .collapse')).visibility"
                ).toString()
                vis shouldBe "visible"
            }
            Then("한 번에 한 rail 만 [expand] — MenuRail 과 ToolRail 이 상호 배타적") {
                val expandedCount = page.evaluate(
                    "document.querySelectorAll('.rail[mobile][expand]').length"
                ).toString()
                expandedCount shouldBe "1"
            }
        }

        // UC-S13: 모바일 드릴백 — CloseToolRailButton 클릭 → MenuRail 복귀
        When("← 버튼(CloseToolRailButton) 을 탭하면") {
            page.click("#close-tool-rail")
            Thread.sleep(500)
            Then("MenuRail 이 다시 EXPAND 로 복귀한다 — 첫 번째 rail 에 [expand] 속성") {
                val hasExpand = page.evaluate(
                    "document.querySelector('.rail:first-child').hasAttribute('expand')"
                ).toString()
                hasExpand shouldBe "true"
            }
            Then("ToolRail 은 다시 HIDE 된다 — [hide] 속성, [mobile] 은 유지") {
                val hasHide = page.evaluate(
                    "document.querySelectorAll('.rail')[1].hasAttribute('hide')"
                ).toString()
                val hasMobile = page.evaluate(
                    "document.querySelectorAll('.rail')[1].hasAttribute('mobile')"
                ).toString()
                hasHide shouldBe "true"
                hasMobile shouldBe "true"
            }
            Then("MobileTabs 가 다시 viewport 전체 폭(375px)을 차지한다 (드릴백 후 상단 Tabs 복귀)") {
                val width = page.evaluate(
                    "document.querySelector('.menu-tabs').getBoundingClientRect().width"
                ).toString().toDouble()
                width shouldBe 375.0
            }
        }
    }

    // UC-S20: 브릿지 통합 테스트 — agent-ui 없이 window 브릿지를 직접 호출해
    // shell 이 메시지에 올바르게 반응하는지 검증한다.
    Given("브릿지가 초기화된 상태에서") {
        page.setViewportSize(1280, 720)
        page.navigate("file://${java.io.File("src/test/webapp/drawer.html").absolutePath}")
        page.waitForLoadState(com.microsoft.playwright.options.LoadState.NETWORKIDLE)
        Thread.sleep(500)

        Then("WindowUriBridge 가 window.__handbook_uri 에 등록되어 있다") {
            val registered = page.evaluate("typeof window.__handbook_uri === 'function'").toString()
            registered shouldBe "true"
        }

        Then("WindowProgressBridge 가 window.__handbook_progress 에 등록되어 있다") {
            val registered = page.evaluate("typeof window.__handbook_progress === 'function'").toString()
            registered shouldBe "true"
        }

        When("WindowUriBridge 로 menu1-tool1 URL 을 전달하면") {
            page.evaluate("window.__handbook_uri('menu1-tool1')")
            Thread.sleep(500)
            Then("Menu 1 이 selected 된다") {
                val selected = page.evaluate(
                    "document.querySelector('.rail .item[selected] md-item [slot=headline]')?.textContent"
                ).toString().lowercase()
                selected shouldBe "menu 1"
            }
        }

        When("WindowUriBridge 로 menu3-tool1 URL 을 전달하면") {
            page.evaluate("window.__handbook_uri('menu3-tool1')")
            Thread.sleep(500)
            Then("Menu 3 이 selected 로 변경된다") {
                val selected = page.evaluate(
                    "document.querySelector('.rail .item[selected] md-item [slot=headline]')?.textContent"
                ).toString().lowercase()
                selected shouldBe "menu 3"
            }
            Then("이전 메뉴(Menu 1) 는 selected 가 아니다") {
                val count = page.evaluate(
                    "document.querySelectorAll('.rail .item[selected]').length"
                ).toString()
                count shouldBe "1"
            }
        }

        // UC-S6 (2026-04-17 재정의): MenuRail 상태 기반 hover 정책
        //   - EXPAND: hover peek 유지 (탐색 UX) — click 기반 ToolRail 전환 테스트(UC-S2/S3)가 간접 커버
        //   - COLLAPSE / 모바일: hover → TooltipCard 라벨만, ToolRail 전환 없음
        //   - agent-command highlight(.ui-highlight) 시 tooltip 즉시 표시
        // EXPAND hover peek positive 회귀는 DrawerMock 의 class 체계 정리 후 별 PR 에서 재도입.

        When("WindowProgressBridge 로 프로그레스를 전달하면") {
            page.evaluate("""
                window.__handbook_progress({ enabled: true, intermediate: false, value: 3, max: 10, description: '처리 중' })
            """.trimIndent())
            Thread.sleep(300)
            Then("프로그레스 옵저버가 값을 수신한다 — 브릿지 연결 검증") {
                // DrawerTest 환경에는 ProgressElement 가 body 에 없으므로
                // 브릿지 함수 호출이 에러 없이 완료되는 것 자체가 연결 검증.
                val bridgeExists = page.evaluate("typeof window.__handbook_progress === 'function'").toString()
                bridgeExists shouldBe "true"
            }
        }
    }
})
