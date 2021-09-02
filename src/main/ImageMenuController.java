package main;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;

import java.io.File;

public class ImageMenuController extends BtnEffectHelper {

    @FXML public TextField srcField;
    @FXML public Button srcBtn;
    @FXML public Button createBtn;

    // Create 버튼 클릭 시
    public void onCreateBtnClicked() {
        if(srcField.getText() != null) {
            HBox imageView = NodeManager.createImage(srcField.getText());
			SlideManager.addNode(imageView);
        }
    }

    // src 버튼 클릭 시 파일 선택 창 띄우기
    public void srcBtnOnAction() {

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Parrot Project");

        // 파일 확장자 이미지로 제한하기
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));

        // 파일 선택 창 생성
        File selectFile = fileChooser.showOpenDialog(null);
        srcField.setText(null);

        if (selectFile != null) {
            srcField.setText(selectFile.getPath());
        }
    }

}
