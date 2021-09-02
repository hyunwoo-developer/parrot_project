package main;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class Controller extends BtnEffectHelper {

	////////////
	// Fields //
	////////////

	// FXML
	@FXML public Pane parrot;           							//  프로그램 전체 컨테이너
	@FXML public Pane slide;    									//  슬라이드 컨테이너
	@FXML public AnchorPane bottomMenu;								//	하단 메뉴바
	@FXML public Button playBtn;        							//  플레이 버튼 : 클릭 시 발표모드로 변환
	@FXML public Label pageLbl;         							//  슬라이드 라벨 : 현재 슬라이드의 번호와 전체 슬라이드의 수를 보여줌
	@FXML public Button leftBtn;        							//  이전 슬라이드 버튼 : 클릭 시 이전 슬라이드로 이동
	@FXML public Button rightBtn;       							//  다음 슬라이드 버튼 : 클릭 시 다음 슬라이드로 이동
	@FXML public Button addBtn;         							//  추가 버튼 : 클릭 시 슬라이드 추가
	@FXML public Button deleteBtn;      							//  삭제 버튼 : 클릭 시 현재 슬라이드의 모든 내용 삭제
	@FXML public Button textBtn;        							//  텍스트 버튼 : 클릭 시 텍스트 생성 메뉴
	@FXML public Button shapeBtn;       							//  도형 버튼 : 클릭 시 도형 생성 메뉴
	@FXML public Button imageBtn;       							//  이미지 버튼 : 클릭 시 이미지 생성 메뉴
	@FXML public Button fileBtn;        							//  파일 버튼 : 클릭 시 로드, 저장 메뉴

	// 메뉴 상태 플래그
	private static final int MENU_CLOSED = 0;						// 메뉴가 닫혀있음
	private static final int TEXT_OPENED = 1;						// 텍스트 메뉴가 열려있음
	private static final int SHAPE_OPENED = 2;						// 도형 메뉴가 열려있음
	private static final int IMAGE_OPENED = 3;						// 이미지 메뉴가 열려있음
	private static final int FILE_OPENED = 4;						// 파일 메뉴가 열려있음
	private int flag = MENU_CLOSED;									// 현재 메뉴 상태 플래그

	private final URL[] menuFXML = new URL[] {						// 하단 메뉴 FXML 배열
			getClass().getResource("fxml/text_menu.fxml"),			// Text
			getClass().getResource("fxml/shape_menu.fxml"),			// Shape
			getClass().getResource("fxml/image_menu.fxml"),			// Image
			getClass().getResource("fxml/file_menu.fxml")			// File
	};

	private Pane currentMenu;										// 현재 메뉴 화면

	//
	// 상단 메뉴 이벤트
	//

	public void onPlayBtnClicked() {
		try {
			Stage playStage = new Stage();
			Parent root = FXMLLoader.load(getClass().getResource("fxml/play_menu.fxml"));
			assert root != null;
			playStage.setTitle("Play Mode");
			
			playStage.getIcons().add(new Image(getClass().getResource("res/ParrotLogo.png").toString()));
			
			playStage.setScene(new Scene(root, 800, 480));
			playStage.sizeToScene();
			playStage.setResizable(false);
			playStage.show();

			playStage.setOnCloseRequest(event -> {  // 플레이 종료시 메인에서의 원활한 편집활동을 위한 복구 작업
				slide.getChildren().clear();		// 플레이로 인한 메인 슬라이드 지우기
				slide.getChildren().add(SlideManager.getSlides().get(SlideManager.getCurrentPage()));   // 현재 슬라이드 인덱스에 맞게 슬라이드를 넣음

				SlideManager.getSlides().stream().forEach(slide -> {        // 슬라이드 마다
					slide.getChildren().stream().forEach(node -> {            // 그 안의 노드들 마다
						node.setMouseTransparent(false);                    // 마우스 이벤트 활성화
						if (node instanceof Label                                                    // 하이퍼링크이면
								&& node.getUserData() != null) {
							node.setOnMouseReleased(null);
							NodeManager.makeDraggable(node);        // 마우스 이벤트 원상복구
						}
					});
				});
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// 왼쪽 화살표(이전 슬라이드로)
	public void onLeftBtnClicked() {
		if(SlideManager.getCurrentPage() > 1) {
			SlideManager.changeSlideTo(SlideManager.getCurrentPage() - 1);
		}
	}

	// 오른쪽 화살표(다음 슬라이드로)
	public void onRightBtnClicked() {
		if(SlideManager.getCurrentPage() < SlideManager.getTotalPage()) {
			SlideManager.changeSlideTo(SlideManager.getCurrentPage() + 1);
		}
	}

	public void onAddBtnClicked() {
		SlideManager.addSlide();
	}

	public void onDeleteBtnClicked() {
		SlideManager.deleteSlide();
	}

	//
	// 하단 메뉴 이벤트
	//

	public void onTextBtnClicked() {
		if(flag != TEXT_OPENED) {										// 현재 텍스트 메뉴가 열려있지 않으면 텍스트 메뉴를 연다
			flag = TEXT_OPENED;
			menuOpen();

			// 메뉴 레이아웃 로드
			try {
				FXMLLoader loader = new FXMLLoader(menuFXML[0]);
				currentMenu = loader.load();
			} catch (IOException e) {
				e.printStackTrace();
			}

			bottomMenu.getChildren().add(currentMenu);

		} else {														// 이미 텍스트 메뉴가 열려있으면 메뉴를 닫는다
			flag = MENU_CLOSED;
			menuClose();
		}
	}

	public void onShapeBtnClicked() {
		if(flag != SHAPE_OPENED) {										// 현재 도형 메뉴가 열려있지 않으면 도형 메뉴를 연다
			flag = SHAPE_OPENED;
			menuOpen();

			// 메뉴 레이아웃 로드
			try {
				FXMLLoader loader = new FXMLLoader(menuFXML[1]);
				currentMenu = loader.load();
			} catch (IOException e) {
				e.printStackTrace();
			}

			bottomMenu.getChildren().add(currentMenu);
		} else {														// 이미 도형 메뉴가 열려있으면 메뉴를 닫는다
			flag = MENU_CLOSED;
			menuClose();
		}
	}

	public void onImageBtnClicked() {
		if(flag != IMAGE_OPENED) {										// 현재 이미지 메뉴가 열려있지 않으면 이미지 메뉴를 연다
			flag = IMAGE_OPENED;
			menuOpen();

			// 메뉴 레이아웃 로드
			try {
				FXMLLoader loader = new FXMLLoader(menuFXML[2]);
				currentMenu = loader.load();
			} catch (IOException e) {
				e.printStackTrace();
			}

			bottomMenu.getChildren().add(currentMenu);
		} else {														// 이미 이미지 메뉴가 열려있으면 메뉴를 닫는다
			flag = MENU_CLOSED;
			menuClose();
		}
	}

	public void onFileBtnClicked() {
		if(flag != FILE_OPENED) {										// 현재 이미지 메뉴가 열려있지 않으면 이미지 메뉴를 연다
			flag = FILE_OPENED;
			menuOpen();

			// 메뉴 레이아웃 로드
			try {
				FXMLLoader loader = new FXMLLoader(menuFXML[3]);
				currentMenu = loader.load();
			} catch (IOException e) {
				e.printStackTrace();
			}

			bottomMenu.getChildren().add(currentMenu);
		} else {														// 이미 이미지 메뉴가 열려있으면 메뉴를 닫는다
			flag = MENU_CLOSED;
			menuClose();
		}
	}

	// 텍스트 수정 메뉴 열기 (Create 메뉴에서 약간 수정)
	public void onModifyTextCalled(Node n) {
		if(flag != MENU_CLOSED) bottomMenu.getChildren().remove(currentMenu);
		else menuOpen();

		flag = TEXT_OPENED;
		// Create 시 메뉴  레이아웃 로드
		try {
			FXMLLoader loader = new FXMLLoader(menuFXML[0]);
			currentMenu = loader.load();
			TextMenuController controller = loader.getController();


			// 메뉴에 값 세팅
			Label nodeToModify = (Label) n;
			String userData = (String) nodeToModify.getUserData();
			Color color = (Color) nodeToModify.getTextFill();

			if(controller != null) {
				controller.inputText.setText(nodeToModify.getText());
				controller.inputColor.setValue(color);
				controller.inputSize.setText(String.valueOf(nodeToModify.getFont().getSize()));
				if (userData != null) controller.inputLink.setText(userData);
				controller.createBtn.setText("Modify");
				controller.createBtn.setOnAction(event -> controller.onModify((Label) n));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		bottomMenu.getChildren().add(currentMenu);
	}

	// 메뉴 열기 메소드
	private void menuOpen() {
		bottomMenu.getChildren().remove(currentMenu);

		bottomMenu.setPrefHeight(364);
		bottomMenu.setLayoutY(436);
		slide.setLayoutY(86);

		textBtn.setLayoutY(310);
		shapeBtn.setLayoutY(313);
		imageBtn.setLayoutY(313.5);
		fileBtn.setLayoutY(313);
	}

	// 메뉴 닫기 메소드
	public void menuClose() {
		bottomMenu.getChildren().remove(currentMenu);

		bottomMenu.setPrefHeight(64);
		bottomMenu.setLayoutY(736);
		slide.setLayoutY(235);

		textBtn.setLayoutY(10);
		shapeBtn.setLayoutY(13);
		imageBtn.setLayoutY(13.5);
		fileBtn.setLayoutY(13);
	}

}
