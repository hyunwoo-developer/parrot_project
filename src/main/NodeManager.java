package main;

import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;

import java.util.ArrayList;
import java.util.List;

public class NodeManager {

	private static List<Node> selectedNodes = new ArrayList<>();    // 선택된 노드

	private static Drag drag = new Drag();	

	public static ResizeBox resizeBox;	// 리사이즈 시 생기는 편집 사각형

	public static ContextMenu contextMenu;	// 노드를 선택하고 우클릭 시 생기는 메뉴창
	public static boolean flag = false;	// 메뉴창이 떠있는지 없는지 확인 플래그

	private NodeManager() {
	}

	// Text 생성(링크 X)
	public static Label createText(String text, double textSize, Color color) {
		Label label = new Label();
		label.setText(text);
		label.setStyle("-fx-font-size:" + textSize + ";");
		label.setTextFill(color);

		makeDraggable(label);

		return label;
	}

	// Text 생성(링크 O)
	public static Label createText(String text, double textSize, Color color, String link) {
		Label label = NodeManager.createText(text, textSize, color);
		label.setUserData(link);

		return label;
	}

	// Image 생성
	public static HBox createImage(String imgSrc) {
		Image image = new Image("file:" + imgSrc);

		return createImage(image);
	}

	// 이미지 사이즈 조정
	public static HBox createImage(Image image) {
		ImageView imageView = new ImageView(image);
		HBox hBox = new HBox();                            // 컨테이너(Border 적용하기 위해서)

		// 이미지가 너무 클 경우 강제로 크기를 줄임
		double width = image.getWidth();
		double height = image.getHeight();
		if (width > SlideManager.SLIDE_WIDTH) {
			imageView.setFitWidth(400);
			imageView.setFitHeight(height * (400 / width));
		}

		if (height > SlideManager.SLIDE_HEIGHT) {
			imageView.setFitHeight(300);
			imageView.setFitWidth(width * (300 / height));
		}

		hBox.getChildren().add(imageView);

		makeDraggable(hBox);

		return hBox;
	}

	// 이미지를 HBox 안에 넣기
	public static HBox createImage(Image image, double width, double height) {
		HBox hBox = createImage(image);
		ImageView imageView = (ImageView) hBox.getChildren().get(0);

		imageView.setFitWidth(width);
		imageView.setFitHeight(height);

		return hBox;
	}

	// Line 기본 값 : 0, 0, 80, 0
	// Line 생성
	public static Line createLine(double startX, double startY, double endX, double endY, Color color) {
		Line line = new Line(startX, startY, endX, endY);

		line.setStroke(color);            // 색상 설정
		line.setStrokeWidth(3.0);        // 두께 설정

		makeDraggable(line);

		return line;
	}

	// 화살표 기본 값 : 48, 48
	// 화살표 생성
	public static SVGPath createArrow(double width, double height, Color color) {
		SVGPath arrow = new SVGPath();

		// 화살표 그리기
		arrow.setContent(
				calculateArrowContent(width, height)		// content 생성
		);

		// 색상 설정
		arrow.setFill(color);

		makeDraggable(arrow);

		return arrow;
	}

	// 별 기본 값 : 76, 72.36
	// 별 생성(두 타원을 이용하여 생성)
	public static SVGPath createStar(double width, double height, Color color) {
		SVGPath star = new SVGPath();

		// 오차 보정(width와 height를 그대로 넣었을 때 오차가 특정 비율로 발생하는 관계로 이를 보정함)
		double real_width = width * 1.0250856297497568;
		double real_height = height * 1.1055727879561124;

		// 별 그리기
		star.setContent(
				calculateStarContent(real_width, real_height)		// content 생성
		);

		// 색상 설정
		star.setFill(color);

		makeDraggable(star);

		return star;
	}

	// 원 기본 값 : 40
	// 원 생성
	public static Ellipse createCircle(double radius, Color color) {
		return createEllipse(radius, radius, color);
	}

	// 타원 생성
	public static Ellipse createEllipse(double radiusX, double radiusY, Color color) {
		Ellipse ellipse = new Ellipse(radiusX, radiusY);

		// 색상 설정
		ellipse.setFill(color);

		ellipse.setCenterX(radiusX);
		ellipse.setCenterY(radiusY);

		makeDraggable(ellipse);

		return ellipse;
	}

