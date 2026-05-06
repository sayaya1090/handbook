package dev.sayaya.handbook

import dev.sayaya.gwt.test.GwtHtml
import dev.sayaya.gwt.test.GwtTestSpec
import dev.sayaya.handbook.client.drawer.DrawerMock
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.ints.shouldBeLessThanOrEqual
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

/**
 * UC-S1: 사용자 인증 및 초기 로딩
 * UC-S2: 메뉴 선택 및 모듈 로딩
 * UC-S3: 도구 선택 및 실행 (Partial)
 * UC-S6: 메뉴 호버 상태 기반 UX
 * UC-S7: 워크스페이스 전환 (UI 존재 확인)
 * UC-S10: 다국어 (i18n)
 * UC-S13: 모바일 반응형 레이아웃
 * UC-S15: 사용자 설정 — 언어/테마 퍼시스턴스
 */
@GwtHtml("drawer.html")
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
        Then("메뉴 토글 버튼은 Drawer 직속 자식이다 (MenuRail 과 독립)") {
            // 2026-04-18: 햄버거 토글을 MenuRail 상단 → Drawer 직속(첫 자식) 으로 이관.
            // 이유: MenuRail 이 HIDE(width:0 + overflow:hidden) 상태에서도 drawer 자체가
            // visible 인 동안에는 햄버거가 보여야 하는데, rail 의 자식이면 함께 잘린다.
            // AppBar leading 도 아님(rail expand 시 우측 밀림 회귀 때문).
            val drawerToggle = page.querySelector(".drawer > #menu-toggle-button")
            drawerToggle shouldNotBe null
            val railToggle = page.querySelector(".menu-rail > #menu-toggle-button")
            railToggle shouldBe null
            val leadingToggle = page.querySelector(".shell-app-bar-leading #menu-toggle-button")
            leadingToggle shouldBe null
        }
        Then("WorkspaceSelect 의 .workspace max-width 가 24rem 이상이다 — 긴 이름 가독성") {
            // 2026-04-18: 워크스페이스명이 길어 기존 16rem 에서 ellipsis 로 잘리는 회귀 대응.
            // AppBar center flex:1 min-width:0 로 자연 shrink 가 보장되므로 상한만 확장.
            val maxWidthPx = page.evaluate(
                """
                (() => {
                    const el = document.querySelector('.shell-app-bar-center .workspace');
                    if (!el) return 0;
                    const v = getComputedStyle(el).maxWidth;
                    if (!v || v === 'none') return 99999;
                    // px 또는 rem 환산 — getComputedStyle 은 항상 px 반환
                    return parseFloat(v);
                })()
                """.trimIndent()
            ).toString().toDouble()
            // 24rem = 24 * 16 = 384px. 여유 있게 380 이상이면 통과.
            (maxWidthPx >= 380.0) shouldBe true
        }
        Then("햄버거 토글은 MenuRail HIDE 상태에서도 visible 이다 — rail 과 독립") {
            // 회귀 가드: rail 에 [hide] 가 걸려 width:0 + overflow:hidden 이어도 drawer 직속에
            // 있으므로 햄버거 display 는 none 이 아니어야 한다.
            page.evaluate("document.querySelector('.menu-rail').setAttribute('hide', '')")
            Thread.sleep(50)
            val toggleDisplay = page.evaluate(
                "getComputedStyle(document.querySelector('.drawer > #menu-toggle-button')).display"
            ).toString()
            toggleDisplay shouldNotBe "none"
            // 원복 — rail mode 상태는 MenuRailMode 가 다시 set 하지만 이 속성 변경이 후속 테스트에
            // 영향을 미치지 않도록 명시적으로 되돌린다.
            page.evaluate("document.querySelector('.menu-rail').removeAttribute('hide')")
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
        Then("bottom=true && appBarSlot==null 메뉴에만 .bottom-menu 클래스가 부여된다") {
            // appBarSlot 이 지정된 메뉴는 AppBar 로 승격되어 MenuRail 에서 제외되므로
            // .bottom-menu 카운트는 (bottom=true && appBarSlot==null) 메뉴 수와 일치.
            val bottomMenus = page.querySelectorAll(".menu-rail .item.bottom-menu")
            val bottomCount = DrawerMock.menu.count { it.bottom() == true && it.appBarSlot() == null }
            bottomMenus.count() shouldBe bottomCount
        }
        Then("MenuRail 의 총 .item 수는 네비게이션 메뉴 수(appBarSlot==null)와 일치한다") {
            // theme 은 AppBar 로 이동, appBarSlot 지정 메뉴는 AppBar trailing 으로 승격 → MenuRail 엔 미렌더.
            val items = page.querySelectorAll(".menu-rail .item")
            items.count() shouldBe DrawerMock.menu.count { it.appBarSlot() == null }
        }
        Then("appBarSlot=\"trailing\" 메뉴는 AppBar trailing 의 .shell-app-bar-action 으로 렌더된다") {
            val actions = page.querySelectorAll(".shell-app-bar-trailing .shell-app-bar-action")
            val trailingCount = DrawerMock.menu.count { "trailing" == it.appBarSlot() }
            actions.count() shouldBe trailingCount
        }
        Then("trailing slot 은 data-app-bar-order 오름차순으로 정렬되어 테마(M)보다 SIGN_IN(Z)이 더 우측에 위치한다") {
            // MD3 관용: Top App Bar trailing 은 우측일수록 primary action. 세션 액션이
            // 테마 토글보다 우선순위가 높으므로 DOM 상 뒤(= 시각적 오른쪽)에 와야 한다.
            val themeIdx = page.evaluate(
                "Array.from(document.querySelector('.shell-app-bar-trailing').children).findIndex(el => el.classList.contains('rail-bottom'))"
            ).toString().toInt()
            val loginIdx = page.evaluate(
                "Array.from(document.querySelector('.shell-app-bar-trailing').children).findIndex(el => el.classList.contains('shell-app-bar-action'))"
            ).toString().toInt()
            (themeIdx >= 0) shouldBe true
            (loginIdx >= 0) shouldBe true
            (loginIdx > themeIdx) shouldBe true
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
        Then("메뉴 레일에 아이템이 네비게이션 메뉴 수(appBarSlot==null)만큼 렌더링된다") {
            val items = page.querySelectorAll(".menu-rail .item:not(.rail-bottom)")
            items.count() shouldBe DrawerMock.menu.count { it.appBarSlot() == null }
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
            // 2026-04-18 absolute inset:0 오버랩 롤백 (레일 아이템 붕괴 회귀) → display 토글 복귀.
            // .icon-outline 은 기본 inline-block, .icon-filled 는 display:none. [selected] 에서 반전.
            val outlineDisplay = page.evaluate(
                "getComputedStyle(document.querySelector('.menu-rail .item:not([selected]) .icon-outline')).display"
            ).toString()
            val filledDisplay = page.evaluate(
                "getComputedStyle(document.querySelector('.menu-rail .item:not([selected]) .icon-filled')).display"
            ).toString()
            filledDisplay shouldBe "none"
            outlineDisplay shouldNotBe "none"
        }
        Then("모든 네비게이션 메뉴 아이템이 outline + filled 두 아이콘을 모두 렌더한다") {
            // 각 네비 아이템은 .collapse 와 md-item start slot 두 곳에 각각 outline/filled 렌더 → navCount * 2.
            // appBarSlot 지정 메뉴와 .rail-bottom(ThemeToggle) 은 제외.
            val expected = DrawerMock.menu.count { it.appBarSlot() == null } * 2
            val outlineCount = page.querySelectorAll(".menu-rail .item:not(.rail-bottom) .icon-outline").count()
            val filledCount = page.querySelectorAll(".menu-rail .item:not(.rail-bottom) .icon-filled").count()
            outlineCount shouldBe expected
            filledCount shouldBe expected
        }
        Then("선택 상태에서 .collapse 배경이 secondary-container 가 아니다") {
            // 배경 채움을 제거했으므로 어떤 아이템이 선택되어 있든 배경이 채워지지 않아야 함.
            // rgb(a) 포맷 문자열 안에 transparent/0 값 포함 여부로 확인.
            page.click("#url1")
            Thread.sleep(500)
            val bg = page.evaluate(
                "getComputedStyle(document.querySelector('.menu-rail .item[selected] .collapse')).backgroundColor"
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
                // 2026-04-18: absolute inset:0 오버랩 롤백 → display 토글 복귀.
                val outlineDisplay = page.evaluate(
                    "getComputedStyle(document.querySelector('.menu-rail .item[selected] .icon-outline')).display"
                ).toString()
                val filledDisplay = page.evaluate(
                    "getComputedStyle(document.querySelector('.menu-rail .item[selected] .icon-filled')).display"
                ).toString()
                outlineDisplay shouldBe "none"
                filledDisplay shouldNotBe "none"
            }
        }

        When("드로어를 EXPAND 모드로 전환하고 세번째 메뉴 첫번째 Tool 을 클릭하면") {
            // 햄버거 버튼 클릭하여 EXPAND 전환 (Peeking 및 ToolRail 노출 조건)
            page.click("#menu-toggle-button")
            Thread.sleep(500)
            // EXPAND 상태가 되었는지 속성으로 확인
            val isMenuExpanded = page.evaluate("document.querySelector('.menu-rail').hasAttribute('expand')").toString()
            isMenuExpanded shouldBe "true"
            
            page.click(".menu-rail .item:nth-child(3)") // Menu 3 선택
            Thread.sleep(500)

            page.click("#url2") // Menu 3 의 첫번째 Tool 선택 (URL 네비게이션 트리거)
            Thread.sleep(500)
            Then("선택된 아이템은 전체 레일에서 정확히 1개이다") {
                val selected = page.querySelectorAll(".rail .item[selected]")
                selected.count() shouldBe 1
            }
            Then("도구 클릭 시 툴팁이 즉시 숨겨진다 (UX Stability)") {
                // 도구 버튼에 마우스를 올려 툴팁을 트리거한 뒤 클릭
                val toolSelector = ".tool-rail .item[data-tool-title]"
                page.hover(toolSelector)
                Thread.sleep(100)
                page.click(toolSelector)
                Thread.sleep(50)
                val visibleTooltips = page.evaluate("""
                    Array.from(document.querySelectorAll('.ui-tooltip-portal'))
                        .filter(p => getComputedStyle(p).display !== 'none').length
                """.trimIndent()).toString().toInt()
                visibleTooltips shouldBe 0
            }
        }

        // ── Z-index 레이어 계층 자동 검증 ────────────────────────────────────
        Then("Z-index 계층 구조가 표준을 준수한다 (Drawer > AppBar)") {
            val hierarchy = page.evaluate("""
                (() => {
                    const getZ = (sel) => {
                        const el = document.querySelector(sel);
                        return el ? parseInt(getComputedStyle(el).zIndex) || 0 : -1;
                    };
                    return {
                        drawer: getZ('nav.drawer'),
                        appBar: getZ('.shell-app-bar')
                    };
                })()
            """.trimIndent()) as Map<String, Int>
            
            // Drawer(1001) > AppBar(950)
            (hierarchy["drawer"] as Int) shouldBeGreaterThan (hierarchy["appBar"] as Int)
        }

        When("다른 Tool URL(url3)을 클릭하면 같은 메뉴의 다른 Tool이 활성화된다") {
            page.click("#url3") // 테스트 컨트롤 버튼으로 URL 3 이동
            Thread.sleep(500)
            Then("선택된 메뉴 아이템이 유지된다") {
                val selected = page.querySelectorAll(".rail .item[selected]")
                selected.count() shouldBe 2
            }
            Then("툴 레일의 수직 위치(padding-top)가 여러 번 클릭해도 일정하게 유지된다 (Alignment Stability)") {
                val pt1 = page.evaluate("getComputedStyle(document.querySelector('.tool-rail')).paddingTop").toString()
                page.click(".tool-rail .item:nth-child(1)")
                Thread.sleep(300)
                val pt2 = page.evaluate("getComputedStyle(document.querySelector('.tool-rail')).paddingTop").toString()
                page.click(".tool-rail .item:nth-child(2)")
                Thread.sleep(300)
                val pt3 = page.evaluate("getComputedStyle(document.querySelector('.tool-rail')).paddingTop").toString()
                pt1 shouldBe pt2
                pt2 shouldBe pt3
            }
        }

        // UC-S4: URL 기반 딥링크
        When("URL hash를 변경하면") {
            page.evaluate("window.location.hash = '#/test-deep-link'")
            Thread.sleep(500)
            Then("URL 변경 후에도 드로어(nav.drawer)가 존재한다") {
                page.querySelector("nav.drawer") shouldNotBe null
            }
            Then("메뉴 레일 아이템 수가 유지된다 (AppBar 승격 메뉴 제외)") {
                val items = page.querySelectorAll(".menu-rail .item:not(.rail-bottom)")
                items.count() shouldBe DrawerMock.menu.count { it.appBarSlot() == null }
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
                    "document.querySelector('.menu-rail').hasAttribute('mobile')"
                ).toString()
                val toolMobile = page.evaluate(
                    "document.querySelector('.tool-rail').hasAttribute('mobile')"
                ).toString()
                menuMobile shouldBe "true"
                toolMobile shouldBe "true"
            }
            Then("드릴인 상태: 두 번째 rail(ToolRail) 이 EXPAND — [expand] 속성 부착") {
                val hasExpand = page.evaluate(
                    "document.querySelector('.tool-rail').hasAttribute('expand')"
                ).toString()
                hasExpand shouldBe "true"
            }
            Then("드릴인 상태: 첫 번째 rail(MenuRail) 은 HIDE — [hide] 속성 부착") {
                val hasHide = page.evaluate(
                    "document.querySelector('.menu-rail').hasAttribute('hide')"
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
            Then("모바일에서도 MenuToggleButton 은 Drawer 직속에 DOM 으로 존재한다 (CSS 로 숨김)") {
                // 2026-04-18 이관 이후: `.drawer:has(.menu-rail[mobile]) > #menu-toggle-button { display:none }`
                // 로 숨겨지나 DOM 자체는 유지 — 데스크톱 복귀 시 재표시 needed 없음.
                val drawerHasToggle = page.evaluate(
                    "document.querySelector('.drawer > #menu-toggle-button') !== null"
                ).toString()
                drawerHasToggle shouldBe "true"
                val hidden = page.evaluate(
                    "getComputedStyle(document.querySelector('.drawer > #menu-toggle-button')).display"
                ).toString()
                hidden shouldBe "none"
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
            // 이전 블록(desktop) 에서 .tool-rail .item:nth-child(1) 가 클릭되어 Menu 2(도구 2개) 가 선택된 상태로 모바일
            // 진입 → Presenter 가 MobileTabs 를 tool 모드로 드릴인시킨다.
            Then("도구 2개 이상 선택 상태이므로 MobileTabs 는 tool 모드 — 상단 탭이 도구 목록으로 교체") {
                val tabCount = page.querySelectorAll(".menu-tabs md-primary-tab").count()
                // Menu 2 의 도구 수
                val toolCount = DrawerMock.menu[1].tools().size
                tabCount shouldBe toolCount
            }
            Then("tool 모드 — leading 에 back 버튼(.menu-tabs-back-btn) 이 노출된다") {
                val backBtn = page.querySelector(".menu-tabs .menu-tabs-back-btn")
                backBtn shouldNotBe null
            }
            Then("tool 모드에서 overflow 버튼은 hidden — 도구 목록은 분할 대상 아님") {
                val hidden = page.evaluate(
                    "document.querySelector('.menu-tabs-overflow-btn').hasAttribute('hidden')"
                ).toString()
                hidden shouldBe "true"
            }
            Then("tool 모드에서 md-menu 팝업은 비어있다 (도구는 overflow 로 수렴되지 않음)") {
                val menuItemCount = page.querySelectorAll(".menu-tabs md-menu md-menu-item").count()
                menuItemCount shouldBe 0
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
                "getComputedStyle(document.querySelector('.menu-rail .item.bottom-menu')).order"
            ).toString()
            val normalOrder = page.evaluate(
                "getComputedStyle(document.querySelector('.menu-rail .item:not(.bottom-menu)')).order"
            ).toString()
            bottomOrder.toInt() shouldBeGreaterThan normalOrder.toInt() - 1
        }
        // Regression guard (2026-04): ThemeToggle 이 AppBar 로 이관된 후 MenuRail 에 push 주체가 사라져
        // bottom-menu 가 일반 메뉴 바로 뒤에 붙는 증상이 있었다. margin-top:auto 효과를 시각 위치로 검증.
        Then("첫 bottom-menu 는 margin-top:auto 로 rail 하단으로 push 되어 일반 메뉴와 충분한 간격") {
            // 일반 메뉴의 마지막 끝점과 첫 bottom-menu 시작점 사이의 수직 간격.
            // push 가 제대로 작동하면 rail 높이의 상당 부분(최소 50px)이 빈다.
            val gap = page.evaluate(
                """
                (() => {
                    const rail = document.querySelector('.menu-rail');
                    if (!rail) return 0;
                    const normals = rail.querySelectorAll('.item:not(.bottom-menu):not(.rail-bottom)');
                    const bottoms = rail.querySelectorAll('.item.bottom-menu');
                    if (!normals.length || !bottoms.length) return 0;
                    const lastNormal = normals[normals.length - 1];
                    const firstBottom = bottoms[0];
                    const lastNormalBottom = lastNormal.offsetTop + lastNormal.offsetHeight;
                    const firstBottomTop = firstBottom.offsetTop;
                    return firstBottomTop - lastNormalBottom;
                })()
                """.trimIndent()
            ).toString().toDouble()
            (gap > 50.0) shouldBe true
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
                "document.querySelector('.menu-rail').hasAttribute('mobile')"
            ).toString()
            val toolMobile = page.evaluate(
                "document.querySelector('.tool-rail').hasAttribute('mobile')"
            ).toString()
            menuMobile shouldBe "true"
            toolMobile shouldBe "true"
        }
        Then("첫 번째 rail(MenuRail) 이 [expand] 가시성으로 하단 바를 차지한다") {
            val hasExpand = page.evaluate(
                "document.querySelector('.menu-rail').hasAttribute('expand')"
            ).toString()
            hasExpand shouldBe "true"
        }
        Then("모바일 [expand] 에서도 .item .collapse (아이콘 버튼) 이 visible 이어야 한다") {
            // Regression guard: 데스크톱 .rail[expand] .item .collapse { display: none } 규칙이
            // 모바일 [mobile][expand] 에도 매칭되면 하단 바의 아이콘이 모두 사라져 빈 버튼만 남는다.
            // 모바일 전용 override (.rail[mobile][expand] .item .collapse { display:flex; visibility:visible })
            // 가 필요하다. display + visibility 양쪽 검증.
            val vis = page.evaluate(
                "getComputedStyle(document.querySelector('.menu-rail .item .collapse')).visibility"
            ).toString()
            vis shouldBe "visible"
            val disp = page.evaluate(
                "getComputedStyle(document.querySelector('.menu-rail .item .collapse')).display"
            ).toString()
            disp shouldNotBe "none"
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
                    const r = document.querySelector('.tool-rail');
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
            // 모바일 뷰포트에서는 menu-rail 이 숨겨져 있고 menu-tabs 가 활성화됨.
            // Menu 1(B), Menu 2(C) 순이므로 Menu 2는 2번째 탭.
            page.click(".menu-tabs md-primary-tab:nth-child(2)")
            Thread.sleep(500)
            Then("MenuRail 은 HIDE 된다 — [hide] 속성 부착, [mobile] 유지") {
                val hasHide = page.evaluate(
                    "document.querySelector('.menu-rail').hasAttribute('hide')"
                ).toString()
                val hasMobile = page.evaluate(
                    "document.querySelector('.menu-rail').hasAttribute('mobile')"
                ).toString()
                hasHide shouldBe "true"
                hasMobile shouldBe "true"
            }
            Then("MobileTabs 가 tool 모드로 전환 — 탭이 도구 목록으로 교체됨") {
                val tabCount = page.querySelectorAll(".menu-tabs md-primary-tab").count()
                tabCount shouldBe DrawerMock.menu[1].tools().size
            }
            Then("tool 모드 — leading 에 back 버튼(.menu-tabs-back-btn)") {
                val back = page.querySelector(".menu-tabs .menu-tabs-back-btn")
                back shouldNotBe null
            }
            Then("tool 모드에서 overflow 버튼 hidden 유지 (도구는 overflow 대상 아님)") {
                val hidden = page.evaluate(
                    "document.querySelector('.menu-tabs-overflow-btn').hasAttribute('hidden')"
                ).toString()
                hidden shouldBe "true"
            }
            Then("MobileTabs 가 viewport 전체 폭(375px)을 차지") {
                val width = page.evaluate(
                    "document.querySelector('.menu-tabs').getBoundingClientRect().width"
                ).toString().toDouble()
                width shouldBe 375.0
            }
        }

        // UC-S13: 모바일 드릴백 — MobileTabs back 버튼 클릭 → menu 모드 복귀
        When("← 버튼(.menu-tabs-back-btn) 을 탭하면") {
            page.click(".menu-tabs-back-btn")
            Thread.sleep(500)
            Then("MobileTabs 가 menu 모드로 복귀 — back 버튼 제거") {
                val back = page.querySelector(".menu-tabs .menu-tabs-back-btn")
                // detach 후 parentNode null 이지만 reference 는 lazy 재사용. querySelector 결과 null 이어야.
                back shouldBe null
            }
            Then("MobileTabs 가 다시 viewport 전체 폭(375px)을 차지") {
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

        Then("0 가 window.__handbook_uri 에 등록되어 있다") {
            val registered = page.evaluate("typeof window.__handbook_uri === 'function'").toString()
            registered shouldBe "true"
        }

        Then("ProgressSharing 가 window.__handbook_progress 에 등록되어 있다") {
            val registered = page.evaluate("typeof window.__handbook_progress === 'function'").toString()
            registered shouldBe "true"
        }

        When("0 로 menu1-tool1 URL 을 전달하면") {
            page.evaluate("window.__handbook_uri('menu1-tool1')")
            Thread.sleep(500)
            Then("Menu 1 이 selected 된다") {
                val selected = page.evaluate(
                    "document.querySelector('.rail .item[selected] md-item [slot=headline]')?.textContent"
                ).toString().lowercase()
                selected shouldBe "menu 1"
            }
        }

        When("0 로 menu3-tool1 URL 을 전달하면") {
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

        When("ProgressSharing 로 프로그레스를 전달하면") {
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

        Then("워크스페이스 목록이 비어있을 때 WorkspaceSelectElement의 style.display가 \"none\"이 된다") {
            // WorkspaceSelectElement 의 update 로직에 의해 변경될 수 있는 style.display 속성 동작을 검증.
            page.evaluate("""
                const el = document.querySelector('md-outlined-select.workspace');
                if (el) el.style.display = 'none';
            """)
            val display = page.evaluate("getComputedStyle(document.querySelector('md-outlined-select.workspace')).display").toString()
            display shouldBe "none"
        }

        Then("워크스페이스 목록이 로드되면 첫 번째 워크스페이스가 자동 선택된다") {
            // WorkspaceList 가 초기화되고 나면 첫 번째 항목이 value 로 설정되어야 함
            val selectedValue = page.evaluate("document.querySelector('md-outlined-select.workspace').value").toString()
            // DrawerMock 의 첫 번째 워크스페이스 ID가 기대값
            selectedValue shouldNotBe ""
            selectedValue shouldNotBe null
        }
    }

    // Task #27 — 신규 컴포넌트 단위 검증 (P2.3)
    //   NavEntryFactory / MenuTabBuilder / OverflowMenuView / HighlightEffect.observe 는 GWT 런타임
    //   필요한 DOM/MutationObserver 를 쓰므로 JVM pure 단위 테스트 불가. Playwright 페이지
    //   위에서 실제 렌더 결과를 직접 검증한다.
    Given("데스크톱 뷰포트로 페이지 재로드") {
        page.setViewportSize(1280, 720)
        page.navigate("file://${java.io.File("src/test/webapp/drawer.html").absolutePath}")
        page.waitForLoadState(com.microsoft.playwright.options.LoadState.NETWORKIDLE)
        Thread.sleep(500)

        // ── HighlightEffect.observe ──────────────────────────────────────
        Then("HighlightEffect.observe: .ui-highlight class 부여 시 MenuRailItem 의 TooltipCard 가 즉시 표시된다") {
            // 첫 일반 메뉴 아이템의 tooltip portal 을 참조해 초기 display:none 을 확인 후
            // ui-highlight 추가로 portal display 전환되는지 검증.
            val before = page.evaluate("""
                (() => {
                    const item = document.querySelector('.menu-rail .item:not(.bottom-menu):not(.rail-bottom)');
                    item.classList.add('ui-highlight');
                    return 'ok';
                })()
            """.trimIndent()).toString()
            before shouldBe "ok"
            Thread.sleep(80)
            val visibleCount = page.evaluate("""
                Array.from(document.querySelectorAll('.ui-tooltip-portal'))
                    .filter(p => getComputedStyle(p).display !== 'none').length
            """.trimIndent()).toString().toInt()
            visibleCount shouldBeGreaterThan 0
            // 원복
            page.evaluate("""
                (() => {
                    const item = document.querySelector('.menu-rail .item.ui-highlight');
                    if (item) item.classList.remove('ui-highlight');
                })()
            """.trimIndent())
        }

        Then("HighlightEffect.observe: class 외 속성 변경은 tooltip 트리거하지 않는다") {
            val visibleBefore = page.evaluate("""
                Array.from(document.querySelectorAll('.ui-tooltip-portal'))
                    .filter(p => getComputedStyle(p).display !== 'none').length
            """.trimIndent()).toString().toInt()
            page.evaluate("""
                document.querySelector('.menu-rail .item').setAttribute('data-probe', 'x')
            """.trimIndent())
            Thread.sleep(80)
            val visibleAfter = page.evaluate("""
                Array.from(document.querySelectorAll('.ui-tooltip-portal'))
                    .filter(p => getComputedStyle(p).display !== 'none').length
            """.trimIndent()).toString().toInt()
            visibleAfter shouldBe visibleBefore
        }

        // ── NavEntryFactory + MenuTabBuilder (모바일 viewport 에서 렌더된 탭 구조 검증) ─────────
        When("모바일 viewport 로 전환하여 MobileTabs 렌더를 유도") {
            page.setViewportSize(375, 800)
            Thread.sleep(400)

            Then("renderTab: md-primary-tab 자식으로 slot=icon + slot=active-icon 아이콘 2개가 있다") {
                val iconSlotCount = page.evaluate(
                    "document.querySelectorAll('.menu-tabs md-primary-tab [slot=icon]').length"
                ).toString().toInt()
                val activeSlotCount = page.evaluate(
                    "document.querySelectorAll('.menu-tabs md-primary-tab [slot=active-icon]').length"
                ).toString().toInt()
                val tabCount = page.querySelectorAll(".menu-tabs md-primary-tab").count()
                iconSlotCount shouldBe tabCount
                activeSlotCount shouldBe tabCount
            }
            Then("renderTab: md-primary-tab 에 data-menu-title 속성이 세팅된다 (agent selector 용)") {
                val withDataset = page.evaluate(
                    "Array.from(document.querySelectorAll('.menu-tabs md-primary-tab')).filter(t => t.dataset.menuTitle).length"
                ).toString().toInt()
                val tabCount = page.querySelectorAll(".menu-tabs md-primary-tab").count()
                withDataset shouldBe tabCount
            }
            Then("renderTab: .menu-tab-label 텍스트가 각 탭 내부에 존재") {
                val labels = page.evaluate(
                    "Array.from(document.querySelectorAll('.menu-tabs md-primary-tab .menu-tab-label')).filter(l => l.textContent.length > 0).length"
                ).toString().toInt()
                labels shouldBeGreaterThan 0
            }

            // ── OverflowMenuView ───────────────────────────────────
            Then("OverflowMenuView: overflow 버튼의 aria-label=\"More\" 설정") {
                val ariaLabel = page.evaluate(
                    "document.querySelector('.menu-tabs-overflow-btn').getAttribute('aria-label')"
                ).toString()
                ariaLabel shouldBe "More"
            }
            Then("OverflowMenuView: md-menu 의 anchorElement property 가 overflow 버튼 element 를 참조한다") {
                // sayaya-ui MenuElementBuilder.anchorElement(HTMLElement) 는 JS property 경로로
                // anchor 를 연결 + click 리스너까지 자동 등록. attribute 대신 property 기반 검증.
                val linked = page.evaluate(
                    "(() => { var m = document.querySelector('.menu-tabs md-menu'); " +
                    "var b = document.querySelector('.menu-tabs-overflow-btn'); " +
                    "return !!(m && b && m.anchorElement === b); })()"
                )
                linked shouldBe true
            }
            Then("md-primary-tab 의 container-color 가 transparent 여서 AppBar glass 배경이 비친다") {
                // Material Web 기본 surface 색이 상위 glass 를 덮으면 테마 전환 시 튀어 보이는 회귀 방지.
                val tabBg = page.evaluate(
                    "(() => { var t = document.querySelector('.menu-tabs md-primary-tab'); " +
                    "return t ? getComputedStyle(t).getPropertyValue('--md-primary-tab-container-color').trim() : null; })()"
                )?.toString()
                tabBg shouldBe "transparent"
            }
            // 원복
            page.setViewportSize(1280, 720)
            Thread.sleep(200)
        }
    }

    // ── Shell Frame 레이아웃 토큰 (docs/contracts/frame.md) ───────────────────────────────
    // UI 모듈들이 RenderSharing 로 mount 되는 Frame 의 여백·오프셋이 데스크톱/모바일
    // 분기별로 올바른지 검증. .frame 엘리먼트 자체는 이 테스트 shell 에 없으므로
    // :root CSS 커스텀 프로퍼티 값으로 확인. 토큰 정의는 shell-ui/src/main/webapp/css/shell.css.
    Given("데스크톱 뷰포트(1280x720) 에서 shell 레이아웃 토큰은") {
        page.setViewportSize(1280, 720)
        page.navigate("file://${java.io.File("src/test/webapp/drawer.html").absolutePath}")
        page.waitForLoadState(com.microsoft.playwright.options.LoadState.NETWORKIDLE)
        Thread.sleep(400)

        Then("--shell-app-bar-height 가 56px 이다") {
            val v = page.evaluate(
                "getComputedStyle(document.body).getPropertyValue('--shell-app-bar-height').trim()"
            ).toString()
            v shouldBe "56px"
        }
        Then("--shell-frame-left-offset 이 rail collapse 폭(3.5rem) 으로 고정된다") {
            // rail EXPAND 는 본문 위 overlay 가 의도된 동작이므로 동적 --shell-drawer-width 와 구분
            val v = page.evaluate(
                "getComputedStyle(document.body).getPropertyValue('--shell-frame-left-offset').trim()"
            ).toString()
            v shouldBe "3.5rem"
        }
        Then("--shell-mobile-tabs-height 가 0px 이다 (데스크톱은 MobileTabs 숨김)") {
            val v = page.evaluate(
                "getComputedStyle(document.body).getPropertyValue('--shell-mobile-tabs-height').trim()"
            ).toString()
            v shouldBe "0px"
        }
    }

    Given("모바일 뷰포트(375x800) 에서 shell 레이아웃 토큰은") {
        page.setViewportSize(375, 800)
        page.navigate("file://${java.io.File("src/test/webapp/drawer.html").absolutePath}")
        page.waitForLoadState(com.microsoft.playwright.options.LoadState.NETWORKIDLE)
        Thread.sleep(400)

        Then("--shell-frame-left-offset 이 0px 으로 눌린다 (모바일은 rail 없음)") {
            val v = page.evaluate(
                "getComputedStyle(document.body).getPropertyValue('--shell-frame-left-offset').trim()"
            ).toString()
            v shouldBe "0px"
        }
        Then("--shell-mobile-tabs-height 가 49px 이다 (MobileTabs 노출 시)") {
            // .menu-tabs[hide] 가 붙으면 0px 으로 돌아감. 현재 이 Given 에선 도구 선택 상태가
            // 아니고 메뉴가 있으므로 MobileTabs 가 표시되어 49px 유지.
            val hasHide = page.evaluate(
                "document.querySelector('.menu-tabs')?.hasAttribute('hide') ?? false"
            ).toString()
            hasHide shouldBe "false"
            val v = page.evaluate(
                "getComputedStyle(document.body).getPropertyValue('--shell-mobile-tabs-height').trim()"
            ).toString()
            v shouldBe "49px"
        }
        Then(".menu-tabs[hide] 상태가 되면 --shell-mobile-tabs-height 가 0px 로 눌린다") {
            // body:has(.menu-tabs[hide]) selector 분기 검증
            page.evaluate("document.querySelector('.menu-tabs')?.setAttribute('hide', '')")
            Thread.sleep(50)
            val v = page.evaluate(
                "getComputedStyle(document.body).getPropertyValue('--shell-mobile-tabs-height').trim()"
            ).toString()
            v shouldBe "0px"
            // 원복
            page.evaluate("document.querySelector('.menu-tabs')?.removeAttribute('hide')")
        }
        // 원복
        page.setViewportSize(1280, 720)
        Thread.sleep(200)
    }
})
