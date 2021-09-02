package main;

import javafx.event.Event;
import javafx.scene.control.Button;
import javafx.scene.effect.Bloom;

// Controller에 공통으로 들어있는 이펙트 처리
public abstract class BtnEffectHelper {
	private Bloom bloom = new Bloom(0.2);

	public void onBtnEntered(Event event) {
		Object source = event.getSource();
		if(source instanceof Button) ((Button) source).setEffect(bloom);
	}

	public void onBtnExited(Event event) {
		Object source = event.getSource();
		if(source instanceof Button) ((Button) source).setEffect(null);
	}
}
