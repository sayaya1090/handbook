package dev.sayaya.handbook.client.usecase.arrow;

import elemental2.dom.DomGlobal;
import lombok.Getter;

import java.util.*;
import java.util.stream.Collectors;

/*
 * private static List<Point> findPath(Rectangle attributeRect, Rectangle tableRect) 함수와
 * private static List<Point> findPath(Point start, Point end) 함수를 만들어 보자.
 * 단, attributeRect에서는 좌변의 leftMid()점 또는 우변의 rightMid()점만 사용할 수 있고 상변과 하변은 사용하면 안 돼.
 * 그리고 각 변에서 출발/도착할 때에는 반드시 그 변과 직교하는 방향으로 선이 그어져서 만나야 하고, 평행하는 방향으로 선이 그어져서 만나면 안 돼.
 * findPath(Rectangle attributeRect, Rectangle tableRect) 각 사각형의 점/선분(origin)과, 점/선분에서 Margin만큼 사각형 바깥쪽으로 떨어진 가상의 점/선분(virt)을 만든다.
 * attributeRect의 virt는 x축으로 Margin만큼 떨어진 점이다. attributeRect의 virt를 선택할 때, 좌변은 -Margin 한 점이고 우변은 +Margin한 점이야.
 * 그리고 모서리에 연결되는 경우를 피하기 위해 가상의 선분은 길이가 원래 변보다 Margin*2만큼 짧게 잡는다.
 * attributeRect 의 origin에서 시작하여 두 사각형의 virt를 지나 tableRect의 origin으로 가는 경로를 찾는다.
 * 맨하탄 거리가 가장 짧은 최적의 점들을 구한 다음에는(동점이 여럿일 수 있음), 그 거리를 만드는 두 선분 상의 점을 각각 선택하여 실제 경로를 생성하는
 * findPath(Point originStart, Point virtStart, Point virtEnd, Point originEnd)함수로 경로를 만든다.
 * 이 함수는 originStart에서 출발하여 virtStart를 지나서, virtEnd를 거쳐 originEnd에 도달하는 경로를 생성한다.
 * origin과 virt는 x축이나 y축 좌표 둘 중 하나가 항상 같으므로, 두 쌍의 경로 사이에는 전환점이 생성될 수 없다.
 * virtStart와 virtEnd 사이에 방향을 전환하기 위한 몇개의 점이 추가될 수 있는데, 방향 전환은 90도로만 가능하다.
 * origin에서 virt로 이동한 다음, 같은 방향으로 직진하는 경로의 경우 virt 점을 제거하여 simplyfy한다.
 * 최종적으로 Simplyfy된 여러 경로 중 목적지로 가기 위한 최단 경로를 찾는데, 거리가 같은 경우 방향 전환을 최소화하고,
 * 그래도 경로가 여럿일 경우에는 점과 점 사이의 거리가 균일한 경로를 선택한다.
 * findPath(Point originStart, Point virtStart, Point virtEnd, Point originEnd) 이 함수는 다른데서 활용할 예정이니 꼭 만들어 줘.
 */
public class ArrowFactory {
    private static final int MARGIN = 30;

    private static int manhattanDistance(Point p1, Point p2) { return Math.abs(p1.x() - p2.x()) + Math.abs(p1.y() - p2.y()); }
    private static double euclideanDistance(Point p1, Point p2) { return Math.sqrt(Math.pow(p1.x() - p2.x(), 2) + Math.pow(p1.y() - p2.y(), 2)); }
    private static int clamp(int value, int min, int max) { return Math.max(min, Math.min(value, max)); }
    private record ClosestPointResult(Point closestPointOnSegment, int manhattanDistance) {}
    private static ClosestPointResult getClosestPointOnSegmentManhattan(Point externalPoint, Point segP1, Point segP2) {
        int px = externalPoint.x(); int py = externalPoint.y(); Point closestPt;
        if (segP1.y() == segP2.y()) {
            int segY = segP1.y(); int segMinX = Math.min(segP1.x(), segP2.x()); int segMaxX = Math.max(segP1.x(), segP2.x());
            int closestX = clamp(px, segMinX, segMaxX); closestPt = new Point(closestX, segY);
        } else if (segP1.x() == segP2.x()) {
            int segX = segP1.x(); int segMinY = Math.min(segP1.y(), segP2.y()); int segMaxY = Math.max(segP1.y(), segP2.y());
            int closestY = clamp(py, segMinY, segMaxY); closestPt = new Point(segX, closestY);
        } else { throw new IllegalArgumentException("선분이 수평 또는 수직이 아닙니다: " + segP1 + " to " + segP2); }
        int dist = manhattanDistance(externalPoint, closestPt); return new ClosestPointResult(closestPt, dist);
    }

