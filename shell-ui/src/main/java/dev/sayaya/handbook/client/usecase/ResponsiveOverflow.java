package dev.sayaya.handbook.client.usecase;

/**
 * 탭 스트립의 3단계 반응형 폴백을 순수 계산으로 결정한다.
 *
 * <p><b>책임:</b> 컨테이너 폭, 상단정렬(top) 탭들의 합계 폭, 하단정렬(bottom) 탭들의
 * 합계 폭, overflow 버튼 예약 폭을 받아 다음 중 하나를 반환한다.
 *
 * <ol>
 *   <li><b>평면</b>: 전체가 컨테이너에 들어감 → {@code showOverflow=false, scrollable=false}</li>
 *   <li><b>overflow</b>: 전체는 안 들어가지만 상단정렬 + overflow 예약은 들어감 → {@code showOverflow=true, scrollable=false}</li>
 *   <li><b>스크롤</b>: 상단정렬조차 넘침 → {@code showOverflow=true, scrollable=true}</li>
 * </ol>
 *
 * <p>폴백 정책상 overflow 에 수렴되는 것은 <b>하단정렬 그룹 전체</b>뿐이다 — 상단정렬 탭의
 * 일부가 overflow 로 들어가는 분해는 하지 않는다. 상단정렬은 항상 평면 또는 스크롤.</p>
 *
 * <p><b>의존관계:</b> 없음 (순수 함수, Dagger 빈 아님).</p>
 *
 * <p><b>주의:</b> {@code bottomWidth == 0} (하단정렬 공급자 없음) 일 때 overflow 버튼은
 * 필요 없고, 상단정렬만 스크롤 여부를 판정한다.</p>
 */
public final class ResponsiveOverflow {

    public static final class Result {
        public final boolean showOverflow;
        public final boolean scrollable;
        private Result(boolean showOverflow, boolean scrollable) {
            this.showOverflow = showOverflow;
            this.scrollable = scrollable;
        }
    }

    private ResponsiveOverflow() {}

    public static Result compute(double containerWidth, double topWidth, double bottomWidth, double reserveWidth) {
        if (bottomWidth <= 0) {
            // 하단정렬 없음: overflow 불필요, 상단정렬만 스크롤 여부 판정.
            return new Result(false, topWidth > containerWidth);
        }
        if (topWidth + bottomWidth <= containerWidth) return new Result(false, false);
        if (topWidth + reserveWidth <= containerWidth) return new Result(true, false);
        return new Result(true, true);
    }
}