	// 직사각형 기본 값 : 80, 80
	// 직사각형 생성
	public static Rectangle createRectangle(double width, double height, Color color) {
		Rectangle rectangle = new Rectangle(width, height);

		// 색상 설정
		rectangle.setFill(color);

		makeDraggable(rectangle);

		return rectangle;
	}

	// 다각형 기본 값 : 3, 80, 80
	// 다각형 생성
	public static Pane createPolygon(int sides, double width, double height, Color color) {
		Polygon polygon = new Polygon();

		// points 생성 및 적용
		List<Double> points = calculatePolygonPoints(sides, width, height);
		polygon.getPoints().setAll(points);

		// 색상 설정
		polygon.setFill(color);

		// 컨테이너 설정
		Pane container = new Pane();
		container.setPrefWidth(width);
		container.setPrefHeight(height);
		container.getChildren().add(polygon);

		makeDraggable(container);

		return container;
	}

	// 다각형 꼭짓점 좌표 계산
	public static List<Double> calculatePolygonPoints(int sides, double width, double height) {
		List<Double> points = new ArrayList<>();

		double angle = Math.PI * 2 / sides;

		// (중심좌표) + (i 바퀴 째의 x/y 좌표) - (파이/2 radian <시작점을 12시 방향으로 하기 위해서>)
		for (int i = 0; i < sides; i++) {
			double x = (width / 2) + (width / 2) * Math.cos(i * angle - (Math.PI / 2));
			double y = (height / 2) + (height / 2) * Math.sin(i * angle - (Math.PI / 2));

			points.add(x);
			points.add(y);
		}

		return points;
	}

	// 화살표 content 생성
	private static String calculateArrowContent(double width, double height) {
		return "M 0 0 " +										// 시작점 설정
				"L" + width + " " + (height / 2) + "  " +		// <--------
				"0 " + height + "  " +							//  선 그리기
				(width / 3) + " " + (height / 2) +				// -------->
				" Z";											// 마무리
	}

	// 별 content 생성
	private static String calculateStarContent(double width, double height) {

		// 중심 좌표
		double centerX = width / 2;
		double centerY = height / 2;

		// 각도
		double angle = Math.PI / 5;

		StringBuilder content = new StringBuilder();

		for (int i = 0; i < 10; i++) {
			// i가 짝수이면 바깥쪽 타원의 장축/단축
			// i가 홀수이면 안쪽 타원의 장축/단축

			double rX = (i % 2) == 0 ? centerX : centerX * 0.3;
			double rY = (i % 2) == 0 ? centerY : centerY * 0.3;

			// Polygon과 유사한 원리로
			double starX = centerX + Math.cos(2 * i * angle - (Math.PI / 2)) * rX;
			double starY = centerY + Math.sin(2 * i * angle - (Math.PI / 2)) * rY;

			if (i == 0) {					// 시작점 설정
				content.append("M ")
						.append(starX)
						.append(" ")
						.append(starY)
						.append(" L ");
			} else {						// 선 그리기
				content.append(starX)
						.append(" ")
						.append(starY)
						.append("  ");
			}
		}

		content.append("Z");				// 마무리

		return content.toString();
	}