    private static List<Point> simplifyPath(List<Point> pathPoints) {
        if (pathPoints == null || pathPoints.size() < 2) return new ArrayList<>(pathPoints != null ? pathPoints : Collections.emptyList());
        List<Point> distinctPoints = new ArrayList<>(); distinctPoints.add(pathPoints.get(0));
        for (int i = 1; i < pathPoints.size(); i++) if (!pathPoints.get(i).equals(pathPoints.get(i-1))) distinctPoints.add(pathPoints.get(i));
        if (distinctPoints.size() < 3) return distinctPoints;
        List<Point> simplifiedPath = new ArrayList<>(); simplifiedPath.add(distinctPoints.get(0));
        for (int i = 1; i < distinctPoints.size() - 1; i++) { Point pP=distinctPoints.get(i-1),pC=distinctPoints.get(i),pN=distinctPoints.get(i+1); if (!((pP.x()==pC.x()&&pC.x()==pN.x())||(pP.y()==pC.y()&&pC.y()==pN.y())) ) simplifiedPath.add(pC); }
        simplifiedPath.add(distinctPoints.get(distinctPoints.size()-1)); return simplifiedPath;
    }

    private static boolean has180Turn(Point p1, Point p2, Point p3) {
        if (p1.equals(p2) || p2.equals(p3)) return false;
        if (p1.y()==p2.y()&&p2.y()==p3.y()) return (p2.x()-p1.x())*(p3.x()-p2.x())<0;
        if (p1.x()==p2.x()&&p2.x()==p3.x()) return (p2.y()-p1.y())*(p3.y()-p2.y())<0;
        return false;
    }

    private static List<Point> buildAndSimplifyPathStructure(Point oS, Point vS, Point vE, Point oE, boolean useXFirstIntermediate) {
        List<Point> path = new ArrayList<>(); path.add(oS); if (!oS.equals(vS)) path.add(vS);
        List<Point> subPathVsToVe = new ArrayList<>(); subPathVsToVe.add(vS);
        if (!vS.equals(vE)) { if (vS.x()!=vE.x()&&vS.y()!=vE.y()) { if(useXFirstIntermediate) subPathVsToVe.add(new Point(vE.x(),vS.y())); else subPathVsToVe.add(new Point(vS.x(),vE.y()));} subPathVsToVe.add(vE); }
        List<Point> simplifiedSubPathVsToVe = simplifyPath(subPathVsToVe);
        Point lastPointInPath = path.get(path.size()-1);
        for (int i=0; i<simplifiedSubPathVsToVe.size(); i++) { Point p=simplifiedSubPathVsToVe.get(i); if(i==0&&lastPointInPath.equals(p))continue; path.add(p); }
        if(!path.get(path.size()-1).equals(oE))path.add(oE);
        if(path.size()<2)return path; List<Point>fDP=new ArrayList<>();fDP.add(path.get(0));for(int i=1;i<path.size();i++)if(!path.get(i).equals(path.get(i-1)))fDP.add(path.get(i));return fDP;
    }

