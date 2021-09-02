package main;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.SubScene;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.LinkedList;
import java.util.ResourceBundle;

public class PlayMenuController extends BtnEffectHelper implements Initializable {

	@FXML public Pane simulatePane; // 시뮬레이션 : 슬라이드가 보이는 Pane
	@FXML public SubScene playScene;

	private int simulatePage = 0;   // 시뮬레이션 Pane의 현재 슬라이드

	LinkedList<Pane> getSlides = new LinkedList<>(SlideManager.getSlides());    // 플레이를 위해 슬라이드를 받아와 저장

	@Override
	public void initialize(URL location, ResourceBundle resources) {

		getSlides = SlideManager.getSlides();				// 플레이 시 슬라이드들을 가져옴
		if(getSlides.size() != 1) simulatePage = 1;			// defaultSlide가 아닌 슬라이드가 하나라도 있으면 첫번째 슬라이드로
		slideChange(simulatePage);

		getSlides.stream().forEach(slide -> {
			slide.getChildren().stream().forEach(node -> {
				if(node instanceof Label					// 하이퍼링크이면
						&& node.getUserData() != null) {
					// 다른 마우스 이벤트 없애기
					node.setOnMousePressed(null);
					node.setOnMouseDragged(null);

					node.setOnMouseReleased(event -> {
						if (event.getClickCount() == 1) {	// 클릭 시
							URI u = URI.create(node.getUserData().toString());		// 데이터에 담긴 주소를 URI로
							try {
								java.awt.Desktop.getDesktop().browse(u);			// 해당 주소로 이동!
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					});
				} else node.setMouseTransparent(true);

				node.getStyleClass().remove("selected");
			});
		});
	}

	public void slideChange(int page) {   // 시뮬레이션 안의 내용을 지우고 슬라이드를 넣음
		simulatePane.getChildren().clear();
		simulatePane.getChildren().add(getSlides.get(page));
	}

	public void playSceneOnKeyPressed(KeyEvent event) {
		if (event.getCode().equals(KeyCode.RIGHT)) {
			if (simulatePage < SlideManager.getTotalPage()) slideChange(++simulatePage);
		} else if (event.getCode().equals(KeyCode.LEFT)) {
			if(simulatePage > 1) slideChange(--simulatePage);
		}
	}
}
