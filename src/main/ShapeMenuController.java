package main;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class ShapeMenuController extends BtnEffectHelper implements Initializable {

	@FXML public ColorPicker inputColor;
	@FXML public Button nUpBtn;
	@FXML public Button nDownBtn;
	@FXML public Pane displayPane;        	// 드래그 할 도형을 전시할 공간
	@FXML public Label nSides;

	@FXML public VBox line_Area;
	@FXML public VBox arrow_Area;
	@FXML public VBox star_Area;
	@FXML public VBox circle_Area;
	@FXML public VBox rect_Area;
	@FXML public VBox nPoly_Area;

	private NodeManager.Drag drag = new NodeManager.Drag();
	private Point2D areaLocation = null;						// 메뉴에 있는 도형의 원래 위치를 저장하기 위한 변수

	private DropShadow dropShadow = new DropShadow(4.5, Color.BLACK);

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// 기본 색 설정
		inputColor.setValue(Color.BLACK);

		// 색상 변경
		inputColor.setOnAction(change -> displayPane.getChildren().stream()
				.filter(container -> container instanceof Pane)
				.forEach(container -> {
					Node n = ((Pane) container).getChildren().get(0);
					Consumer<Color> changeColor;

					// 노드 타입에 따라 색상 변경 메소드를 참조함
					if (n instanceof Line) changeColor = ((Line) n)::setStroke;
					else if (n instanceof Pane) changeColor = ((Shape) ((Pane) n).getChildren().get(0))::setFill;
					else changeColor = ((Shape) n)::setFill;

					changeColor.accept(inputColor.getValue());
				}));

		// 기본 다각형 : 삼각형
		Pane polygon = NodeManager.createPolygon(3, 80, 80, inputColor.getValue());
		polygon.getChildren().get(0).setEffect(dropShadow);
		polygon.setMouseTransparent(true);
		nPoly_Area.getChildren().add(polygon);

	}

	// 변 개수 증가
	public void nUpBtnOnAction() {
		if(Integer.parseInt(nSides.getText()) < 20) {					// 최대 이십각형
			int sides = Integer.parseInt(nSides.getText()) + 1;
			nSides.setText(String.valueOf(sides));

			// 도형 생성 후 도형에 포함되어있는 마우스 이벤트 무력화
			Pane polygon = NodeManager.createPolygon(sides, 80, 80, inputColor.getValue());
			polygon.getChildren().get(0).setEffect(dropShadow);
			polygon.setMouseTransparent(true);

			nPoly_Area.getChildren().set(0, polygon);
		}
	}

	// 변 개수 감소
	public void nDownBtnOnAction() {
		if (Integer.parseInt(nSides.getText()) > 3) {					// 최소 삼각형
			int sides = Integer.parseInt(nSides.getText()) - 1;
			nSides.setText(String.valueOf(sides));

			// 도형 생성 후 도형에 포함되어있는 마우스 이벤트 무력화
			Pane polygon = NodeManager.createPolygon(sides, 80, 80, inputColor.getValue());
			polygon.getChildren().get(0).setEffect(dropShadow);
			polygon.setMouseTransparent(true);

			nPoly_Area.getChildren().set(0, polygon);
		}
	}

	//
	// *_Area에 해당하는 이벤트 핸들러
	//

	// 마우스가 눌렸을 때
	public void onAreaPressed(MouseEvent event) {
		drag.isDragging = false;

		// 현재 Area 받아오기
		Node area = (Node) event.getSource();
		area.toFront();

		// 드래그 시작 시의 좌표 값 저장
		drag.x = event.getSceneX();
		drag.y = event.getSceneY();

		// Area 현재 위치 저장
		areaLocation = new Point2D(area.getLayoutX(), area.getLayoutY());

		// 커서 변경
		area.setCursor(Cursor.CLOSED_HAND);

	}

	// 드래그 감지 되었을 때
	public void onAreaDragDetected() {drag.isDragging = true;}

	// 드래그 중일 때
	public void onAreaDragged(MouseEvent event) {
		// 현재 Area 받아오기
		Node area = (Node) event.getSource();

		// 드래그 변화량
		double deltaX = event.getSceneX() - drag.x;
		double deltaY = event.getSceneY() - drag.y;

		area.setLayoutX(area.getLayoutX() + deltaX);
		area.setLayoutY(area.getLayoutY() + deltaY);

		drag.x = event.getSceneX();
		drag.y = event.getSceneY();
	}

	public void onAreaReleased(MouseEvent event) {
		// 현재 Area 받아오기
		Node area = (Node) event.getSource();

		Pane slide = SlideManager.getController().slide;	// 메인 controller의 slide

		// 노드가 슬라이드 위에 있으면
		if(area.localToScene(area.getBoundsInLocal())
				.intersects(slide.localToScene(slide.getBoundsInLocal()))) {

			// 현재 Area에 들어있는 도형
			Shape shape;
			if(area.equals(nPoly_Area)) {
				Node n =((Pane) area).getChildren().get(0);
				shape = (Shape) ((Pane) n).getChildren().get(0);
			}
			else shape = (Shape) ((Pane) area).getChildren().get(0);

			// 추가할 도형
			Node node = null;

			// shape의 Scene에서의 X, Y 좌표값
			Point2D shapeXY = shape.localToScene(shape.getLayoutBounds().getMinX(), shape.getLayoutBounds().getMinY());
			Point2D nodeXY = new Point2D(
					shapeXY.getX() - slide.getLayoutX(),
					shapeXY.getY() - slide.getLayoutY());

			if(area.equals(line_Area)) node = NodeManager.createLine(
					nodeXY.getX(), nodeXY.getY(), nodeXY.getX() + 80, nodeXY.getY(), (Color) shape.getStroke());
			else if(area.equals(arrow_Area)) node = NodeManager.createArrow(48, 48, (Color) shape.getFill());
			else if(area.equals(star_Area)) node = NodeManager.createStar(76, 72.36, (Color) shape.getFill());
			else if(area.equals(circle_Area)) node = NodeManager.createCircle(40, (Color) shape.getFill());
			else if(area.equals(rect_Area)) node = NodeManager.createRectangle(80, 80, (Color) shape.getFill());
			else if(area.equals(nPoly_Area)) node = NodeManager.createPolygon(Integer.parseInt(nSides.getText()), 80, 80, (Color) shape.getFill());

			if(node != null) {
				if(node instanceof Line) {
					node.setLayoutX(0);
					node.setLayoutY(0);
				} else {
					node.setLayoutX(shapeXY.getX() - slide.getLayoutX());
					node.setLayoutY(shapeXY.getY() - slide.getLayoutY());
				}

				SlideManager.addNode(node);
			}
		}

		// 원래 위치로
		area.setLayoutX(areaLocation.getX());
		area.setLayoutY(areaLocation.getY());

		// 커서 변경
		area.setCursor(Cursor.HAND);
	}

	public void onAreaEntered(MouseEvent event) {
		// 현재 Area 받아오기
		Node area = (Node) event.getSource();

		// 커서 변경
		area.setCursor(Cursor.HAND);
	}

	public void onAreaExited(MouseEvent event) {
		// 현재 Area 받아오기
		Node area = (Node) event.getSource();

		// 커서 변경
		area.setCursor(Cursor.DEFAULT);
	}

}
