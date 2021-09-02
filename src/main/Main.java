package main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class Main extends Application {

    public Scene scene;


    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("main.fxml"));
        Parent root = loader.load();

        SlideManager.setController(loader.getController());

        scene = new Scene(root, 480, 800);
        scene.getStylesheets().add(getClass().getResource("css/parrot.css").toString());

        initShortcut();

        primaryStage.setTitle("Parrot Project");

        primaryStage.getIcons().add(new Image(getClass().getResource("res/ParrotLogo.png").toString()));
        
        primaryStage.setScene(scene);
        primaryStage.sizeToScene();
		primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    // 단축키 설정
    public void initShortcut() {
        scene.setOnKeyPressed(event -> {
            if (event.getCode().equals(KeyCode.BACK_SPACE)
                    || event.getCode().equals(KeyCode.DELETE)) {     // 백스페이스 혹은 Delete키 입력 시 선택된 노드들 삭제
                SlideManager.getCurrentSlide().getChildren()
						.removeAll(NodeManager.getSelectedNodes());
                SlideManager.getCurrentSlide().getChildren()
                        .removeIf(node -> node instanceof NodeManager.ResizeBox);
            }

            if (event.getCode().equals(KeyCode.ESCAPE)) {       // Esc 로 리사이즈 모드 나가기
                if (SlideManager.getCurrentSlide().getChildren().contains(NodeManager.resizeBox)) {
                    SlideManager.getCurrentSlide().getChildren().removeAll(NodeManager.resizeBox);

                    if (NodeManager.getSelectedNodes().size() > 0) {
                        NodeManager.getSelectedNodes().get(0).getStyleClass().add("selected");
                    }
                }

            }

        });

        // Ctrl(Cmd) + F - 선택한 노드를 제일 앞으로
        final KeyCombination toFrontKey = new KeyCodeCombination(KeyCode.F,
                KeyCombination.SHORTCUT_DOWN);

        final KeyCombination toBackKey = new KeyCodeCombination(KeyCode.B,     // Ctrl(Cmd) + B
                KeyCombination.SHORTCUT_DOWN);

        // 둘 이상의 버튼을 동시에 누르는 단축키 처리
        scene.addEventHandler(KeyEvent.KEY_RELEASED, event -> {
            if (toFrontKey.match(event)) toFront.run();			// Ctrl(Cmd) + F
			else if (toBackKey.match(event)) toBack.run();		// Ctrl(Cmd) + B
        });

    }


	// 선택된 노드를 앞으로 가져오는 코드
	public static Runnable toFront = () -> {
		NodeManager.getSelectedNodes().stream()
				.sorted((a, b) -> {                // 순차 정렬
					Pane slide = SlideManager.getCurrentSlide();
					int indexA = slide.getChildren().indexOf(a);
					int indexB = slide.getChildren().indexOf(b);

					return ((Integer) indexA).compareTo(indexB);
				})
				.forEach(Node::toFront);		// 선택한 노드를 제일 앞으로
	};

	// 선택된 노드를 뒤로 보내는 코드
	public static Runnable toBack = () -> {
		NodeManager.getSelectedNodes().stream()
				.sorted((a, b) -> {             // 역순 정렬
					Pane slide = SlideManager.getCurrentSlide();
					int indexA = slide.getChildren().indexOf(a);
					int indexB = slide.getChildren().indexOf(b);

					return ((Integer) indexB).compareTo(indexA);
				})
				.forEach(Node::toBack);			// 선택한 노드를 제일 뒤로

		SlideManager.getCurrentSlide().getChildren()            // 현재 슬라이드에서
				.get(NodeManager.getSelectedNodes().size())     // dragArea를
				.toBack();                                      // 가장 뒤로 보낸다
	};
}