    public static List<Point> findPath(Point oS, Point vS, Point vE, Point oE) {
        List<Point> pathUsingP1 = null; List<Point> pathUsingP2 = null;
        boolean vsVeAligned = (vS.x()==vE.x()||vS.y()==vE.y());
        List<Point> candidateKeyPoints1 = new ArrayList<>(List.of(oS,vS)); if(!vsVeAligned)candidateKeyPoints1.add(new Point(vE.x(),vS.y())); candidateKeyPoints1.add(vE); candidateKeyPoints1.add(oE);
        List<Point> distinctKeyPoints1 = new ArrayList<>(); if(!candidateKeyPoints1.isEmpty()){distinctKeyPoints1.add(candidateKeyPoints1.get(0));for(int i=1;i<candidateKeyPoints1.size();++i)if(!candidateKeyPoints1.get(i).equals(candidateKeyPoints1.get(i-1)))distinctKeyPoints1.add(candidateKeyPoints1.get(i));}
        boolean p1PathHas180Turn=false; if(distinctKeyPoints1.size()>=3)for(int i=0;i<=distinctKeyPoints1.size()-3;i++)if(has180Turn(distinctKeyPoints1.get(i),distinctKeyPoints1.get(i+1),distinctKeyPoints1.get(i+2))){p1PathHas180Turn=true;break;}
        if(!p1PathHas180Turn)pathUsingP1=buildAndSimplifyPathStructure(oS,vS,vE,oE,true);
        if(!vsVeAligned){ List<Point>candidateKeyPoints2=new ArrayList<>(List.of(oS,vS));candidateKeyPoints2.add(new Point(vS.x(),vE.y()));candidateKeyPoints2.add(vE);candidateKeyPoints2.add(oE);
            List<Point>distinctKeyPoints2=new ArrayList<>();if(!candidateKeyPoints2.isEmpty()){distinctKeyPoints2.add(candidateKeyPoints2.get(0));for(int i=1;i<candidateKeyPoints2.size();++i)if(!candidateKeyPoints2.get(i).equals(candidateKeyPoints2.get(i-1)))distinctKeyPoints2.add(candidateKeyPoints2.get(i));}
            boolean p2PathHas180Turn=false;if(distinctKeyPoints2.size()>=3)for(int i=0;i<=distinctKeyPoints2.size()-3;i++)if(has180Turn(distinctKeyPoints2.get(i),distinctKeyPoints2.get(i+1),distinctKeyPoints2.get(i+2))){p2PathHas180Turn=true;break;}
            if(!p2PathHas180Turn)pathUsingP2=buildAndSimplifyPathStructure(oS,vS,vE,oE,false);
        }else if(p1PathHas180Turn)pathUsingP1=null;
        if(pathUsingP1!=null&&pathUsingP2!=null){EvaluatedPath evalP1=new EvaluatedPath(pathUsingP1);EvaluatedPath evalP2=new EvaluatedPath(pathUsingP2);return evalP1.compareTo(evalP2)<=0?pathUsingP1:pathUsingP2;}
        else if(pathUsingP1!=null)return pathUsingP1; else if(pathUsingP2!=null)return pathUsingP2; else return Collections.emptyList();
    }

    private record ConnectionCandidate(Point originStart, Point virtStart, Point originEnd, Point virtEnd, int virtPointsManhattanDistance) {}

    /**
     * 생성된 경로와 평가 지표를 저장하고 비교 기능을 제공하는 클래스입니다.
     * 굴절 횟수를 최우선으로, 그 다음 총 길이, 그 다음 선분 길이 균일도 순으로 비교합니다.
     */
    private static class EvaluatedPath implements Comparable<EvaluatedPath> {
        final List<Point> path; final double totalLength; final int turns; final double segmentLengthVariance;
        EvaluatedPath(List<Point> path) {
            this.path = path; List<Double> sl = new ArrayList<>(); if (path.size() >= 2) for (int i=0; i<path.size()-1; ++i) sl.add(euclideanDistance(path.get(i), path.get(i+1)));
            this.totalLength = sl.stream().mapToDouble(Double::doubleValue).sum(); int ct=0; if (path.size() >= 3) for (int i=1; i<path.size()-1; ++i) { Point p0=path.get(i-1),p1=path.get(i),p2=path.get(i+1); int dx1=p1.x()-p0.x(),dy1=p1.y()-p0.y(),dx2=p2.x()-p1.x(),dy2=p2.y()-p1.y(); if((dx1!=0&&dy2!=0&&dx2==0&&dy1==0)||(dy1!=0&&dx2!=0&&dx1==0&&dy2==0)) ct++;}
            this.turns = ct; if (sl.size()>1){double m=this.totalLength/sl.size(),ssd=0;for(double l:sl)ssd+=Math.pow(l-m,2);this.segmentLengthVariance=ssd/sl.size();}else this.segmentLengthVariance=0;
        }
        @Override public int compareTo(EvaluatedPath other){
            // 1. 굴절 횟수가 적은 순서 (최우선)
            if (this.turns != other.turns) {
                return Integer.compare(this.turns, other.turns);
            }
            // 2. 총 경로 길이가 짧은 순서
            if (Math.abs(this.totalLength - other.totalLength) > 1e-9) {
                return Double.compare(this.totalLength, other.totalLength);
            }
            // 3. 선분 길이가 더 균일한 순서 (분산 값이 작은 순서)
            return Double.compare(this.segmentLengthVariance, other.segmentLengthVariance);
        }
    }
    private enum TargetSideType { LEFT, RIGHT, TOP, BOTTOM }
    private record VirtualSegmentDef(Point p1, Point p2, TargetSideType type, Rectangle originalTargetRect) {}

