package main;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;

import java.net.URL;
import java.util.ResourceBundle;

public class TextMenuController extends BtnEffectHelper implements Initializable {

	////////////
	// Fields //
	////////////

	@FXML public TextArea inputText;			// 텍스트 입력창
	@FXML public ColorPicker inputColor;		// 색상 선택창
	@FXML public TextField inputSize;			// 사이즈 입력창
	@FXML public TextField inputLink;			// 링크 입력창
	@FXML public ImageView checkImage;			// 링크 옆에 있는 체크 표시
	@FXML public Button createBtn;				// Create 버튼

	private double textSize;					// 텍스트 크기
	private boolean linkAvailable;				// 링크 사용 여부

	// 웹 주소인지 판단하기 위한 정규표현식
	private static final String regex = "^(https?)://([^:/\\s]+)(:([^/]*))?((/[^\\s/]+)*)?/?([^#\\s\\?]*)(\\?([^#\\s]*))?(#(\\w*))?$";

	/////////////
	// Methods //
	/////////////

	// 초기화 메소드
	@Override
	public void initialize(URL location, ResourceBundle resources) {

		// 기본 색상을 검정색으로
		inputColor.setValue(Color.BLACK);
		textSize = 15;

		inputSize.setText(textSize + "pt");

		// Size TextField의 focus 변화를 감지하는 Listener
		inputSize.focusedProperty().addListener((observable, oldValue, newValue) -> {
			if (inputSize.isFocused()) {													// Focus 되었을 때
				inputSize.setEditable(true);
				if (inputSize.getText().contains("pt")) {									// "pt" 문자열을 포함하면 편의를 위해 "pt"를 없앤다
					inputSize.setText(inputSize.getText(0, inputSize.getLength() - 2));
				}
			} else {																						// Focus가 풀렸을 때
				if (!inputSize.getText().contains("pt")) inputSize.setText(textSize + "pt");		// 값 마지막에 "pt"를 붙인다
				inputSize.setEditable(false);

			}
		});

		// Size TextField의 입력값이 바뀔 때
		inputSize.textProperty().addListener((observable, oldValue, newValue) -> {
			try {
				// 비어있지 않고 "pt"문자열을 포함하지 않을 때
				if (!newValue.isEmpty() && !newValue.contains("pt")) {
					// newValue가 실수 값이 아니면 NumberFormatException이 발생한다
					textSize = Double.parseDouble(newValue);
				}
			} catch (NumberFormatException e) {
				// 이전 값으로 되돌린다
				inputSize.setText(oldValue);
			} finally {
				// textSize가 0보다 작으면 이전 값으로 되돌린다
				if(textSize < 0) inputSize.setText(oldValue);
			}
		});

		// Link TextField의 값이 바뀔 때
		inputLink.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				// newValue가 "http(s)://~"이면
				if(newValue.matches(regex)) {
					// 노란색 체크로 변경
					checkImage.setImage(new Image(getClass().getResourceAsStream("res/checked.png")));
					linkAvailable = true;
				} else {
					// 회색 체크로 변경
					checkImage.setImage(new Image(getClass().getResourceAsStream("res/unchecked.png")));
					linkAvailable = false;
				}
			}
		});

	}

	// 키보드로 Size 값조절
	public void onInputSizeKeypressed(KeyEvent keyEvent) {
		if(keyEvent.getCode().equals(KeyCode.UP)) {						// 위 방향키 -> Size + 1
			inputSize.setText(String.valueOf(++textSize));
		} else if(keyEvent.getCode().equals(KeyCode.DOWN)) {			// 아래 방향키 -> Size - 1
			inputSize.setText(String.valueOf(--textSize));
		}
	}

	// Create 버튼 클릭 시
	public void onCreateBtnClicked() {
		Label label;
		if(linkAvailable) {						// 링크가 사용 가능할 때
			label = NodeManager.createText(
					inputText.getText(),
					textSize,
					inputColor.getValue(),
					inputLink.getText()
			);
		}
		else {
			label = NodeManager.createText(
					inputText.getText(),
					textSize,
					inputColor.getValue()
			);
		}

		SlideManager.addNode(label);
	}

	// 텍스트 속성 수정
	public void onModify(Label label) {
		label.setText(inputText.getText());
		label.setTextFill(inputColor.getValue());
		label.setStyle("-fx-font-size:" + textSize + ";");

		if(linkAvailable) label.setUserData(inputLink.getText());
	}
}