	// 드래그 관련 이벤트 처리
	public static void makeDraggable(Node node) {
		node.setOnMousePressed(event -> {
			drag.isDragging = false;

			// 드래그 시작 시의 마우스 좌표를 받는다
			drag.x = event.getSceneX();
			drag.y = event.getSceneY();

			// 현재 선택되어있지 않은 노드이면
			if (!selectedNodes.contains(node)) {
				// selectedNodes를 비우고
				selectedNodes.forEach(n -> n.getStyleClass().remove("selected"));
				selectedNodes.clear();

				// 현재 노드를 추가한다
				node.getStyleClass().add("selected");
				selectedNodes.add(node);
			}

			if (event.getClickCount() == 2) {

				selectedNodes.get(selectedNodes.indexOf(node)).getStyleClass().remove("selected");

				if (SlideManager.getCurrentSlide().getChildren().contains(resizeBox)) {
					SlideManager.getCurrentSlide().getChildren().removeAll(resizeBox);
				}

				if(!(node instanceof Label)) {			// 라벨을 제외한 노드들
					// resizeBox 생성
					resizeBox = new ResizeBox(node);

					if(!(node instanceof Line)) {							// Line이 아닌 노드일 때
						// 크기 설정
						resizeBox.setSize(
								node.getBoundsInParent().getWidth(),
								node.getBoundsInParent().getHeight()
						);
						// 위치 설정
						resizeBox.setLayoutX(node.getBoundsInParent().getMinX() - 10);
						resizeBox.setLayoutY(node.getBoundsInParent().getMinY() - 10);
					}

					SlideManager.getCurrentSlide().getChildren().add(resizeBox);
				}

				// 더블 클릭으로 리사이즈 모드 해제
				if(resizeBox != null) {
					resizeBox.setOnMouseClicked(exit -> {
						if (exit.getClickCount() == 2) {
							SlideManager.getCurrentSlide().getChildren().removeAll(resizeBox);
	
							if(!selectedNodes.contains(node))
								selectedNodes.add(node);
								node.getStyleClass().add("selected");
						}
					});
				}
			}
		});

		node.setOnDragDetected(event -> drag.isDragging = true);

		node.setOnMouseDragged(event -> {
			double deltaX = event.getSceneX() - drag.x;
			double deltaY = event.getSceneY() - drag.y;
			selectedNodes.forEach(n -> {
				if(n instanceof Line) {
					Line l = (Line) n;
					l.setStartX(l.getStartX() + deltaX);
					l.setStartY(l.getStartY() + deltaY);
					l.setEndX(l.getEndX() + deltaX);
					l.setEndY(l.getEndY() + deltaY);
				} else {
					n.setLayoutX(n.getLayoutX() + deltaX);
					n.setLayoutY(n.getLayoutY() + deltaY);
				}
			});
			drag.x = event.getSceneX();
			drag.y = event.getSceneY();
		});

		// 우클릭 시 메뉴 관련
		final MenuItem modify = new MenuItem("Modify");
		final MenuItem toFront = new MenuItem("To Front");
		final MenuItem toBack = new MenuItem("To Back");

		// 모디파이를 클릭했을 때
		modify.setOnAction(event -> SlideManager.getController().onModifyTextCalled(node));

		// toFront를 클릭했을 때
		toFront.setOnAction(event -> Main.toFront.run());

		// toBack을 클릭했을 때
		toBack.setOnAction(event -> Main.toBack.run());

		node.setOnMouseClicked(event -> {
			if (event.getButton().equals(MouseButton.SECONDARY)) {

				if (flag) {
					if (contextMenu.isShowing()) contextMenu.hide();
				}

				if (NodeManager.getSelectedNodes().size() == 1 && node instanceof Label) {
					contextMenu = new ContextMenu(modify, toFront, toBack);
				} else {
					contextMenu = new ContextMenu(toFront, toBack);
				}

				// 메뉴 띄우기
				contextMenu.show(node, event.getScreenX(), event.getScreenY());
				flag = true;
			}
		});

		node.setOnMouseEntered(event -> node.setCursor(Cursor.HAND));
		node.setOnMouseExited(event -> node.setCursor(Cursor.DEFAULT));

	}

	// Getter
	public static List<Node> getSelectedNodes() {
		return selectedNodes;
	}


	/////////////
	// Classes //
	/////////////

	// 리사이즈 박스
	public static class ResizeBox extends Region {

		// 사각형들의 위치를 구분하기 한 값
		private enum Position {
			LineLeft, LineRight,
			TopLeft, TopRight, BottomRight, BottomLeft
		}

		// 꼭짓점 위오른쪽, 위왼쪽, 아래오른쪽, 아래왼쪽
		private Rectangle tr, tl, br, bl;

		// 리사이즈 라인
		private Line top, right, bottom, left;

		// 꼭짓점 크기
		private double cornerSize = 10;

		private double x, y;