    /**
     * 두 사각형 간의 최적 경로를 찾습니다.
     * 굴절 횟수를 최소화하는 것을 우선으로 경로를 선택합니다.
     */
    public static List<Point> findPath(Rectangle attributeRect, Rectangle targetRect) {
        List<ConnectionCandidate> candidates = new ArrayList<>();
        Point attrLeftOrigin = attributeRect.leftMid(); Point attrLeftVirt = new Point(attrLeftOrigin.x() - MARGIN, attrLeftOrigin.y());
        Point attrRightOrigin = attributeRect.rightMid(); Point attrRightVirt = new Point(attrRightOrigin.x() + MARGIN, attrRightOrigin.y());
        List<Map.Entry<Point, Point>> startOriginVirtPairs = List.of(new AbstractMap.SimpleEntry<>(attrLeftOrigin, attrLeftVirt), new AbstractMap.SimpleEntry<>(attrRightOrigin, attrRightVirt));
        List<VirtualSegmentDef> targetVirtualSegments = new ArrayList<>();
        int trX=targetRect.x(),trY=targetRect.y(),trW=targetRect.w(),trH=targetRect.h();
        if(trH>=2*MARGIN){targetVirtualSegments.add(new VirtualSegmentDef(new Point(trX-MARGIN,trY+MARGIN),new Point(trX-MARGIN,trY+trH-MARGIN),TargetSideType.LEFT,targetRect));targetVirtualSegments.add(new VirtualSegmentDef(new Point(trX+trW+MARGIN,trY+MARGIN),new Point(trX+trW+MARGIN,trY+trH-MARGIN),TargetSideType.RIGHT,targetRect));}
        if(trW>=2*MARGIN){targetVirtualSegments.add(new VirtualSegmentDef(new Point(trX+MARGIN,trY-MARGIN),new Point(trX+trW-MARGIN,trY-MARGIN),TargetSideType.TOP,targetRect));targetVirtualSegments.add(new VirtualSegmentDef(new Point(trX+MARGIN,trY+trH+MARGIN),new Point(trX+trW-MARGIN,trY+trH+MARGIN),TargetSideType.BOTTOM,targetRect));}

        // 1. 모든 가능한 연결 후보 생성 (virtStart-virtEnd 맨해튼 거리 필터링 없이)
        for(Map.Entry<Point,Point>startPair:startOriginVirtPairs){
            Point oS=startPair.getKey(),vS=startPair.getValue();
            for(VirtualSegmentDef segDef:targetVirtualSegments){
                ClosestPointResult cpr=getClosestPointOnSegmentManhattan(vS,segDef.p1(),segDef.p2());
                Point vE=cpr.closestPointOnSegment(); int dist=cpr.manhattanDistance(); Point oE;
                Rectangle ot=segDef.originalTargetRect();
                oE = switch (segDef.type()) {
                    case LEFT -> new Point(ot.x(), vE.y());
                    case RIGHT -> new Point(ot.x() + ot.w(), vE.y());
                    case TOP -> new Point(vE.x(), ot.y());
                    case BOTTOM -> new Point(vE.x(), ot.y() + ot.h());
                    default -> throw new IllegalStateException("알 수 없는 대상 변 타입: " + segDef.type());
                };
                candidates.add(new ConnectionCandidate(oS,vS,oE,vE,dist));
            }
        }
        if(candidates.isEmpty()) return Collections.emptyList(); // 그래도 후보가 없으면 빈 경로 반환
        // 2. 모든 후보에 대해 경로 생성 및 평가
        List<EvaluatedPath> evaluatedPaths = new ArrayList<>();
        for(ConnectionCandidate cand:candidates){ // virtStart-virtEnd 맨해튼 거리로 필터링하지 않음!
            List<Point>pathPts=findPath(cand.originStart(),cand.virtStart(),cand.virtEnd(),cand.originEnd());
            if(pathPts!=null && pathPts.size()>=2) { // 유효한 경로(180도 회전 없음 등)만 평가 대상
                evaluatedPaths.add(new EvaluatedPath(pathPts));
            }
        }
        if(evaluatedPaths.isEmpty()) return Collections.emptyList(); // 유효한 경로가 하나도 없는 경우
        // 3. 평가된 경로들을 새 비교 기준(굴절 우선)에 따라 정렬하고 최적 경로 반환
        Collections.sort(evaluatedPaths);
        return evaluatedPaths.get(0).path;
    }

    public static Arrow createArrow(Rectangle attributeRect, Rectangle tableRect) {
        List<Point> pathPointsFromFindPath = findPath(attributeRect, tableRect);
        if(pathPointsFromFindPath.isEmpty()) return null;
        return createArrow(pathPointsFromFindPath);
    }
    public static Arrow createArrow(List<Point> points) {
        return new Arrow(points);
    }
}