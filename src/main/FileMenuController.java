package main;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.LinkedList;

public class FileMenuController extends BtnEffectHelper {

	////////////
	// Fields //
	////////////

	@FXML public Button newFileBtn;					// New File 버튼
	@FXML public Button openFileBtn;				// Open File 버튼
	@FXML public Button saveFileBtn;				// Save File 버튼
	@FXML public Button exportToImgBtn;				// Export to Image 버튼

	/////////////
	// Methods //
	/////////////


	// New File 버튼 클릭 시(새 파일)
	public void onNewFileBtnClicked() {
		if(SlideManager.getSlides().size() != 0) {
			SlideManager.setSlides(new LinkedList<>());
			SlideManager.changeSlideTo(0);
	 	}
	}

	// Open File 버튼 클릭 시(파일 불러오기)
	public void onOpenFileBtnClicked() {
		// Open 창 띄우기
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Open File");
		fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Parrot Files", "*.parrot"));		// .parrot 확장자 지정
		fileChooser.setInitialFileName("New Slide");
		File parrotFile = fileChooser.showOpenDialog(null);

		// 불러오기
		if(parrotFile != null) FileManager.openFile(parrotFile);
	}

	// Save File 버튼 클릭 시
	public void onSaveFileBtnClicked() {

		// Save 창 띄우기
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Where to Save?");
		fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Parrot Files", "*.parrot"));		// .parrot 확장자 지정
		fileChooser.setInitialFileName("New Slide");		// 기본 파일명 지정
		File outputFile = fileChooser.showSaveDialog(null);

		// 파일 저장
		if(outputFile != null) FileManager.saveFile(outputFile);

	}

	// Export to Image 버튼 클릭 시
	public void onExportToImgBtnClicked() {

		// 디렉토리 선택 창 띄우기
		DirectoryChooser directoryChooser = new DirectoryChooser();
		directoryChooser.setTitle("Export to Image");
		File selectedDir = directoryChooser.showDialog(null);

		FileManager.exportToImage(selectedDir);
	}
}
