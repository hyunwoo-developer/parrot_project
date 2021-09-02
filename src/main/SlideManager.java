package main;

import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.LinkedList;
import java.util.List;

public class SlideManager {

	public static final int SLIDE_WIDTH = 440;						// 슬라이드 가로 길이
	public static final int SLIDE_HEIGHT = 330;						// 슬라이드 세로 길이

	private static int totalPage = 0;								//	전체 슬라이드 수
	private static int currentPage = 0;								//	현재 슬라이드 번호

	private static LinkedList<Pane> slides = new LinkedList<>();	// 슬라이드 리스트
	private static Controller controller;							// 메인 컨트롤러
	private static Pane defaultSlide;

	static {
		// 기본 슬라이드 세팅
		defaultSlide = makeSlide();
		ImageView parrotImg = new ImageView();
		parrotImg.setImage(new Image(SlideManager.class.getResourceAsStream("res/Default_slide.png")));
		parrotImg.setFitWidth(SLIDE_WIDTH);
		parrotImg.setFitHeight(SLIDE_HEIGHT);
		defaultSlide.getChildren().add(parrotImg);
		slides.add(defaultSlide);
	}

	private SlideManager() {}

	// 새 슬라이드 추가(Controller의 onAddBtnClicked)
	public static void addSlide() {
		Pane newSlide = makeSlide();

		if(currentPage == totalPage) {								// 현재 페이지가 마지막 페이지일 때
			slides.add(newSlide);
			currentPage++;
		} else {													// 현재 페이지가 마지막 페이지가 아닐 때
			slides.add(++currentPage, newSlide);					// 바로 다음장에 새 슬라이드 추가
		}

		totalPage++;

		changeSlideTo(currentPage);
	}

	// 현재 슬라이드 제거(Controller의 onDeleteBtnClicked)
	public static void deleteSlide() {
		if (totalPage != 0 && currentPage != 0) {
			slides.remove(currentPage);                            // 현재 슬라이드를 리스트에서 제거
			if (currentPage == totalPage) currentPage--;
			totalPage--;
			changeSlideTo(currentPage);
		}
	}

	// 새 슬라이드 생성
	public static Pane makeSlide() {

		// 새 슬라이드
		Pane newSlide = new Pane();
		newSlide.setPrefWidth(SLIDE_WIDTH);
		newSlide.setPrefHeight(SLIDE_HEIGHT);

		// 드래그 인식 공간
		Pane dragArea = new Pane();
		dragArea.setPrefWidth(SLIDE_WIDTH);
		dragArea.setPrefHeight(SLIDE_HEIGHT);
		initDragAndSelect(dragArea);
		newSlide.getChildren().add(dragArea);

		return newSlide;
	}

	// 슬라이드에 Node 추가
	public static void addNode(Node n) {
		if(totalPage == 0) addSlide();

		slides.get(currentPage).getChildren().add(n);
	}

	// 표시되는 슬라이드 변경
	public static void changeSlideTo(int page) {
		// 슬라이드 컨테이너에 해당 페이지의 슬라이드를 넣음
		controller.slide.getChildren().remove(0);
		controller.slide.getChildren().add(slides.get(page));

		currentPage = page;

		// 페이지 Label 텍스트 변경
		controller.pageLbl.setText(currentPage + "/" + totalPage);
	}

	// Drag & Select 설정
	public static void initDragAndSelect(Pane dragArea) {
		// 인덱스 0 - X좌표, 인덱스 1 - Y좌표
		double[] slideLayoutXY = new double[2];					// slide 좌표
		NodeManager.Drag drag = new NodeManager.Drag();			// 드래그 시작점 좌표

		List<Node> selectedNodes = NodeManager.getSelectedNodes();

		// 드래그 영역 표시하는 사각형 생성
		Rectangle selectRect = new Rectangle();
		selectRect.setFill(Color.GRAY);
		selectRect.setOpacity(0.6);

		dragArea.setOnMousePressed(event -> {
			drag.isDragging = false;

			// 현재 슬라이드
			Pane slide = slides.get(currentPage);

			// 슬라이드 컨테이너 좌표
			slideLayoutXY[0] = controller.slide.getLayoutX();
			slideLayoutXY[1] = controller.slide.getLayoutY();

			// 드래그 시작 좌표
			drag.x = event.getSceneX();
			drag.y = event.getSceneY();

			// selectRect 초기 설정
			selectRect.setLayoutX(drag.x);
			selectRect.setLayoutY(drag.y);
			selectRect.setWidth(0);
			selectRect.setHeight(0);
			selectRect.toFront();

			// selectRect의 경계(크기)가 변할 때
			selectRect.boundsInParentProperty().addListener((observable, oldValue, newValue) -> {
				slide.getChildren().stream()
						.skip(1)												// 첫번째 노드(dragArea) 건너뛰고
						.filter(n -> n != selectRect)							// selectRect가 아니고
						.filter(n -> !(n instanceof NodeManager.ResizeBox))		// ResizeBox가 아닌 노드에 대해
						.forEach(n -> {
							// 사각형이 노드와 겹쳤을 때
							if (newValue.intersects(n.getBoundsInParent())) {
								if (!selectedNodes.contains(n)) {
									// selected 스타일클래스 추가
									n.getStyleClass().add("selected");
									selectedNodes.add(n);
								}
							} else {
								if (selectedNodes.contains(n)) {
									selectedNodes.remove(n);
									n.getStyleClass().remove("selected");
								}
							}
						});
			});

			selectedNodes.stream().forEach(n -> n.getStyleClass().clear());
			selectedNodes.clear();

			if (!slide.getChildren().contains(selectRect)) slide.getChildren().add(selectRect);
		});

		dragArea.setOnDragDetected(event -> drag.isDragging = true);

		dragArea.setOnMouseDragged(event -> {
			double mouseX = event.getSceneX();
			double mouseY = event.getSceneY();

			// selectRect 크기 조절
			if (drag.x <= mouseX) {
				selectRect.setLayoutX(drag.x - slideLayoutXY[0]);
				selectRect.setWidth(mouseX - drag.x);
			} else {
				selectRect.setLayoutX(mouseX - slideLayoutXY[0]);
				selectRect.setWidth(drag.x - mouseX);
			}

			if (drag.y <= mouseY) {
				selectRect.setLayoutY(drag.y - slideLayoutXY[1]);
				selectRect.setHeight(mouseY - drag.y);
			} else {
				selectRect.setLayoutY(mouseY - slideLayoutXY[1]);
				selectRect.setHeight(drag.y - mouseY);
			}
		});

		dragArea.setOnMouseReleased(event -> {
			Pane slide = slides.get(currentPage);
			if (!drag.isDragging) {
				selectedNodes.forEach(n -> n.getStyleClass().clear());
				selectedNodes.clear();
			}

			slide.getChildren().remove(selectRect);
		});
	}

	// Fields Getter, Setter
	public static int getCurrentPage() {
		return currentPage;
	}

	public static int getTotalPage() {
		return totalPage;
	}

	public static Controller getController() {
		return controller;
	}

	public static LinkedList<Pane> getSlides() {
		return slides;
	}

	public static void setController(Controller controller) {
		SlideManager.controller = controller;
	}

	public static void setSlides(LinkedList<Pane> slides) {
		if(slides.size() == 0 || !slides.get(0).equals(defaultSlide))
			slides.add(0, defaultSlide);
		totalPage = slides.size() - 1;
		SlideManager.slides = slides;
	}

	// 편의성을 위한 Getter
	public static Pane getCurrentSlide() {
		return slides.get(currentPage);
	}

}