		private Node node;
		public ResizeBox (Node node) {
			this.node = node;

			if(node instanceof Line) {				// node가 Line일 때
				this.setLayoutX(0);
				this.setLayoutY(0);

				// 선 양끝에 사각형 생성
				tl = buildCorner(Position.LineLeft);
				tr = buildCorner(Position.LineRight);

				// 위치 bind
				tl.layoutXProperty().bind(((Line) node).startXProperty()
						.subtract(cornerSize / 2));
				tl.layoutYProperty().bind(((Line) node).startYProperty()
						.subtract(cornerSize / 2));
				tr.layoutXProperty().bind(((Line) node).endXProperty()
						.subtract(cornerSize / 2));
				tr.layoutYProperty().bind(((Line) node).endYProperty()
						.subtract(cornerSize / 2));

				tl.setCursor(Cursor.CROSSHAIR);
				tr.setCursor(Cursor.CROSSHAIR);

				getChildren().addAll(tl, tr);
			} else {								// Line이 아닌 나머지일 때
				// 꼭짓점 사각형 생성
				tr = buildCorner(Position.TopRight);
				tl = buildCorner(Position.TopLeft);
				br = buildCorner(Position.BottomRight);
				bl = buildCorner(Position.BottomLeft);

				tr.setCursor(Cursor.CROSSHAIR);
				tl.setCursor(Cursor.CROSSHAIR);
				br.setCursor(Cursor.CROSSHAIR);
				bl.setCursor(Cursor.CROSSHAIR);

				// 테두리 Line 생성
				top = buildLine();
				bottom = buildLine();
				left = buildLine();
				right = buildLine();

				// 테두리 위치 Bind
				top.startXProperty().bind(tl.xProperty().add(cornerSize));
				top.startYProperty().bind(tl.yProperty().add(cornerSize));
				top.endXProperty().bind(tr.xProperty());
				top.endYProperty().bind(tr.yProperty().add(cornerSize));
				left.startXProperty().bind(top.startXProperty());
				left.startXProperty().bind(top.startYProperty());
				left.endXProperty().bind(bl.xProperty().add(cornerSize));
				left.endYProperty().bind(bl.yProperty());
				bottom.startXProperty().bind(left.endXProperty());
				bottom.startYProperty().bind(left.endYProperty());
				bottom.endXProperty().bind(br.xProperty());
				bottom.endYProperty().bind(br.yProperty());
				right.startXProperty().bind(top.endXProperty());
				right.startYProperty().bind(top.endYProperty());
				right.endXProperty().bind(bottom.endXProperty());
				right.endYProperty().bind(bottom.endYProperty());

				getChildren().addAll(top, bottom, left, right, tr, tl, br, bl);
			}

		}

		// ResizeBox 사이즈 변경
		public void setSize (double width, double height) {
			// 바뀐 사이즈에 맞춰 사각형들의 위치를 조정해준다
			tl.setX(0);
			tl.setY(0);

			tr.setX(width + cornerSize);
			tr.setY(0);

			bl.setX(0);
			bl.setY(height + cornerSize);

			br.setX(width + cornerSize);
			br.setY(height + cornerSize);
		}

		// 마우스 좌표 저장
		private void setMouse(double x, double y) {
			this.x = x;
			this.y = y;
		}

		private double getMouseX () {
			return x;
		}

		private double getMouseY () {
			return y;
		}

		// 꼭짓점 만들기
		private Rectangle buildCorner (final Position pos) {

			// 꼭짓점 초기 설정
			Rectangle r = new Rectangle();
			r.setWidth(cornerSize);
			r.setHeight(cornerSize);
			r.setStroke(Color.rgb(0, 0, 0, 0.75));
			r.setFill(Color.rgb(0, 0, 0, 0.25));
			r.setStrokeWidth(1);

			r.setStrokeType(StrokeType.INSIDE);


			// 마우스 이벤트 처리
			r.setOnMousePressed(event -> setMouse(event.getSceneX(), event.getSceneY()));

			if(node instanceof Line) {				// Line일 때
				Line line = (Line) node;
				r.setOnMouseDragged(event -> {
					// 마우스 변화량
					double dx = event.getSceneX() - getMouseX();
					double dy = event.getSceneY() - getMouseY();

					setMouse(event.getSceneX(), event.getSceneY());

					Point2D startXY = new Point2D(line.getStartX(), line.getStartY());
					Point2D endXY = new Point2D(line.getEndX(), line.getEndY());

					// line 길이 변경
					if(pos == Position.LineLeft) {
						line.setStartX(startXY.getX() + dx);
						line.setStartY(startXY.getY() + dy);
					} else if(pos == Position.LineRight) {
						line.setEndX(endXY.getX() + dx);
						line.setEndY(endXY.getY() + dy);
					}
				});
			} else {
				r.setOnMouseDragged(event -> {
					// 마우스 변화량
					double dx = event.getSceneX() - getMouseX();
					double dy = event.getSceneY() - getMouseY();

					setMouse(event.getSceneX(), event.getSceneY());

					// [0]너비, [1]높이
					double[] dwh = wh();

					// 리사이즈 할 때마다 꼭짓점 위치변환
					if (pos == Position.TopLeft) {				// 왼쪽 상단
						// 다른 꼭짓점과의 거리가 1 이상일 때
						if (dwh[0] - dx > 1 && dwh[1] - dy > 1) {
							// 노드 이동 및 크기 변경
							node.setLayoutX(node.getLayoutX() + dx);
							node.setLayoutY(node.getLayoutY() + dy);

							resizeBox.setLayoutX(node.getLayoutX() - cornerSize);
							resizeBox.setLayoutY(node.getLayoutY() - cornerSize);

							setSize(dwh[0] - dx, dwh[1] - dy);
						}
					} else if (pos == Position.TopRight) {		// 오른쪽 상단
						// 다른 꼭짓점과의 거리가 1 이상일 때
						if (dwh[0] + dx > 1 && dwh[1] - dy > 1) {
							// 노드 이동 및 크기 변경
							node.setLayoutY(node.getLayoutY() + dy);

							resizeBox.setLayoutY(node.getLayoutY() - cornerSize);

							setSize(dwh[0] + dx, dwh[1] - dy);
						}
					} else if (pos == Position.BottomRight) {	// 오른쪽 하단
						// 다른 꼭짓점과의 거리가 1 이상일 때
						if (dwh[0] + dx > 1 && dwh[1] + dy > 1) {
							// 노드 크기 변경
							setSize(dwh[0] + dx, dwh[1] + dy);
						}
					} else if (pos == Position.BottomLeft) {	// 왼쪽 하단
						// 다른 꼭짓점과의 거리가 1 이상일 때
						if (dwh[0] - dx > 1 && dwh[1] + dy > 1) {
							// 노드 이동 및 크기 변경
							node.setLayoutX(node.getLayoutX() + dx);

							resizeBox.setLayoutX(node.getLayoutX() - cornerSize);

							setSize(dwh[0] - dx, dwh[1] + dy);
						}
					}

					// 바뀐 크기 적용
					wh();
				});
			}


			return r;
		}

		// 테두리 생성
		private Line buildLine () {
			Line l = new Line ();

			l.setStroke(Color.rgb(0, 0, 0, 0.75));
			l.setStrokeWidth(0.5);

			return l;
		}

		// ResizeBox 너비, 높이 설정
		public double[] wh () {
			// 사각형의 현재 너비와 높이
			double width = bottom.getEndX() - bottom.getStartX();
			double height = right.getEndY() - right.getStartY();

			// 원
			if (node instanceof Ellipse) {
				((Ellipse) node).setRadiusX(width / 2);
				((Ellipse) node).setRadiusY(height / 2);
				((Ellipse) node).setCenterX(width / 2);
				((Ellipse) node).setCenterY(height / 2);
                resizeBox.setLayoutX(node.getBoundsInParent().getMinX() - cornerSize);
                resizeBox.setLayoutY(node.getBoundsInParent().getMinY() - cornerSize);
			}

			// 사각형
			else if (node instanceof Rectangle) {
				((Rectangle)node).setWidth(width);
				((Rectangle)node).setHeight(height);
			}

			// 이미지
			else if (node instanceof HBox) {
				((ImageView) (((HBox) node).getChildren().get(0))).setFitWidth(width);
				((ImageView) (((HBox) node).getChildren().get(0))).setFitHeight(height);
			}

			else if (node instanceof SVGPath) {
				SVGPath svgPath = (SVGPath) node;

				// 별
				if (svgPath.getContent().contains("  Z")) {
					svgPath.setContent(
							calculateStarContent(width, height));
				}

				// 화살표
				else {
					svgPath.setContent(
							calculateArrowContent(width, height));
				}

			}

			// 다각형
			else if (node instanceof Pane) {
				Polygon polygon = (Polygon) ((Pane) node).getChildren().get(0);

				int sides = polygon.getPoints().size() / 2;

				polygon.getPoints().setAll(calculatePolygonPoints(sides, width, height));


				((Pane) node).setPrefWidth(width);
				((Pane) node).setPrefHeight(height);


			}

			return new double[] {width, height};
		}



	}

	// Drag 클래스
	public static class Drag {
		public double x;
		public double y;

		// 드래그 여부 확인용 flag
		public boolean isDragging;
	}
}